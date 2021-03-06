package fr.olympa.core.bungee.datamanagment;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import fr.olympa.api.bungee.customevent.OlympaPlayerLoginEvent;
import fr.olympa.api.bungee.mojangapi.MojangAPI;
import fr.olympa.api.bungee.mojangapi.objects.UuidResponse;
import fr.olympa.api.bungee.player.CachePlayer;
import fr.olympa.api.bungee.player.DataHandler;
import fr.olympa.api.bungee.utils.BungeeUtils;
import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.security.SecurityHandler;
import fr.olympa.core.bungee.utils.SpigotPlayerPack;
import fr.olympa.core.common.provider.AccountProvider;
import fr.olympa.core.common.provider.BungeeNewPlayerEvent;
import fr.olympa.core.common.utils.GsonCustomizedObjectTypeAdapter;
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

	private static Set<String> wait = new HashSet<>();

	public static boolean containsWait(ProxiedPlayer proxiedPlayer) {
		return wait.contains(proxiedPlayer.getName());
	}

	private static Cache<String, UuidResponse> newPlayerPremium = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).build();

	@EventHandler
	public void on1PreLogin(PreLoginEvent event) {
		if (event.isCancelled())
			return;

		PendingConnection connection = event.getConnection();
		String name = connection.getName();
		if (wait.contains(name))
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
				event.setCancelReason(BungeeUtils.connectScreen("&cUne erreur est survenue. \n\n&e&lMerci de la signaler au staff.\n&eCode d'erreur: &l#InterruptedException"));
				event.setCancelled(true);
				return;
			}
		CachePlayer cache = new CachePlayer(name);
		OlympaPlayer olympaPlayer;
		try {
			olympaPlayer = AccountProvider.getter().get(name);
		} catch (Exception | NoClassDefFoundError e) {
			e.printStackTrace();
			event.setCancelReason(BungeeUtils.connectScreen("&cUne erreur est survenue.\n\n&e&lMerci de la signaler au staff.\n&eCode d'erreur: &l#SQLBungeeLost"));
			event.setCancelled(true);
			return;
		}

		// Si le joueur ne s'est jamais connect??
		if (olympaPlayer == null) {
			if (!SecurityHandler.getInstance().getAntibot().getCase().canfirstConnectionCrack(event))
				return;

			UuidResponse response = newPlayerPremium.asMap().get(name);
			if (response == null)
				try {
					// On regarde si le pseudo est utilis?? par un compte premium
					response = MojangAPI.getFromName(connection);
				} catch (IOException e) {
					e.printStackTrace();
					event.setCancelReason(BungeeUtils.connectScreen("&cUne erreur est survenue avec les serveurs d'authentifications de Mojang.\n&eCode d'erreur: &l#BungeeMojangNewPlayer"));
					event.setCancelled(true);
					return;
				}
			else if (response.addRetry() % 2 == 0) {
				event.setCancelReason(BungeeUtils.connectScreen("&eSi tu utilise un compte crack, change de pseudo, celui-ci est d??j?? utilis??.\n\n&eSi tu utilise un compte premium, r??esaye de te connecter ou/et relancer Minecraft."));
				event.setCancelled(true);
			}
			UUID uuidPremium = null;

			// Si le pseudo est un compte premium
			if (response != null) {
				connection.setOnlineMode(true);
				// Si la connection est crack
				/*if (!connection.isOnlineMode()) {
					event.setCancelReason(BungeeUtils.connectScreen("&cLe pseudo &4" + response.getName() + "&c est un compte premium.\n&c&nTu ne peux pas l'utiliser."));
					event.setCancelled(true);
					return;
				}*/
				uuidPremium = response.getUuid();
				cache.setPremiumUUID(uuidPremium);

				// V??rifie si le joueur n'a pas chang?? de nom.
				try {
					olympaPlayer = AccountProvider.getter().getSQL().getPlayerByPremiumUuid(uuidPremium);
				} catch (SQLException e) {
					e.printStackTrace();
					event.setCancelReason(BungeeUtils.connectScreen("&cUne erreur est survenue. \n\n&e&lMerci de la signaler au staff.\n&eCode d'erreur: &l#SQLBungeePreLogin"));
					event.setCancelled(true);
					return;
				}
				// Si le joueur a chang?? de nom
				if (olympaPlayer != null) {
					// Changement de nom + reconnection < 2 secs
					if (wait.contains(olympaPlayer.getName())) {
						event.setCancelReason(BungeeUtils.connectScreen("&eMerci de patienter 2 secondes avant chaque reconnection."));
						event.setCancelled(true);
						return;
					}
					OlympaBungee.getInstance().sendMessage("??cChangement de pseudo de %s (anciennement %s), UUID: ", name, olympaPlayer.getName(), uuidPremium);
				} else {
					newPlayerPremium.put(name, response);
					OlympaBungee.getInstance().sendMessage("??cNouveau Joueur %s, UUID: %s", name, uuidPremium);
				}
			} else {
				connection.setOnlineMode(false);
				OlympaBungee.getInstance().sendMessage("??7Joueur crack sans donn??es ??e" + name);
			}
			// Si la connection est premium
			/*if (connection.isOnlineMode()) {
				event.setCancelReason(BungeeUtils.connectScreen("&cTu ne peux pas te faire passer pour un compte premium."));
				event.setCancelled(true);
				return;
			}*/
		}

		// Si le joueur s'est d??j?? connect??
		if (olympaPlayer != null) {
			cache.setOlympaPlayer(olympaPlayer);
			OlympaBungee.getInstance().sendMessage("??7Connexion du joueur connu ??e" + olympaPlayer.getName());
			if (olympaPlayer.getPremiumUniqueId() == null) {
				connection.setOnlineMode(false);
				/*if (connection.isOnlineMode()) {
					event.setCancelReason(BungeeUtils.connectScreen(
							"&cCe pseudo &4" + olympaPlayer.getName() + "&c est utilis?? par un compte crack.\n&bIl est pr??f??rable de changer de pseudo, toutefois il est possible de faire une demande au Staff."));
					event.setCancelled(true);
					return;
				}*/
				if (!name.equals(olympaPlayer.getName())) {
					event.setCancelReason(BungeeUtils.connectScreen("&aTu as mal ??crit ton pseudo, connecte toi avec &2" + olympaPlayer.getName() + "&a.\n&eTu utilises actuellement le pseudo " + name + "."));
					event.setCancelled(true);
					return;
				}
			} else
				connection.setOnlineMode(true);
		}
		SecurityHandler securityHandler = SecurityHandler.getInstance();
		if (!connection.isOnlineMode() && !securityHandler.isAllowCrack()) {
			event.setCancelReason(BungeeUtils.connectScreen("&cLes versions Crack sont temporairement d??sactiv??es. D??sol?? du d??rangement.\nMerci de r??essayer plus tard..."));
			event.setCancelled(true);
			return;
		} else if (connection.isOnlineMode() && !securityHandler.isAllowPremium()) {
			event.setCancelReason(BungeeUtils.connectScreen("&cLes versions Premium sont temporairement d??sactiv??es. D??sol?? du d??rangement.\nMerci de r??essayer plus tard..."));
			event.setCancelled(true);
			return;
		}
		cache.setSubDomain(event.getConnection());
		try {
			DataHandler.addPlayer(cache); // Arrive de temps en temps
		} catch (Exception e) {
			e.printStackTrace();
			event.setCancelReason(BungeeUtils.connectScreen("&cUne erreur est survenue. Signale au staff. #ErrorCantData"));
			event.setCancelled(true);
			return;
		}
	}

	@EventHandler(priority = (byte) 128)
	public void on2PreLogin(PreLoginEvent event) {
		PendingConnection connection = event.getConnection();
		String name = connection.getName();
		if (event.isCancelled())
			DataHandler.removePlayer(name);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void on3Login(LoginEvent event) {
		PendingConnection connection = event.getConnection();
		String name = connection.getName();
		if (event.isCancelled())
			return;
		CachePlayer cache = DataHandler.get(name);
		if (cache == null) {
			// ?? ajouter ?? la liste des erreurs
			event.setCancelReason(BungeeUtils.connectScreen("&cUne erreur est survenue. \n\n&e&lMerci de la signaler au staff.\n&eCode d'erreur: &l#BungeeNoCache"));
			event.setCancelled(true);
			return;
		}
		OlympaPlayer olympaPlayer = cache.getOlympaPlayer();
		OlympaBungee.getInstance().sendMessage("??7LoginEvent ??6%s??7 du joueur ??e%s??7. Json: ??f%s", connection.isOnlineMode() ? "online" : "offline", name, GsonCustomizedObjectTypeAdapter.GSON.toJson(olympaPlayer));
		String ip = connection.getAddress().getAddress().getHostAddress();
		AccountProvider olympaAccount;
		// Si le joueur s'est d??j?? connect??
		if (olympaPlayer != null) {
			if (!olympaPlayer.getName().equals(name)) {
				OlympaBungee.getInstance().sendMessage("Changement de nom du joueur %s (ancien: %s)", olympaPlayer.getName(), name);
				olympaPlayer.addNewName(name);
			}
			olympaAccount = new AccountProvider(olympaPlayer.getUniqueId());
		} else
			// Si le joueur ne s'est jamais connect??
			try {
				UUID uuidCrack = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name.toLowerCase()).getBytes(StandardCharsets.UTF_8));

				// V??rifie que l'uuid n'est pas d??j?? utiliser
				OlympaPlayer uuidAlreadyExist = AccountProvider.getter().getSQL().getPlayer(uuidCrack);
				if (uuidAlreadyExist != null)
					do {
						uuidCrack = UUID.randomUUID();
						uuidAlreadyExist = AccountProvider.getter().getSQL().getPlayer(uuidCrack);
					} while (uuidAlreadyExist != null);

				olympaAccount = new AccountProvider(uuidCrack);
				olympaPlayer = olympaAccount.createOlympaPlayer(name, ip);
				BungeeNewPlayerEvent newPlayerEvent = ProxyServer.getInstance().getPluginManager().callEvent(new BungeeNewPlayerEvent(connection, olympaPlayer));
				if (newPlayerEvent.isCancelled()) {
					event.setCancelReason(newPlayerEvent.getCancelReason());
					event.setCancelled(true);
					return;
				}
				OlympaBungee.getInstance().sendMessage("Cr??ation du compte de ??6%s", name);
				olympaPlayer = olympaAccount.createNew(olympaPlayer);
				UUID uuidPremium = cache.getPremiumUUID();
				if (uuidPremium != null)
					olympaPlayer.setPremiumUniqueId(uuidPremium);
				cache.setOlympaPlayer(olympaPlayer);
			} catch (Exception | NoClassDefFoundError e) {
				e.printStackTrace();
				event.setCancelReason(BungeeUtils.connectScreen("&cUne erreur est survenue. \n\n&e&lMerci de la signaler au staff.\n&eCode d'erreur: &l#BungeeCantCreateNew"));
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

	@EventHandler(priority = (byte) 128)
	public void on4Login(PreLoginEvent event) {
		PendingConnection connection = event.getConnection();
		String name = connection.getName();
		if (event.isCancelled())
			DataHandler.removePlayer(name);
	}

	@EventHandler(priority = EventPriority.LOW)
	public void on5PostLogin(PostLoginEvent event) {
		ProxiedPlayer player = event.getPlayer();
		OlympaBungee.getInstance().sendMessage("??7PostLoginEvent ??6" + (player.getPendingConnection().isOnlineMode() ? "online" : "offline/cracked"));
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
		DataHandler.removePlayer(player.getName());
		wait.add(player.getName());
		AccountProvider olympaAccount = new AccountProvider(player.getUniqueId());
		olympaAccount.removeFromCache();
		ProxyServer.getInstance().getScheduler().schedule(OlympaBungee.getInstance(), () -> {
			wait.remove(player.getName());
			OlympaPlayer olympaPlayer = olympaAccount.getFromRedis();
			if (olympaPlayer == null) {
				OlympaBungee.getInstance().sendMessage("??4ERROR ??cLe joueur " + player.getUniqueId() + " n'avait pas de donn??es dans redis.");
				return;
			}
			olympaPlayer.setLastConnection(Utils.getCurrentTimeInSeconds());
			olympaPlayer.setConnected(false);
			olympaAccount.removeFromRedis();
			//olympaAccount.saveToDb(olympaPlayer);
		}, 4, TimeUnit.SECONDS);
		SpigotPlayerPack.playerLeaves(player);
		OlympaBungee.getInstance().sendMessage("??7D??connexion du joueur ??e" + player.getName() + (event.getPlayer().getServer() == null ? "" : " ??7(serveur ??6" + event.getPlayer().getServer().getInfo().getName() + "??7)"));
	}

}
