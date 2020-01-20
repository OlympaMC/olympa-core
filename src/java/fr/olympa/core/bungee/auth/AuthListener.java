package fr.olympa.core.bungee.auth;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.UUID;

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
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

@SuppressWarnings("deprecation")
public class AuthListener implements Listener {

	@EventHandler(priority = EventPriority.LOW)
	public void on1PreLogin(PreLoginEvent event) {
		if (event.isCancelled()) {
			return;
		}
		PendingConnection connection = event.getConnection();
		String name = connection.getName();
		System.out.println("PreLoginEvent onlinemode ? " + connection.isOnlineMode());
		String ip = connection.getAddress().getAddress().getHostAddress();
		OlympaPlayer olympaPlayer;
		try {
			//TODO
			olympaPlayer = MySQL.getPlayerByName(name);
		} catch (SQLException e) {
			e.printStackTrace();
			event.setCancelled(true);
			event.setCancelReason(BungeeUtils.connectScreen("&cUne erreur est survenue. \n\n&e&lMerci de la signaler au staff.\n&eCode d'erreur: &l#SQLBungeePreLogin"));
			return;
		}
		if (olympaPlayer == null) {
			UUID uuid = AuthUtils.getUuid(connection);
			if (uuid != null) {
				System.out.println("null onlinemode");
				connection.setOnlineMode(true);
				// TODO SET UUID SERVER TO PREMIUM
				/*try {
					connection.setUniqueId(UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes("UTF-8")));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}*/
			} else {
				event.setCancelled(true);
				event.setCancelReason(BungeeUtils.connectScreen("&7[&cSécuriter&7] &eLes cracks ne sont pas encore autoriser."));
				System.out.println("null offlinemode");
				return;
				// connection.setOnlineMode(false);
			}
		} else if (!olympaPlayer.getName().equals(name)) {
			event.setCancelled(true);
			event.setCancelReason(BungeeUtils.connectScreen("&cTon pseudo n'est pas bien correctement écrit.\n Tu dois utiliser ce pseudo &2"
					+ olympaPlayer.getName() + "&c.\n\n&cVa sur le site &4&nolympa.fr&c si tu souhaite changer de pseudo"));
			return;
		} else {
			if (olympaPlayer.getPremiumUniqueId() != null) {
				System.out.println("olympaPlayer onlinemode");
				connection.setOnlineMode(true);
				// connection.setUniqueId(olympaPlayer.getUniqueId());
			} else {
				System.out.println("olympaPlayer offlinemode");
				connection.setOnlineMode(false);
				connection.setUniqueId(olympaPlayer.getUniqueId());
			}
			new AccountProvider(olympaPlayer.getUniqueId()).saveToCache(olympaPlayer);
		}
		System.out.println("PreLoginEvent2 onlinemode ? " + connection.isOnlineMode());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void on2Login(LoginEvent event) {
		PendingConnection connection = event.getConnection();
		UUID uuid = connection.getUniqueId();
		String name = connection.getName();
		String ip = connection.getAddress().getAddress().getHostAddress();
		AccountProvider olympaAccount = new AccountProvider(uuid);
		OlympaPlayer olympaPlayer = null;
		System.out.println("LoginEvent onlinemode ? " + connection.isOnlineMode());

		try {
			olympaPlayer = olympaAccount.get();
			if (olympaPlayer == null) {
				olympaPlayer = olympaAccount.createOlympaPlayer(name, ip);
				//GET IT
				String uuidPremium = null;
				if (uuidPremium != null && !uuidPremium.isEmpty()) {
					olympaPlayer.setPremiumUniqueId(UUID.fromString(uuidPremium));
				}
				if (!olympaAccount.createNew(olympaPlayer)) {
					event.setCancelled(true);
					event.setCancelReason(BungeeUtils.connectScreen("&cUne erreur est survenue. \n\n&e&lMerci de la signaler au staff.\n&eCode d'erreur: &l#SQLBungeeCantCreateNew"));
					return;
				}
				OlympaBungee.getInstance().sendMessage("Nouveau joueur: " + olympaPlayer.getName());
			}
		} catch (SQLException e) {
			event.setCancelled(true);
			event.setCancelReason(BungeeUtils.connectScreen("&cUne erreur est survenue. \n\n&e&lMerci de la signaler au staff.\n&eCode d'erreur: &l#SQLBungeeLogin"));
			e.printStackTrace();
		}

		if (connection.isOnlineMode()) {
			try {
				Class<?> initialHandlerClass = connection.getClass();
				Field uniqueIdField = initialHandlerClass.getDeclaredField("uniqueId");
				uniqueIdField.setAccessible(true);
				uniqueIdField.set(connection, olympaPlayer.getUniqueId());
			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
			System.out.println("NEW UUID " + connection.getUniqueId());
		}

		olympaAccount.saveToRedisBungee(olympaPlayer);
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
		OlympaPlayer olympaPlayer = olympaAccount.getFromRedis();
		if (olympaPlayer == null) {
			System.out.println("ATTENTION le joueur " + olympaPlayer + " n'avait pas de donnés dans redis.");
			return;
		}
		olympaAccount.accountExpire();
		olympaAccount.saveToDbBungee(olympaPlayer);
	}

}
