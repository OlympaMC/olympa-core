package fr.olympa.core.bungee.security;

import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import fr.olympa.api.bungee.customevent.OlympaPlayerLoginEvent;
import fr.olympa.api.bungee.utils.BungeeUtils;
import fr.olympa.api.common.match.RegexMatcher;
import fr.olympa.api.common.player.OlympaConsole;
import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.utils.CacheStats;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.common.provider.AccountProvider;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

@SuppressWarnings("deprecation")
public class BasicSecurityListener implements Listener {

	private static final Cache<String, String> cache = CacheBuilder.newBuilder().recordStats().expireAfterWrite(10, TimeUnit.MINUTES).maximumSize(100).build();

	{
		CacheStats.addCache("WHO_PING", BasicSecurityListener.cache);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void on0Ping(ProxyPingEvent event) {
		PendingConnection connection = event.getConnection();
		cache.put(connection.getAddress().getAddress().getHostAddress(), "");
		InetSocketAddress test = (InetSocketAddress) connection.getSocketAddress();
	}

	@EventHandler
	public void on1PlayerDisconnect(PlayerDisconnectEvent event) {
		ProxiedPlayer player = event.getPlayer();
		cache.put(player.getAddress().getAddress().getHostAddress(), "");
	}

	@EventHandler(priority = -128)
	public void on1PreLogin(PreLoginEvent event) {
		if (event.isCancelled())
			return;
		PendingConnection connection = event.getConnection();
		String name = connection.getName();
		String ip = connection.getAddress().getAddress().getHostAddress();

		if (!RegexMatcher.USERNAME.is(name)) {
			event.setCancelReason(BungeeUtils.connectScreen("&6Ton pseudo doit contenir uniquement des chiffres, des lettres et des tiret bas et entre 4 et 16 caract??res."));
			event.setCancelled(true);
			return;
		}

		String connectIp = event.getConnection().getVirtualHost().getHostName();
		String connectDomain = Utils.getAfterFirst(connectIp, ".");
		String subdomain = event.getConnection().getVirtualHost().getHostName().split("\\.")[0];

		SecurityHandler securityHandler = SecurityHandler.getInstance();
		// V??rifie si l'adresse est correct
		if (RegexMatcher.IP.is(connectIp) && securityHandler.isCheckCorrectEntredIpNumber()
				|| securityHandler.isCheckCorrectEntredIp() && (!connectDomain.equalsIgnoreCase("olympa.fr") && !connectDomain.equalsIgnoreCase("olympa.net")
						|| !subdomain.equalsIgnoreCase("play") && !subdomain.equalsIgnoreCase("buildeur") && !subdomain.equalsIgnoreCase("dev"))) {
			event.setCancelReason(BungeeUtils.connectScreen("&7[&cS??curit??&7] &cTu dois te connecter avec l'adresse &nplay.olympa.fr&c."));
			event.setCancelled(true);
		}

		if (securityHandler.isPingBeforeJoin() || securityHandler.getAntibot().isEnable()) {
			String test = cache.asMap().get(ip);
			if (test == null) {
				event.setCancelReason(BungeeUtils.connectScreen("&7[&cS??curit??&7] &2Actualise la liste des serveurs pour te connecter.&r"));
				event.setCancelled(true);
				return;
			}
		}

		// V??rifie si le joueur n'est pas d??j?? connect??
		ProxiedPlayer target = ProxyServer.getInstance().getPlayer(name);
		if (target != null) {
			if (!target.getAddress().getAddress().getHostAddress().equals(ip)) {
				event.setCancelReason(BungeeUtils.connectScreen("&cTon compte est d??j?? connect?? avec une IP diff??rente de la tienne."));
				event.setCancelled(true);
				return;
			}
			String turne = "";
			OlympaPlayer olympaPlayer;
			try {
				olympaPlayer = new AccountProvider(target.getUniqueId()).get();
			} catch (SQLException e) {
				olympaPlayer = null;
				e.printStackTrace();
			}
			if (olympaPlayer != null)
				turne = olympaPlayer.getGender().getTurne();
			target.disconnect(BungeeUtils.connectScreen("&cTu t'es connect??" + turne + " depuis une autre fen??tre sur ton r??seau."));
			event.setCancelled(false);
		}

		/*
		 * String test = this.cache.asMap().get(ip); if (test == null) {
		 * event.setCancelled(true); event.setCancelReason(BungeeUtils.
		 * connectScreen("&7[&cS??curiter&7] &6Tu dois ajouter le serveur avant de pouvoir te connecter.\n La connexion direct n'est pas autoriser."
		 * )); return; } this.cache.invalidate(ip);
		 */
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void on2Login(LoginEvent event) {
		if (event.isCancelled())
			return;
		UUID playerUUID = event.getConnection().getUniqueId();
		String playername = event.getConnection().getName();
		if (playerUUID == OlympaConsole.getUniqueId() || playername.equalsIgnoreCase(OlympaConsole.getName())) {
			event.setCancelReason(BungeeUtils.connectScreen("&cImpossible de se connecter avec ce pseudo."));
			event.setCancelled(true);
		}
	}

	// TODO Change file
	@EventHandler(priority = EventPriority.LOW)
	public void on3OlympaPlayerLogin(OlympaPlayerLoginEvent event) {
		OlympaPlayer olympaPlayer = event.getOlympaPlayer();
		String ip = event.getIp();
		if (!olympaPlayer.getIp().equals(ip))
			olympaPlayer.addNewIp(ip);
		cache.invalidate(ip);
	}

	// Cache<String, String> cache = CacheBuilder.newBuilder().expireAfterWrite(10,
	// TimeUnit.MINUTES).maximumSize(size).build();

	/*
	 * @EventHandler public void on0Ping(ProxyPingEvent event) { PendingConnection
	 * connection = event.getConnection();
	 * this.cache.put(connection.getAddress().getAddress().getHostAddress(), ""); }
	 */
	/*
	 * @EventHandler public void ServerConnectEvent(ServerConnectEvent event) {
	 * ProxiedPlayer player = event.getPlayer(); ServerInfo serverTarget =
	 * event.getTarget(); serverTarget.getPlayers().stream().filter(p ->
	 * player.getUniqueId().equals(p.getUniqueId())).forEach(p ->
	 * p.getPendingConnection().disconnect()); }
	 */
	/*
	 * @EventHandler(priority = EventPriority.HIGHEST) public void
	 * on4Disconnect(PlayerDisconnectEvent event) { ProxiedPlayer player =
	 * event.getPlayer();
	 * this.cache.put(player.getAddress().getAddress().getHostAddress(), ""); }
	 */

}
