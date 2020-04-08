package fr.olympa.core.bungee.datamanagment;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.provider.BungeeNewPlayerEvent;
import fr.olympa.api.sql.MySQL;
import fr.olympa.api.utils.GsonCustomizedObjectTypeAdapter;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.api.mojangapi.MojangAPI;
import fr.olympa.core.bungee.api.mojangapi.objects.UuidResponse;
import fr.olympa.core.bungee.login.events.OlympaPlayerLoginEvent;
import fr.olympa.core.bungee.utils.BungeeUtils;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

@SuppressWarnings("deprecation")
public class AuthListener implements Listener {

	public static Cache<String, String> cacheServer = CacheBuilder.newBuilder().expireAfterWrite(60, TimeUnit.SECONDS).build();
	private Cache<String, UUID> cachePremiumUUID = CacheBuilder.newBuilder().expireAfterWrite(60, TimeUnit.SECONDS).build();
	private Cache<String, OlympaPlayer> cachePlayer = CacheBuilder.newBuilder().expireAfterWrite(60, TimeUnit.SECONDS).build();
	private Set<String> wait = new HashSet<>();

	@EventHandler(priority = EventPriority.LOW)
	public void on1PreLogin(PreLoginEvent event) {
		if (event.isCancelled()) {
			return;
		}
		PendingConnection connection = event.getConnection();
		String name = connection.getName();
		// String ip = connection.getAddress().getAddress().getHostAddress();
		OlympaPlayer olympaPlayer;
		UUID uuidCrack = null;

		if (wait.contains(name)) {
			event.setCancelReason(BungeeUtils.connectScreen("&eMerci de patienter avant chaque reconnection."));
			event.setCancelled(true);
			return;
		}
		try {
			olympaPlayer = AccountProvider.get(name);
		} catch (Exception e) {
			e.printStackTrace();
			event.setCancelReason(BungeeUtils.connectScreen("&cUne erreur est survenue. \n\n&e&lMerci de la signaler au staff.\n&eCode d'erreur: &l#SQLBungeeLost"));
			event.setCancelled(true);
			return;
		}
		if (olympaPlayer == null) {
			UuidResponse response;
			try {
				response = MojangAPI.getFromName(connection);
			} catch (IOException e) {
				e.printStackTrace();
				event.setCancelReason(BungeeUtils.connectScreen("&cUne erreur est survenu avec les serveurs d'authentifications de Mojang.\n&eCode d'erreur: &l#BungeeMojangNewPlayer"));
				event.setCancelled(true);
				return;
			}
			UUID uuidPremium = null;
			if (response != null) {
				if (!response.getName().equals(name)) {
					event.setCancelReason(BungeeUtils.connectScreen("&cLe pseudo &4" + response.getName() + "&c est un compte premium.\nTu ne peux pas l'utiliser.\n&eCode d'erreur: &l#BungeeNoPremium"));
					event.setCancelled(true);
					return;
				}
				uuidPremium = response.getUuid();
			}
			try {
				uuidCrack = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name.toLowerCase()).getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			if (uuidPremium == null) {
				event.setCancelReason(BungeeUtils.connectScreen("&eLes cracks ne sont pas encore autoriser."));
				event.setCancelled(true);
				connection.setOnlineMode(false);
				connection.setUniqueId(uuidCrack);
				System.out.println("Crack with no data " + name);
			} else {
				cachePremiumUUID.put(name, uuidPremium);
				connection.setOnlineMode(true);

				// Vérifie si le joueur n'a pas changé de nom.
				try {
					olympaPlayer = MySQL.getPlayerByPremiumUuid(uuidPremium);
				} catch (SQLException e) {
					e.printStackTrace();
					event.setCancelReason(BungeeUtils.connectScreen("&cUne erreur est survenue. \n\n&e&lMerci de la signaler au staff.\n&eCode d'erreur: &l#SQLBungeePreLogin"));
					event.setCancelled(true);
					return;
				}
				if (olympaPlayer != null) {
					// Changement de nom + reconnection < 2 secs
					if (wait.contains(olympaPlayer.getName())) {
						event.setCancelReason(BungeeUtils.connectScreen("&eMerci de patienter secondes avant chaque reconnection."));
						event.setCancelled(true);
						return;
					}
					if (!olympaPlayer.getName().equals(name)) {
						olympaPlayer.addNewName(name);
					}
				}
				System.out.println("Premium no data" + name);
				OlympaBungee.getInstance().sendMessage("NEW JOUEUR  " + connection.getUniqueId() + " UUID CRACK: " + uuidCrack + " HAS PREMIUM ? " + uuidPremium + " HIS PREMIUM ? " + connection.isOnlineMode());
			}
		}

		// code déplacer dans VpnListener
		/*
		 * if (olympaPlayer == null) { boolean isVpn = false; try { OlympaVpn olympaVpn
		 * = VpnSql.getIpInfo(ip); if (olympaVpn == null) { isVpn =
		 * OlympaVpn.isVPN(event.getConnection()); VpnSql.setIp(ip, isVpn); } else {
		 * isVpn = olympaVpn.isVpn(); } } catch (SQLException e) { e.printStackTrace();
		 * } if (isVpn) { event.setCancelReason(BungeeUtils.
		 * connectScreen("&cImpossible d'utiliser un VPN. \n\n&e&lSi tu pense qu'il y a une erreur, contacte un membre du staff."
		 * )); event.setCancelled(true); return; } }
		 */

		if (olympaPlayer != null) {
			System.out.println("player info " + GsonCustomizedObjectTypeAdapter.GSON.toJson(olympaPlayer));
			if (olympaPlayer.getPremiumUniqueId() == null && !connection.isOnlineMode()) {
				if (!name.equals(olympaPlayer.getName())) {
					event.setCancelReason(BungeeUtils.connectScreen("&aTu as mal écrit ton pseudo, connecte toi avec &2" + olympaPlayer.getName() + "&a.\n&eLa tu utilise le pseudo " + name + "."));
					event.setCancelled(true);
					return;
				}
				// event.setCancelled(true);
				// event.setCancelReason(BungeeUtils.connectScreen("&7[&cSécuriter&7] &eLes
				// cracks ne sont pas encore autoriser. (compte déjà crée)"));
				connection.setOnlineMode(false);
			} else {
				connection.setOnlineMode(true);
			}
			cachePlayer.put(name, olympaPlayer);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void on2Login(LoginEvent event) {
		if (event.isCancelled()) {
			return;
		}
		PendingConnection connection = event.getConnection();
		System.out.println("LoginEvent onlinemode ? " + connection.isOnlineMode() + " " + connection.getName());
		String name = connection.getName();
		UUID uuidPremium = cachePremiumUUID.getIfPresent(name);
		String ip = connection.getAddress().getAddress().getHostAddress();
		OlympaPlayer olympaPlayer = cachePlayer.getIfPresent(name);
		AccountProvider olympaAccount;
		if (olympaPlayer != null) {
			cachePlayer.invalidate(name);
			olympaAccount = new AccountProvider(olympaPlayer.getUniqueId());
			cacheServer.put(name, event.getConnection().getVirtualHost().getHostName().split("\\.")[0]);
		} else {
			try {
				UUID uuidCrack = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name.toLowerCase()).getBytes("UTF-8"));
				olympaAccount = new AccountProvider(uuidCrack);
				olympaPlayer = olympaAccount.createOlympaPlayer(name, ip);
				BungeeNewPlayerEvent newPlayerEvent = ProxyServer.getInstance().getPluginManager().callEvent(new BungeeNewPlayerEvent(connection, olympaPlayer));
				if (newPlayerEvent.isCancelled()) {
					cachePremiumUUID.invalidate(name);
					return;
				}
				if (uuidPremium != null) {
					olympaPlayer.setPremiumUniqueId(uuidPremium);
				}
				cachePremiumUUID.invalidate(name);
				olympaPlayer = olympaAccount.createNew(olympaPlayer);
			} catch (SQLException | UnsupportedEncodingException e) {
				e.printStackTrace();
				event.setCancelReason(BungeeUtils.connectScreen("&cUne erreur est survenue. \n\n&e&lMerci de la signaler au staff.\n&eCode d'erreur: &l#UTF8BungeeCantCreateNew"));
				event.setCancelled(true);
				return;
			}
			// OlympaBungee.getInstance().sendMessage("Nouveau joueur: " +
			// olympaPlayer.getId() + " " + olympaPlayer.getName());
		}
		try {
			Class<?> initialHandlerClass = connection.getClass();
			Field uniqueIdField = initialHandlerClass.getDeclaredField("uniqueId");
			uniqueIdField.setAccessible(true);
			uniqueIdField.set(connection, olympaPlayer.getUniqueId());
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
			event.setCancelReason(BungeeUtils.connectScreen("&cUne erreur est survenue. \n\n&e&lMerci de la signaler au staff.\n&eCode d'erreur: &l#BungeeReflection"));
			event.setCancelled(true);
			return;
		}
		olympaPlayer.setConnected(true);
		olympaAccount.saveToCache(olympaPlayer);
		olympaAccount.saveToRedis(olympaPlayer);
		olympaAccount.accountPersist();
	}

	@EventHandler(priority = EventPriority.LOW)
	public void on3PostLogin(PostLoginEvent event) {
		ProxiedPlayer player = event.getPlayer();
		System.out.println("PostLoginEvent onlinemode ? " + player.getPendingConnection().isOnlineMode());
		if (!player.getPendingConnection().isOnlineMode()) {
			return;
		}
		OlympaPlayer olympaPlayer = AccountProvider.get(player.getUniqueId());
		OlympaPlayerLoginEvent olympaPlayerLoginEvent = ProxyServer.getInstance().getPluginManager().callEvent(new OlympaPlayerLoginEvent(olympaPlayer, player));
		olympaPlayerLoginEvent.cancelIfNeeded();
	}

	@EventHandler
	public void on4Switch(ServerSwitchEvent event) {
		ProxiedPlayer player = event.getPlayer();
		cachePlayer.invalidate(player.getName());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void on5Disconnect(PlayerDisconnectEvent event) {
		ProxiedPlayer player = event.getPlayer();
		cacheServer.invalidate(player.getName());
		cachePlayer.invalidate(player.getName());
		AccountProvider olympaAccount = new AccountProvider(player.getUniqueId());
		olympaAccount.removeFromCache();
		player.removeGroups(player.getGroups().toArray(new String[0]));
		wait.add(player.getName());
		ProxyServer.getInstance().getScheduler().schedule(OlympaBungee.getInstance(), () -> {
			wait.remove(player.getName());
			OlympaPlayer olympaPlayer = olympaAccount.getFromRedis();
			if (olympaPlayer == null) {
				System.out.println("ATTENTION le joueur " + player.getUniqueId() + " n'avait pas de donnés dans redis.");
				return;
			}
			olympaPlayer.setLastConnection(Utils.getCurrentTimeInSeconds());
			olympaPlayer.setConnected(false);
			olympaAccount.accountExpire();
			olympaAccount.saveToDb(olympaPlayer);
		}, 3, TimeUnit.SECONDS);
	}

}
