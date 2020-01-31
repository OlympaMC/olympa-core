package fr.olympa.core.bungee.datamanagment;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import fr.olympa.api.objects.OlympaConsole;
import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.login.events.OlympaPlayerLoginEvent;
import fr.olympa.core.bungee.utils.BungeeUtils;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

@SuppressWarnings("deprecation")
public class BasicSecurityListener implements Listener {

	private Cache<String, String> cache = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).build();

	@EventHandler
	public void on0Ping(ProxyPingEvent event) {
		PendingConnection connection = event.getConnection();
		this.cache.put(connection.getAddress().getAddress().getHostAddress(), "");
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void on1PreLogin(PreLoginEvent event) {
		if (event.isCancelled()) {
			return;
		}
		PendingConnection connection = event.getConnection();
		String name = connection.getName();
		String ip = connection.getAddress().getAddress().getHostAddress();

		//ProtocolSupport.

		if (!name.matches("[a-zA-Z0-9_]*")) {
			event.setCancelReason(BungeeUtils.connectScreen("&6Ton pseudo doit contenir uniquement des chiffres, des lettres et des tiret bas."));
			event.setCancelled(true);
			return;
		}

		String connectIp = event.getConnection().getVirtualHost().getHostName();
		String connectDomain = Utils.getAfterFirst(connectIp, ".");
		// Vérifie si l'adresse est correct
		if (!connectDomain.equalsIgnoreCase("olympa.fr") && !connectDomain.equalsIgnoreCase("olympa.net")) {
			ProxyServer.getInstance().getScheduler().schedule(OlympaBungee.getInstance(), () -> {
				ProxiedPlayer player = ProxyServer.getInstance().getPlayer(name);
				if (player != null) {
					event.setCancelReason(BungeeUtils.connectScreen("§7[§cSécuriter§7] &cTu dois te connecter avec l'adresse &nplay.olympa.fr&c."));
				}

			}, 5, TimeUnit.SECONDS);
		}

		String test = this.cache.asMap().get(ip);
		long uptime = Utils.getCurrentTimeInSeconds() - OlympaBungee.getInstance().getUptimeLong();
		if (uptime > 60 && test == null) {
			event.setCancelReason(BungeeUtils.connectScreen("§7[§cSécuriter§7] §6Tu dois ajouter le serveur avant de pouvoir te connecter.\n La connexion direct n'est pas autoriser."));
			event.setCancelled(true);
			return;
		}

		// Vérifie si le joueur n'est pas déjà connecté
		ProxiedPlayer target = ProxyServer.getInstance().getPlayer(name);
		if (target != null) {
			if (!target.getAddress().getAddress().getHostAddress().equals(ip)) {
				event.setCancelReason(BungeeUtils.connectScreen("&cTon compte est déjà connecté avec une IP différente de la tienne."));
				event.setCancelled(true);
				return;
			}
			event.setCancelReason(BungeeUtils.connectScreen("&cTon compte est déjà connecté."));
			event.setCancelled(true);
		}

		/*String test = this.cache.asMap().get(ip);
		if (test == null) {
			event.setCancelled(true);
			event.setCancelReason(BungeeUtils.connectScreen("&7[&cSécuriter&7] &6Tu dois ajouter le serveur avant de pouvoir te connecter.\n La connexion direct n'est pas autoriser."));
			return;
		}
		this.cache.invalidate(ip);*/
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void on2Login(LoginEvent event) {
		if (event.isCancelled()) {
			return;
		}
		UUID playerUUID = event.getConnection().getUniqueId();
		String playername = event.getConnection().getName();
		if (playerUUID == OlympaConsole.getUniqueId() || playername.equalsIgnoreCase(OlympaConsole.getName())) {
			event.setCancelReason(BungeeUtils.connectScreen("&cImpossible de se connecter avec ce pseudo."));
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void on3OlympaPlayerLogin(OlympaPlayerLoginEvent event) {
		OlympaPlayer olympaPlayer = event.getOlympaPlayer();
		String ip = event.getIp();
		olympaPlayer.addNewIp(ip);
	}

	//Cache<String, String> cache = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).maximumSize(size).build();

	/*@EventHandler
	public void on0Ping(ProxyPingEvent event) {
		PendingConnection connection = event.getConnection();
		this.cache.put(connection.getAddress().getAddress().getHostAddress(), "");
	}*/
	/*@EventHandler
	public void ServerConnectEvent(ServerConnectEvent event) {
		ProxiedPlayer player = event.getPlayer();
		ServerInfo serverTarget = event.getTarget();
		serverTarget.getPlayers().stream().filter(p -> player.getUniqueId().equals(p.getUniqueId())).forEach(p -> p.getPendingConnection().disconnect());
	}*/
	/*@EventHandler(priority = EventPriority.HIGHEST)
	public void on4Disconnect(PlayerDisconnectEvent event) {
		ProxiedPlayer player = event.getPlayer();
		this.cache.put(player.getAddress().getAddress().getHostAddress(), "");
	}*/

}
