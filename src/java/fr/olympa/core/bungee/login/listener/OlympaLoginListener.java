package fr.olympa.core.bungee.login.listener;

import java.util.concurrent.TimeUnit;

import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.server.OlympaServer;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.api.customevent.OlympaGroupChangeEvent;
import fr.olympa.core.bungee.datamanagment.CachePlayer;
import fr.olympa.core.bungee.datamanagment.DataHandler;
import fr.olympa.core.bungee.login.events.OlympaPlayerLoginEvent;
import fr.olympa.core.bungee.redis.RedisBungeeSend;
import fr.olympa.core.bungee.servers.MonitorServers;
import fr.olympa.core.bungee.servers.ServersConnection;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerConnectEvent.Reason;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

@SuppressWarnings("deprecation")
public class OlympaLoginListener implements Listener {

	@EventHandler
	public void onOlympaGroupChange(OlympaGroupChangeEvent event) {
		ProxiedPlayer player = event.getPlayer();
		player.removeGroups(player.getGroups().toArray(new String[0]));
		OlympaPlayer olympaPlayer = event.getOlympaPlayer();
		String[] groupsNames = olympaPlayer.getGroups().keySet().stream().map(OlympaGroup::name).toArray(String[]::new);
		if (groupsNames.length > 0)
			player.addGroups(groupsNames);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onOlympaPlayerLogin(OlympaPlayerLoginEvent event) {
		ProxiedPlayer player = event.getPlayer();
		OlympaPlayer olympaPlayer = event.getOlympaPlayer();
		String[] groupsNames = olympaPlayer.getGroups().keySet().stream().map(OlympaGroup::name).toArray(String[]::new);
		if (groupsNames.length > 0)
			player.addGroups(groupsNames);
		String ip = player.getAddress().getAddress().getHostAddress();
		if (!olympaPlayer.getIp().equals(ip))
			olympaPlayer.addNewIp(ip);
		CachePlayer cache = DataHandler.get(player.getName());
		OlympaBungee.getInstance().getTask().runTaskLater(() -> {
			if (cache != null) {
				if (!olympaPlayer.isPremium()) {
					String subdomain = cache.getSubDomain();
					if (subdomain != null)
						if (subdomain.equalsIgnoreCase("buildeur")) {
							ServersConnection.tryConnect(player, OlympaServer.BUILDEUR);
							return;
						} else if (subdomain.equalsIgnoreCase("dev")) {
							ServersConnection.tryConnect(player, OlympaServer.DEV);
							return;
						}
					ServersConnection.tryConnect(player, OlympaServer.LOBBY);
				}
				DataHandler.removePlayer(cache);
			}
		}, 2, TimeUnit.SECONDS);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerDisconnect(PlayerDisconnectEvent event) {
		ProxiedPlayer player = event.getPlayer();
		player.removeGroups(player.getGroups().toArray(new String[0]));
		ServersConnection.removeTryToConnect(player);
	}

	@EventHandler
	public void onServerConnect(ServerConnectEvent event) {
		if (event.isCancelled())
			return;
		Reason reason = event.getReason();
		if (reason != Reason.JOIN_PROXY)
			return;
		ProxiedPlayer player = event.getPlayer();
		boolean tryConnect = false;
		CachePlayer cache = DataHandler.get(player.getName());
		if (cache != null) {
			OlympaPlayer olympaPlayer = cache.getOlympaPlayer();
			if (olympaPlayer != null && olympaPlayer.isConnected() && olympaPlayer.isPremium()) {
				String subdomain = cache.getSubDomain();
				if (subdomain != null && !subdomain.equalsIgnoreCase("play")) {
					OlympaServer olympaServer = null;
					if (subdomain.equalsIgnoreCase("buildeur"))
						olympaServer = OlympaServer.BUILDEUR;
					else if (subdomain.equalsIgnoreCase("dev"))
						olympaServer = OlympaServer.DEV;
					if (olympaServer != null) {
						ServerInfo server = ServersConnection.getBestServer(olympaServer, null);
						if (server == null || !MonitorServers.getMonitor(server).isOpen()) {
							tryConnect = true;
							ServersConnection.tryConnect(player, olympaServer);
						} else {
							event.setTarget(server);
							return;
						}
					}
				}
				ServerInfo lobby = ServersConnection.getBestServer(OlympaServer.LOBBY, null);
				if (lobby != null) {
					event.setTarget(lobby);
					return;
				} else if (!tryConnect)
					ServersConnection.tryConnect(player, OlympaServer.LOBBY);
			}
		}
		ServerInfo auth = ServersConnection.getBestServer(OlympaServer.AUTH, null);
		System.out.println("ok " + auth);
		if (auth != null)
			event.setTarget(auth);
	}

	@EventHandler
	public void onServerConnected(ServerConnectedEvent event) {
		ProxiedPlayer player = event.getPlayer();
		ServersConnection.removeTryToConnect(player);
		CachePlayer cache = DataHandler.get(player.getName());
		if (cache != null) {
			OlympaPlayer olympaPlayer = cache.getOlympaPlayer();
			if (olympaPlayer != null && olympaPlayer.isConnected() && olympaPlayer.isPremium())
				DataHandler.removePlayer(cache);
		}
	}

	@EventHandler
	public void onServerSwitch(ServerSwitchEvent event) {
		ProxiedPlayer player = event.getPlayer();
		if (event.getFrom() != null)
			RedisBungeeSend.askGiveOlympaPlayer(event.getFrom(), player.getServer().getInfo(), player.getUniqueId());
	}
}
