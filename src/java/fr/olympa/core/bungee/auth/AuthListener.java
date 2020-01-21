package fr.olympa.core.bungee.auth;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.Gson;

import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.sql.MySQL;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.utils.BungeeUtils;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

@SuppressWarnings("deprecation")
public class AuthListener implements Listener {

	private Cache<String, UUID> cachePremiumUUID = CacheBuilder.newBuilder().expireAfterWrite(4, TimeUnit.SECONDS).build();
	private Cache<String, OlympaPlayer> cachePlayer = CacheBuilder.newBuilder().expireAfterWrite(4, TimeUnit.SECONDS).build();

	@EventHandler(priority = EventPriority.LOW)
	public void on1PreLogin(PreLoginEvent event) {
		if (event.isCancelled()) {
			return;
		}
		PendingConnection connection = event.getConnection();
		String name = connection.getName();
		AccountProvider account;
		OlympaPlayer olympaPlayer;
		UUID uuidCrack = null;

		System.out.println("PreLoginEvent onlinemode ? " + connection.isOnlineMode());
		try {
			olympaPlayer = AccountProvider.get(name);
		} catch (SQLException e) {
			e.printStackTrace();
			event.setCancelled(true);
			event.setCancelReason(BungeeUtils.connectScreen("§cUne erreur est survenue. \n\n§e§lMerci de la signaler au staff.\n§eCode d'erreur: §l#SQLBungeeLost"));
			return;
		}
		if (olympaPlayer == null) {
			UUID uuidPremium = AuthUtils.getUuid(connection);
			try {
				uuidCrack = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name.toLowerCase()).getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			if (uuidPremium == null) {
				event.setCancelled(true);
				event.setCancelReason(BungeeUtils.connectScreen("§7[§cSécuriter§7] §eLes cracks ne sont pas encore autoriser."));
				connection.setOnlineMode(false);
				connection.setUniqueId(uuidCrack);
				System.out.println("null offlinemode");
				return;
			} else {
				this.cachePremiumUUID.put(name, uuidPremium);
				connection.setOnlineMode(true);

				// Vérifie si le joueur n'a pas changé de nom.
				try {
					olympaPlayer = MySQL.getPlayerByPremiumUuid(uuidPremium);
				} catch (SQLException e) {
					e.printStackTrace();
					event.setCancelled(true);
					event.setCancelReason(BungeeUtils.connectScreen("§cUne erreur est survenue. \n\n§e§lMerci de la signaler au staff.\n§eCode d'erreur: §l#SQLBungeePreLogin"));
					return;
				}
				if (olympaPlayer != null) {
					olympaPlayer.addNewName(name);
				}
				System.out.println("null onlinemode");
			}
			System.out.println("NEW JOUEUR  " + connection.getUniqueId() + " UUID CRACK: " + uuidCrack + " HAS PREMIUM ? " + uuidPremium);
		}

		if (olympaPlayer != null) {
			System.out.println("player info " + new Gson().toJson(olympaPlayer));
			if (olympaPlayer.getPremiumUniqueId() == null) {
				connection.setOnlineMode(false);
			} else {
				connection.setOnlineMode(true);
			}
			account = new AccountProvider(olympaPlayer.getUniqueId());
			account.saveToCache(olympaPlayer);
			this.cachePlayer.put(name, olympaPlayer);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void on2Login(LoginEvent event) throws SQLException, UnsupportedEncodingException {
		if (event.isCancelled()) {
			return;
		}
		PendingConnection connection = event.getConnection();
		System.out.println("LoginEvent onlinemode ? " + connection.isOnlineMode());
		String name = connection.getName();
		UUID uuidPremium = this.cachePremiumUUID.getIfPresent(name);
		String ip = connection.getAddress().getAddress().getHostAddress();
		OlympaPlayer olympaPlayer = this.cachePlayer.getIfPresent(name);
		AccountProvider olympaAccount;
		if (olympaPlayer != null) {
			olympaAccount = new AccountProvider(olympaPlayer.getUniqueId());
			this.cachePlayer.invalidate(name);
		} else {
			UUID uuidCrack = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name.toLowerCase()).getBytes("UTF-8"));
			olympaAccount = new AccountProvider(uuidCrack);
			olympaPlayer = olympaAccount.createOlympaPlayer(name, ip);
			if (uuidPremium != null) {
				olympaPlayer.setPremiumUniqueId(uuidPremium);
			}
			this.cachePremiumUUID.invalidate(name);
			if (!olympaAccount.createNew(olympaPlayer)) {
				event.setCancelled(true);
				event.setCancelReason(BungeeUtils.connectScreen("&cUne erreur est survenue. \n\n&e&lMerci de la signaler au staff.\n&eCode d'erreur: &l#SQLBungeeCantCreateNew"));
				return;
			}
			olympaPlayer = MySQL.getPlayer(olympaPlayer.getUniqueId());
			OlympaBungee.getInstance().sendMessage("Nouveau joueur: " + olympaPlayer.getId() + " " + olympaPlayer.getName());
		}
		try {
			Class<?> initialHandlerClass = connection.getClass();
			Field uniqueIdField = initialHandlerClass.getDeclaredField("uniqueId");
			uniqueIdField.setAccessible(true);
			uniqueIdField.set(connection, olympaPlayer.getUniqueId());
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		olympaAccount.saveToRedis(olympaPlayer);
	}

	@EventHandler(priority = EventPriority.LOW)
	public void on3PostLogin(PostLoginEvent event) {
		ProxiedPlayer player = event.getPlayer();
		System.out.println("PostLoginEvent onlinemode ? " + player.getPendingConnection().isOnlineMode());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void on4Disconnect(PlayerDisconnectEvent event) {
		ProxiedPlayer player = event.getPlayer();
		AccountProvider olympaAccount = new AccountProvider(player.getUniqueId());
		ProxyServer.getInstance().getScheduler().schedule(OlympaBungee.getInstance(), () -> {
			olympaAccount.accountExpire();
			OlympaPlayer olympaPlayer = olympaAccount.getFromRedis();
			if (olympaPlayer == null) {
				System.out.println("ATTENTION le joueur " + player.getUniqueId() + " n'avait pas de donnés dans redis.");
				return;
			}
			olympaAccount.saveToDb(olympaPlayer);
		}, 1, TimeUnit.SECONDS);
	}

}
