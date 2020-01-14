package fr.olympa.core.bungee.auth;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.sql.MySQL;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.utils.BungeeUtils;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

@SuppressWarnings("deprecation")
public class AuthListener implements Listener {

	Cache<String, String> cache = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).build();

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onDisconnect(PlayerDisconnectEvent event) {
		ProxiedPlayer player = event.getPlayer();
		AccountProvider olympaAccount = new AccountProvider(player.getUniqueId());

		OlympaPlayer olympaPlayer = olympaAccount.getFromRedis();
		if (olympaPlayer == null) {
			System.out.println("ATTENTION le joueur " + olympaPlayer + " n'avait pas de donnés dans redis.");
			return;
		}

		olympaAccount.accountExpire();
		olympaAccount.saveToDbBungee(olympaPlayer);
	}

	/*@EventHandler(priority = EventPriority.LOWEST)
	public void onLogin(LoginEvent event) {
		PendingConnection connection = event.getConnection();
		String name = connection.getName();
		String ip = connection.getAddress().getAddress().getHostAddress();

		UUID uuid_premium = event.getConnection().getUniqueId();
		AccountProvider olympaAccount = new AccountProvider(uuid_premium);

		OlympaPlayer olympaPlayer;
		try {
			olympaPlayer = olympaAccount.get();
		} catch (SQLException e) {

			event.setCancelled(true);
			event.setCancelReason(BungeeUtils.connectScreen("§cUne erreur est survenue. \n\n§e§lMerci de la signaler au staff.\n§eCode d'erreur: §l#SQLBungeeLogin"));
			e.printStackTrace();
			return;
		}
		this.cache.invalidate(ip);
	}*/

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onLogin2(LoginEvent event) {
		PendingConnection connection = event.getConnection();
		UUID uuid = connection.getUniqueId();
		String name = connection.getName();
		String ip = connection.getAddress().getAddress().getHostAddress();
		AccountProvider olympaAccount = new AccountProvider(uuid);
		OlympaPlayer olympaPlayer = null;
		System.out.println("onlinemode ? " + connection.isOnlineMode());
		this.cache.invalidate(ip);
		try {
			olympaPlayer = olympaAccount.get();
			if (olympaPlayer == null) {
				olympaPlayer = olympaAccount.createOlympaPlayer(name, ip);

				String uuidPremium = this.cache.asMap().get(ip);
				if (uuidPremium != null && !uuidPremium.isEmpty()) {
					olympaPlayer.setPremiumUniqueId(UUID.fromString(uuidPremium));
				}
				if (!olympaAccount.createNew(olympaPlayer)) {
					event.setCancelled(true);
					event.setCancelReason(BungeeUtils.connectScreen("§cUne erreur est survenue. \n\n§e§lMerci de la signaler au staff.\n§eCode d'erreur: §l#SQLBungeeCantCreateNew"));
					return;
				}
				OlympaBungee.getInstance().sendMessage("Nouveau joueur: " + olympaPlayer.getName());
			}
		} catch (SQLException e) {
			event.setCancelled(true);
			event.setCancelReason(BungeeUtils.connectScreen("§cUne erreur est survenue. \n\n§e§lMerci de la signaler au staff.\n§eCode d'erreur: §l#SQLBungeeLogin"));
			e.printStackTrace();
		}

		olympaAccount.saveToRedisBungee(olympaPlayer);
	}

	@EventHandler
	public void onPing(ProxyPingEvent event) {
		PendingConnection connection = event.getConnection();
		this.cache.put(connection.getAddress().getAddress().getHostAddress(), "");
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPostLogin(PostLoginEvent event) {
		ProxiedPlayer connection = event.getPlayer();
		System.out.println("onlinemode ? " + connection.getPendingConnection().isOnlineMode());
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPreLogin(PreLoginEvent event) {
		if (event.isCancelled()) {
			return;
		}
		PendingConnection connection = event.getConnection();
		System.out.println("onlinemode ? " + connection.isOnlineMode());
		String name = connection.getName();
		String ip = connection.getAddress().getAddress().getHostAddress();

		if (!name.matches("[a-zA-Z0-9_]*")) {
			event.setCancelReason(BungeeUtils.connectScreen("§6Ton pseudo doit contenir uniquement des chiffres, des lettres et des tiret bas."));
			return;
		}

		String test = this.cache.asMap().get(ip);
		if (test == null) {
			event.setCancelReason(BungeeUtils.connectScreen("§7[§cSécuriter§7] §6Tu dois ajouter le serveur avant de pouvoir te connecter.\n La connexion direct n'est pas autoriser."));
			return;
		}
		OlympaPlayer olympaPlayer;
		try {
			olympaPlayer = MySQL.getPlayersByName(name);
		} catch (SQLException e) {
			e.printStackTrace();
			event.setCancelReason(BungeeUtils.connectScreen("§cUne erreur est survenue. \n\n§e§lMerci de la signaler au staff.\n§eCode d'erreur: §l#SQLBungeePreLogin"));
			return;
		}
		if (olympaPlayer == null) {
			UUID uuid = AuthUtils.getUuid(connection);
			if (uuid == null) {
				event.setCancelled(true);
				event.setCancelReason(BungeeUtils.connectScreen("§7[§cSécuriter§7] §eLes cracks ne sont pas encore autoriser."));
				System.out.println("null offlinemode");
				return;
				// connection.setOnlineMode(false);
			} else {
				this.cache.put(name, uuid.toString());
				connection.setOnlineMode(true);
				try {
					connection.setUniqueId(UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes("UTF_8")));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				System.out.println("null onlinemode");
			}
		} else {
			connection.setUniqueId(olympaPlayer.getUniqueId());
			if (olympaPlayer.getPremiumUniqueId() != null) {
				System.out.println("olympaPlayer onlinemode");
				connection.setOnlineMode(true);
			} else {
				System.out.println("olympaPlayer offlinemode");
				connection.setOnlineMode(false);
			}
			new AccountProvider(olympaPlayer.getUniqueId()).saveToCache(olympaPlayer);
		}

	}

	/*@EventHandler(priority = EventPriority.LOWEST)
	public void onServerConnect(ServerConnectEvent event) {

		ProxiedPlayer player = event.getPlayer();
		String name = player.getName();
		String ip = this.cache.asMap().get(name);
		if (ip == null) {
			return;
		}
		ServerInfo lobby = OlympaBungee.getInstance().getProxy().getServers().get("lobby1");
		if (lobby == null) {
			event.setTarget(lobby);
		}
		this.cache.invalidate(name);
	}*/
}
