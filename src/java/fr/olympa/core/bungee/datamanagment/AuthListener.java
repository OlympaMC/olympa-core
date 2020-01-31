package fr.olympa.core.bungee.datamanagment;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.Gson;

import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.provider.BungeeNewPlayerEvent;
import fr.olympa.api.sql.MySQL;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.login.events.OlympaPlayerLoginEvent;
import fr.olympa.core.bungee.servers.ServersConnection;
import fr.olympa.core.bungee.utils.BungeeUtils;
import fr.olympa.core.bungee.vpn.OlympaVpn;
import fr.olympa.core.bungee.vpn.VpnSql;
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

	private Cache<String, UUID> cachePremiumUUID = CacheBuilder.newBuilder().expireAfterWrite(4, TimeUnit.SECONDS).build();
	private Cache<String, OlympaPlayer> cachePlayer = CacheBuilder.newBuilder().expireAfterWrite(4, TimeUnit.SECONDS).build();
	private Set<String> wait = new HashSet<>();

	@EventHandler(priority = EventPriority.LOW)
	public void on1PreLogin(PreLoginEvent event) {
		if (event.isCancelled()) {
			return;
		}
		PendingConnection connection = event.getConnection();
		String name = connection.getName();
		String ip = connection.getAddress().getAddress().getHostAddress();
		OlympaPlayer olympaPlayer;
		UUID uuidCrack = null;

		if (this.wait.contains(name)) {
			event.setCancelReason(BungeeUtils.connectScreen("&eMerci de patienter 2 secondes avant chaque reconnection."));
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
			UUID uuidPremium = AuthUtils.getUuid(connection);
			try {
				uuidCrack = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name.toLowerCase()).getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			if (uuidPremium == null) {
				event.setCancelReason(BungeeUtils.connectScreen("&7[&cSécuriter&7] &eLes cracks ne sont pas encore autoriser."));
				event.setCancelled(true);
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
					event.setCancelReason(BungeeUtils.connectScreen("&cUne erreur est survenue. \n\n&e&lMerci de la signaler au staff.\n&eCode d'erreur: &l#SQLBungeePreLogin"));
					event.setCancelled(true);
					return;
				}
				if (olympaPlayer != null) {
					// Changement de nom + reconnection < 2 secs
					if (this.wait.contains(olympaPlayer.getName())) {
						event.setCancelReason(BungeeUtils.connectScreen("&eMerci de patienter 2 secondes avant chaque reconnection."));
						event.setCancelled(true);
						return;
					}
					olympaPlayer.addNewName(name);
				}
				System.out.println("null onlinemode");
			}
			OlympaBungee.getInstance().sendMessage("NEW JOUEUR  " + connection.getUniqueId() + " UUID CRACK: " + uuidCrack + " HAS PREMIUM ? " + uuidPremium + " HIS PREMIUM ? " + connection.isOnlineMode());
		}

		if (olympaPlayer == null) {
			boolean isVpn = false;
			try {
				OlympaVpn olympaVpn = VpnSql.getIpInfo(ip);
				if (olympaVpn == null) {
					isVpn = OlympaVpn.isVPN(event.getConnection());
					VpnSql.setIp(ip, isVpn);
				} else {
					isVpn = olympaVpn.isVpn();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			if (isVpn) {
				event.setCancelReason(BungeeUtils.connectScreen("&cImpossible d'utiliser un VPN. \n\n&e&lSi tu pense qu'il y a une erreur, contacte un membre du staff."));
				event.setCancelled(true);
				return;
			}
		}

		if (olympaPlayer != null) {
			System.out.println("player info " + new Gson().toJson(olympaPlayer));
			if (olympaPlayer.getPremiumUniqueId() == null) {
				if (!name.equals(olympaPlayer.getName())) {
					event.setCancelReason(BungeeUtils.connectScreen("&aTu as mal écrit ton pseudo, connecte toi avec &2" + olympaPlayer.getName() + "&a.\n&eLa tu utilise le pseudo " + name + "."));
					event.setCancelled(true);
					return;
				}
				//event.setCancelled(true);
				//event.setCancelReason(BungeeUtils.connectScreen("&7[&cSécuriter&7] &eLes cracks ne sont pas encore autoriser. (compte déjà crée)"));
				connection.setOnlineMode(false);
			} else {
				connection.setOnlineMode(true);
			}
			this.cachePlayer.put(name, olympaPlayer);
			System.out.println("name" + name);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void on2Login(LoginEvent event) {
		if (event.isCancelled()) {
			return;
		}
		PendingConnection connection = event.getConnection();
		System.out.println("LoginEvent onlinemode ? " + connection.isOnlineMode());
		String name = connection.getName();
		System.out.println("name2 " + name);
		UUID uuidPremium = this.cachePremiumUUID.getIfPresent(name);
		String ip = connection.getAddress().getAddress().getHostAddress();
		OlympaPlayer olympaPlayer = this.cachePlayer.getIfPresent(name);
		AccountProvider olympaAccount;
		if (olympaPlayer != null) {
			this.cachePlayer.invalidate(name);
			olympaAccount = new AccountProvider(olympaPlayer.getUniqueId());
		} else {
			try {
				UUID uuidCrack = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name.toLowerCase()).getBytes("UTF-8"));
				olympaAccount = new AccountProvider(uuidCrack);
				olympaPlayer = olympaAccount.createOlympaPlayer(name, ip);
				BungeeNewPlayerEvent newPlayerEvent = ProxyServer.getInstance().getPluginManager().callEvent(new BungeeNewPlayerEvent(connection, olympaPlayer));
				if (newPlayerEvent.isCancelled()) {
					this.cachePremiumUUID.invalidate(name);
					return;
				}
				if (uuidPremium != null) {
					olympaPlayer.setPremiumUniqueId(uuidPremium);
				}
				this.cachePremiumUUID.invalidate(name);
				olympaAccount.createNew(olympaPlayer);
				olympaPlayer = MySQL.getPlayer(olympaPlayer.getUniqueId());
			} catch (SQLException e) {
				e.printStackTrace();
				event.setCancelReason(BungeeUtils.connectScreen("&cUne erreur est survenue. \n\n&e&lMerci de la signaler au staff.\n&eCode d'erreur: &l#SQLBungeeCantCreateNew"));
				event.setCancelled(true);
				return;
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				event.setCancelReason(BungeeUtils.connectScreen("&cUne erreur est survenue. \n\n&e&lMerci de la signaler au staff.\n&eCode d'erreur: &l#UTF8BungeeCantCreateNew"));
				event.setCancelled(true);
				return;
			}
			OlympaBungee.getInstance().sendMessage("Nouveau joueur: " + olympaPlayer.getId() + " " + olympaPlayer.getName());
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
		olympaAccount.saveToCache(olympaPlayer);
		olympaAccount.saveToRedis(olympaPlayer);
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
		this.cachePlayer.invalidate(player.getName());
		if (ServersConnection.isAuth(player)) {
			player.removeGroups(player.getGroups().toArray(new String[0]));
			return;
		}
		if (player.getGroups().isEmpty()) {
			OlympaPlayer olympaPlayer = new AccountProvider(player.getUniqueId()).getFromRedis();
			if (olympaPlayer != null) {
				player.addGroups(olympaPlayer.getGroups().keySet().stream().map(gr -> gr.getName()).collect(Collectors.toList()).toArray(new String[0]));
			}
		}

	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void on5Disconnect(PlayerDisconnectEvent event) {
		ProxiedPlayer player = event.getPlayer();
		this.cachePlayer.invalidate(player.getName());
		AccountProvider olympaAccount = new AccountProvider(player.getUniqueId());
		olympaAccount.removeFromCache();
		player.removeGroups(player.getGroups().toArray(new String[0]));
		this.wait.add(player.getName());
		ProxyServer.getInstance().getScheduler().schedule(OlympaBungee.getInstance(), () -> {
			this.wait.remove(player.getName());
			olympaAccount.accountExpire();
			OlympaPlayer olympaPlayer = olympaAccount.getFromRedis();
			if (olympaPlayer == null) {
				System.out.println("ATTENTION le joueur " + player.getUniqueId() + " n'avait pas de donnés dans redis.");
				return;
			}
			olympaPlayer.setLastConnection(Utils.getCurrentTimeInSeconds());
			olympaAccount.saveToDb(olympaPlayer);
		}, 2, TimeUnit.SECONDS);
	}

}
