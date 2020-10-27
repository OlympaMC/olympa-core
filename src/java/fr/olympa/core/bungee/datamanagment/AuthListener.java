package fr.olympa.core.bungee.datamanagment;

import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import fr.olympa.api.bungee.mojangapi.MojangAPI;
import fr.olympa.api.bungee.mojangapi.objects.UuidResponse;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.provider.BungeeNewPlayerEvent;
import fr.olympa.api.sql.MySQL;
import fr.olympa.api.utils.GsonCustomizedObjectTypeAdapter;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.antibot.AntiBotHandler;
import fr.olympa.core.bungee.connectionqueue.QueueHandler;
import fr.olympa.core.bungee.login.events.OlympaPlayerLoginEvent;
import fr.olympa.core.bungee.security.SecurityHandler;
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

	public static Set<String> wait = new HashSet<>();

	@EventHandler
	public void on1PreLogin(PreLoginEvent event) {
		if (event.isCancelled())
			return;
		PendingConnection connection = event.getConnection();
		String name = connection.getName();
		if (wait.contains(name))
			try {
				Thread.sleep(2 * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		CachePlayer oldCache = DataHandler.get(name);
		if (oldCache != null)
			DataHandler.removePlayer(oldCache);
		CachePlayer cache = new CachePlayer(name);
		OlympaPlayer olympaPlayer;
		try {
			olympaPlayer = AccountProvider.get(name);
		} catch (Exception e) {
			e.printStackTrace();
			event.setCancelReason(BungeeUtils.connectScreen("&cUne erreur est survenue. \n\n&e&lMerci de la signaler au staff.\n&eCode d'erreur: &l#SQLBungeeLost"));
			event.setCancelled(true);
			return;
		}
		// Si le joueur ne s'est jamais connecté
		if (olympaPlayer == null) {
			if (QueueHandler.getQueueSize() > 10 || AntiBotHandler.isEnable()) {
				event.setCancelReason(BungeeUtils.connectScreen("&4AntiBot Activé &c> Tu dois t'inscrire sur le site pour te connecter\nwww.olympa.fr"));
				event.setCancelled(true);
				return;
			}

			UuidResponse response;
			try {
				// On regarde si le pseudo est utilisé par un compte premium
				response = MojangAPI.getFromName(connection);
			} catch (IOException e) {
				e.printStackTrace();
				event.setCancelReason(BungeeUtils.connectScreen("&cUne erreur est survenu avec les serveurs d'authentifications de Mojang.\n&eCode d'erreur: &l#BungeeMojangNewPlayer"));
				event.setCancelled(true);
				return;
			}
			UUID uuidPremium = null;

			// Si le pseudo est un compte premium
			if (response != null) {
				connection.setOnlineMode(true);
				// Si la connection est crack
				//				if (!connection.isOnlineMode()) {
				//					event.setCancelReason(BungeeUtils.connectScreen("&cLe pseudo &4" + response.getName() + "&c est un compte premium.\n&c&nTu ne peux pas l'utiliser."));
				//					event.setCancelled(true);
				//					return;
				//				}
				uuidPremium = response.getUuid();
				// cachePremiumUUID.put(name, uuidPremium);
				cache.setPremiumUUID(uuidPremium);

				// Vérifie si le joueur n'a pas changé de nom.
				try {
					olympaPlayer = MySQL.getPlayerByPremiumUuid(uuidPremium);
				} catch (SQLException e) {
					e.printStackTrace();
					event.setCancelReason(BungeeUtils.connectScreen("&cUne erreur est survenue. \n\n&e&lMerci de la signaler au staff.\n&eCode d'erreur: &l#SQLBungeePreLogin"));
					event.setCancelled(true);
					return;
				}
				// Si le joueur a changé de nom
				if (olympaPlayer != null) {
					// Changement de nom + reconnection < 2 secs
					if (wait.contains(olympaPlayer.getName())) {
						event.setCancelReason(BungeeUtils.connectScreen("&eMerci de patienter 2 secondes avant chaque reconnection."));
						event.setCancelled(true);
						return;
					}
					OlympaBungee.getInstance().sendMessage("&cChangement de pseudo  " + name + " HAS PREMIUM ? " + uuidPremium);
				} else
					OlympaBungee.getInstance().sendMessage("&cNouveau Joueur  " + name + " HAS PREMIUM ? " + uuidPremium);
			} else {
				connection.setOnlineMode(false);
				OlympaBungee.getInstance().sendMessage("§7Joueur crack sans données §e" + name);
			}
			// Si la connection est premium
			/*if (connection.isOnlineMode()) {
				event.setCancelReason(BungeeUtils.connectScreen("&cTu ne peux pas te faire passer pour un compte premium."));
				event.setCancelled(true);
				return;
			}*/
			//				event.setCancelReason(BungeeUtils.connectScreen("&eLes cracks ne sont pas encore autoriser."));
			//				event.setCancelled(true);
		}

		// Si le joueur s'est déjà connecté
		if (olympaPlayer != null) {
			cache.setOlympaPlayer(olympaPlayer);
			OlympaBungee.getInstance().sendMessage("§7Connexion du joueur connu §e" + olympaPlayer.getName());
			if (olympaPlayer.getPremiumUniqueId() == null) {
				connection.setOnlineMode(false);
				/*if (connection.isOnlineMode()) {
					event.setCancelReason(BungeeUtils.connectScreen(
							"&cCe pseudo &4" + olympaPlayer.getName() + "&c est utilisé par un compte crack.\n&bIl est préférable de changer de pseudo, toutefois il est possible de faire une demande au Staff."));
					event.setCancelled(true);
					return;
				}*/
				if (!name.equals(olympaPlayer.getName())) {
					event.setCancelReason(BungeeUtils.connectScreen("&aTu as mal écrit ton pseudo, connecte toi avec &2" + olympaPlayer.getName() + "&a.\n&eLà tu utilise le pseudo " + name + "."));
					event.setCancelled(true);
					return;
				}
			} else
				connection.setOnlineMode(true);
		}
		if (!connection.isOnlineMode() && !SecurityHandler.ALLOW_CRACK) {
			event.setCancelReason(BungeeUtils.connectScreen("&cLes versions Crack sont temporairement désactivées. Désolé du dérangement."));
			event.setCancelled(true);
			return;
		} else if (connection.isOnlineMode() && !SecurityHandler.ALLOW_PREMIUM) {
			event.setCancelReason(BungeeUtils.connectScreen("&cLes versions Premium sont temporairement désactivées. Désolé du dérangement."));
			event.setCancelled(true);
			return;
		}
		cache.setSubDomain(event.getConnection());
		DataHandler.addPlayer(cache);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void on2PreLogin(PreLoginEvent event) {
		PendingConnection connection = event.getConnection();
		String name = connection.getName();
		if (event.isCancelled())
			DataHandler.removePlayer(name);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void on3Login(LoginEvent event) {
		PendingConnection connection = event.getConnection();
		String name = connection.getName();
		if (event.isCancelled())
			DataHandler.removePlayer(name);
		CachePlayer cache = DataHandler.get(name);
		if (cache == null) {
			// à ajouter à la liste des erreurs
			event.setCancelReason(BungeeUtils.connectScreen("&cUne erreur est survenue. \n\n&e&lMerci de la signaler au staff.\n&eCode d'erreur: &l#BungeeNoCache"));
			event.setCancelled(true);
			return;
		}
		OlympaPlayer olympaPlayer = cache.getOlympaPlayer();
		OlympaBungee.getInstance().sendMessage("§7LoginEvent §6" + (connection.isOnlineMode() ? "online" : "offline/cracked") + "§7 du joueur §e" + name + "§7. Json: §f" + GsonCustomizedObjectTypeAdapter.GSON.toJson(olympaPlayer));
		String ip = connection.getAddress().getAddress().getHostAddress();
		AccountProvider olympaAccount;
		// Si le joueur s'est déjà connecté
		if (olympaPlayer != null) {
			if (!olympaPlayer.getName().equals(name))
				olympaPlayer.addNewName(name);
			olympaAccount = new AccountProvider(olympaPlayer.getUniqueId());
		} else
			// Si le joueur ne s'est jamais connecté
			try {
				UUID uuidCrack = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name.toLowerCase()).getBytes("UTF-8"));

				// Vérifie que l'uuid n'est pas déjà utiliser
				OlympaPlayer uuidAlreadyExist = MySQL.getPlayer(uuidCrack);
				if (uuidAlreadyExist != null)
					do {
						uuidCrack = UUID.randomUUID();
						uuidAlreadyExist = MySQL.getPlayer(uuidCrack);
					} while (uuidAlreadyExist != null);

				olympaAccount = new AccountProvider(uuidCrack);
				olympaPlayer = olympaAccount.createOlympaPlayer(name, ip);
				BungeeNewPlayerEvent newPlayerEvent = ProxyServer.getInstance().getPluginManager().callEvent(new BungeeNewPlayerEvent(connection, olympaPlayer));
				if (newPlayerEvent.isCancelled()) {
					event.setCancelReason(newPlayerEvent.getCancelReason());
					event.setCancelled(true);
					return;
				}
				UUID uuidPremium = cache.getPremiumUUID();
				if (uuidPremium != null)
					olympaPlayer.setPremiumUniqueId(uuidPremium);
				olympaPlayer = olympaAccount.createNew(olympaPlayer);
				cache.setOlympaPlayer(olympaPlayer);
			} catch (Exception e) {
				e.printStackTrace();
				event.setCancelReason(BungeeUtils.connectScreen("&cUne erreur est survenue. \n\n&e&lMerci de la signaler au staff.\n&eCode d'erreur: &l#UTF8BungeeCantCreateNew"));
				event.setCancelled(true);
				DataHandler.removePlayer(name);
				return;
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
			DataHandler.removePlayer(name);
			return;
		}
		olympaPlayer.setConnected(true);
		olympaAccount.saveToRedis(olympaPlayer);
		olympaAccount.accountPersist();
	}

	@EventHandler(priority = EventPriority.LOW)
	public void on5PostLogin(PostLoginEvent event) {
		ProxiedPlayer player = event.getPlayer();
		OlympaBungee.getInstance().sendMessage("§7PostLoginEvent §6" + (player.getPendingConnection().isOnlineMode() ? "online" : "offline/cracked"));
		CachePlayer cache = DataHandler.get(player.getName());
		OlympaPlayer olympaPlayer;
		if (cache == null || (olympaPlayer = cache.getOlympaPlayer()) == null || !olympaPlayer.isPremium())
			return;
		OlympaPlayerLoginEvent olympaPlayerLoginEvent = ProxyServer.getInstance().getPluginManager().callEvent(new OlympaPlayerLoginEvent(olympaPlayer, player));
		olympaPlayerLoginEvent.cancelIfNeeded();
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void on6Disconnect(PlayerDisconnectEvent event) {
		ProxiedPlayer player = event.getPlayer();
		OlympaBungee.getInstance().sendMessage("§7Déconnexion du joueur §e" + player.getName() + (event.getPlayer().getServer() == null ? "" : " §7(serveur §6" + event.getPlayer().getServer().getInfo().getName() + "§7)"));
		AccountProvider olympaAccount = new AccountProvider(player.getUniqueId());
		olympaAccount.removeFromCache();
		wait.add(player.getName());
		DataHandler.removePlayer(player.getName());
		ProxyServer.getInstance().getScheduler().schedule(OlympaBungee.getInstance(), () -> {
			wait.remove(player.getName());
			OlympaPlayer olympaPlayer = olympaAccount.getFromRedis();
			if (olympaPlayer == null) {
				System.out.println("ATTENTION le joueur " + player.getUniqueId() + " n'avait pas de donnés dans redis.");
				return;
			}
			olympaPlayer.setLastConnection(Utils.getCurrentTimeInSeconds());
			olympaPlayer.setConnected(false);
			olympaAccount.removeFromRedis();
			olympaAccount.saveToDb(olympaPlayer);
		}, 4, TimeUnit.SECONDS);
	}

}
