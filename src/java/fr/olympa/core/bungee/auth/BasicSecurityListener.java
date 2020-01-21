package fr.olympa.core.bungee.auth;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import fr.olympa.api.objects.OlympaConsole;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.OlympaBungee;
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
		if (!name.matches("[a-zA-Z0-9_]*")) {
			event.setCancelled(true);
			event.setCancelReason(BungeeUtils.connectScreen("&6Ton pseudo doit contenir uniquement des chiffres, des lettres et des tiret bas."));
			return;
		}

		String test = this.cache.asMap().get(ip);
		long uptime = Utils.getCurrentTimeInSeconds() - OlympaBungee.getInstance().getUptimeLong();
		if (uptime > 60 && test == null) {
			event.setCancelReason(BungeeUtils.connectScreen("§7[§cSécuriter§7] §6Tu dois ajouter le serveur avant de pouvoir te connecter.\n La connexion direct n'est pas autoriser."));
			return;
		}

		// Vérifie si l'adresse est correct
		if (!Utils.getAfterFirst(event.getConnection().getVirtualHost().getHostName(), ".").equalsIgnoreCase("olympa.fr")) {
			ProxyServer.getInstance().getScheduler().schedule(OlympaBungee.getInstance(), () -> {
				ProxiedPlayer player = ProxyServer.getInstance().getPlayer(name);
				if (player != null) {
					player.sendMessage(BungeeUtils.color("&cMerci de vous connecter avec l'adresse &nplay.olympa.fr&c la prochaine fois, l'adresse que vous utilier ne fonctionnera bientôt plus."));
				}

			}, 5, TimeUnit.SECONDS);
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

	@EventHandler(priority = EventPriority.LOWEST)
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
