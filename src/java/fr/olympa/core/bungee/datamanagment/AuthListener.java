package fr.olympa.core.bungee.datamanagment;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import fr.olympa.api.player.OlympaPlayer;
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
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

@SuppressWarnings("deprecation")
public class AuthListener implements Listener {

	// public static Cache<String, String> cacheServer =
	// CacheBuilder.newBuilder().expireAfterWrite(60, TimeUnit.SECONDS).build();
	// private Cache<String, UUID> cachePremiumUUID =
	// CacheBuilder.newBuilder().expireAfterWrite(60, TimeUnit.SECONDS).build();
	// private Cache<String, OlympaPlayer> cachePlayer =
	// CacheBuilder.newBuilder().expireAfterWrite(60, TimeUnit.SECONDS).build();
	private Set<String> wait = new HashSet<>();

	@EventHandler
	public void on1PreLogin(PreLoginEvent event) {
		if (event.isCancelled()) {
			return;
		}
		PendingConnection connection = event.getConnection();
		String name = connection.getName();
		CachePlayer cache = new CachePlayer(name);
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
		// Si le joueur ne s'est jamais connecté
		if (olympaPlayer == null) {
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
				// Si la connection est crack
				if (!connection.isOnlineMode()) {
					event.setCancelReason(BungeeUtils.connectScreen("&cLe pseudo &4" + response.getName() + "&c est un compte premium.\n&c&nTu ne peux pas l'utiliser."));
					event.setCancelled(true);
					return;
				}
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
						event.setCancelReason(BungeeUtils.connectScreen("&eMerci de patienter secondes avant chaque reconnection."));
						event.setCancelled(true);
						return;
					}
				}
				OlympaBungee.getInstance().sendMessage("&cNouveau Joueur  " + name + " " + connection.getUniqueId() + " UUID CRACK: " + uuidCrack + " HAS PREMIUM ? " + uuidPremium + " HIS PREMIUM ? " + connection.isOnlineMode());

			} else {
				// Si le pseudo n'est pas un compte premium

				// Si la connection est premium
				/*if (connection.isOnlineMode()) {
					event.setCancelReason(BungeeUtils.connectScreen("&cTu ne peux pas te faire passer pour un compte premium."));
					event.setCancelled(true);
					return;
				}*/
				event.setCancelReason(BungeeUtils.connectScreen("&eLes cracks ne sont pas encore autoriser."));
				event.setCancelled(true);
				System.out.println("Crack with no data " + name);
				return;
			}
		}

		// Si le joueur s'est déjà connecté
		if (olympaPlayer != null) {
			cache.setOlympaPlayer(olympaPlayer);
			System.out.println("player info " + GsonCustomizedObjectTypeAdapter.GSON.toJson(cache.getOlympaPlayer()));
			if (olympaPlayer.getPremiumUniqueId() == null) {
				/*if (connection.isOnlineMode()) {
					event.setCancelReason(BungeeUtils.connectScreen(
							"&cCe pseudo &4" + olympaPlayer.getName() + "&c est utilisé par un compte crack.\n&bIl est préférable de changer de pseudo, toutefois il est possible de faire une demande au Staff."));
					event.setCancelled(true);
					return;
				}*/
				if (!name.equals(olympaPlayer.getName())) {
					event.setCancelReason(BungeeUtils.connectScreen("&aTu as mal écrit ton pseudo, connecte toi avec &2" + olympaPlayer.getName() + "&a.\n&eLa tu utilise le pseudo " + name + "."));
					event.setCancelled(true);
					return;
				}
				connection.setOnlineMode(false);
			} else {
				connection.setOnlineMode(true);
			}
		}
		cache.setSubDomain(event.getConnection());
		DataHandler.addPlayer(cache);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void on2PreLogin(PreLoginEvent event) {
		PendingConnection connection = event.getConnection();
		String name = connection.getName();
		if (event.isCancelled()) {
			DataHandler.removePlayer(name);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void on3Login(LoginEvent event) {
		PendingConnection connection = event.getConnection();
		String name = connection.getName();
		CachePlayer cache = DataHandler.get(name);
		if (cache == null) {
			// à ajouter à la liste des erreurs
			event.setCancelReason(BungeeUtils.connectScreen("&cUne erreur est survenue. \n\n&e&lMerci de la signaler au staff.\n&eCode d'erreur: &l#BungeeNoCache"));
			event.setCancelled(true);
			return;
		}
		OlympaPlayer olympaPlayer = cache.getOlympaPlayer();
		System.out.println("LoginEvent onlinemode ? " + connection.isOnlineMode() + " " + name + " p: " + GsonCustomizedObjectTypeAdapter.GSON.toJson(olympaPlayer));
		String ip = connection.getAddress().getAddress().getHostAddress();
		AccountProvider olympaAccount;
		// Si le joueur s'est déjà connecté
		if (olympaPlayer != null) {
			if (!olympaPlayer.getName().equals(name)) {
				olympaPlayer.addNewName(name);
			}
			olympaAccount = new AccountProvider(olympaPlayer.getUniqueId());
		} else {
			// Si le joueur ne s'est jamais connecté
			try {
				UUID uuidCrack = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name.toLowerCase()).getBytes("UTF-8"));
				olympaAccount = new AccountProvider(uuidCrack);
				olympaPlayer = olympaAccount.createOlympaPlayer(name, ip);
				BungeeNewPlayerEvent newPlayerEvent = ProxyServer.getInstance().getPluginManager().callEvent(new BungeeNewPlayerEvent(connection, olympaPlayer));
				if (newPlayerEvent.isCancelled()) {
					event.setCancelReason(newPlayerEvent.getCancelReason());
					event.setCancelled(true);
					return;
				}
				UUID uuidPremium = cache.getPremiumUUID();
				if (uuidPremium != null) {
					olympaPlayer.setPremiumUniqueId(uuidPremium);
				}
				olympaPlayer = olympaAccount.createNew(olympaPlayer);
				cache.setOlympaPlayer(olympaPlayer);
			} catch (SQLException | UnsupportedEncodingException e) {
				e.printStackTrace();
				event.setCancelReason(BungeeUtils.connectScreen("&cUne erreur est survenue. \n\n&e&lMerci de la signaler au staff.\n&eCode d'erreur: &l#UTF8BungeeCantCreateNew"));
				event.setCancelled(true);
				DataHandler.removePlayer(name);
				return;
			}
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

	@EventHandler(priority = EventPriority.HIGHEST)
	public void on42Login(LoginEvent event) {
		PendingConnection connection = event.getConnection();
		String name = connection.getName();
		if (event.isCancelled()) {
			DataHandler.removePlayer(name);
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void on5PostLogin(PostLoginEvent event) {
		ProxiedPlayer player = event.getPlayer();
		System.out.println("PostLoginEvent onlinemode ? " + player.getPendingConnection().isOnlineMode());
		if (!player.getPendingConnection().isOnlineMode()) {
			return;
		}
		OlympaPlayer olympaPlayer = DataHandler.get(player.getName()).getOlympaPlayer();
		OlympaPlayerLoginEvent olympaPlayerLoginEvent = ProxyServer.getInstance().getPluginManager().callEvent(new OlympaPlayerLoginEvent(olympaPlayer, player));
		olympaPlayerLoginEvent.cancelIfNeeded();
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void on6Disconnect(PlayerDisconnectEvent event) {
		ProxiedPlayer player = event.getPlayer();
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
			olympaAccount.accountExpire();
			olympaAccount.saveToDb(olympaPlayer);
		}, 2, TimeUnit.SECONDS);
	}

}
