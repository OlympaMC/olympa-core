package fr.olympa.core.bungee.login.listener;

import java.util.concurrent.TimeUnit;

import fr.olympa.api.bungee.customevent.OlympaGroupChangeEvent;
import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.server.OlympaServer;
import fr.olympa.core.bungee.OlympaBungee;
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
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class OlympaLoginListener implements Listener {

	@EventHandler
	public void onOlympaGroupChange(OlympaGroupChangeEvent event) {
		ProxiedPlayer player = event.getPlayer();
		if (player == null)
			return;
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
		AccountProvider account = new AccountProvider(olympaPlayer.getUniqueId());
		String ip = event.getIp();
		if (!olympaPlayer.getIp().equals(ip)) {
			olympaPlayer.addNewIp(ip);
			account.saveToRedis(olympaPlayer);
			RedisBungeeSend.sendOlympaPlayer(player.getServer().getInfo(), olympaPlayer);
		}
		String[] groupsNames = olympaPlayer.getGroups().keySet().stream().map(OlympaGroup::name).toArray(String[]::new);
		if (groupsNames.length > 0)
			player.addGroups(groupsNames);
		CachePlayer cache = DataHandler.get(player.getName());
		OlympaBungee.getInstance().getTask().runTaskLater("connect_player_" + player.getUniqueId(), () -> {
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
		OlympaBungee.getInstance().getTask().cancelTaskByName("connect_player_" + player.getUniqueId());
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
							OlympaServer olympaServer2 = olympaServer;
							OlympaBungee.getInstance().getTask().runTaskLater(() -> ServersConnection.tryConnect(player, olympaServer2), 5, TimeUnit.SECONDS);
						} else {
							event.setTarget(server);
							// TODO envoyer les donnés du joueur au serveur spigot
							return;
						}
					}
				}
				ServerInfo lobby = ServersConnection.getBestServer(OlympaServer.LOBBY, null);
				if (lobby != null) {
					event.setTarget(lobby);
					RedisBungeeSend.sendOlympaPlayerFirstConnection(lobby, olympaPlayer);
					return;
				} else if (!tryConnect)
					OlympaBungee.getInstance().getTask().runTaskLater(() -> ServersConnection.tryConnect(player, OlympaServer.LOBBY), 5, TimeUnit.SECONDS);
			}
		}
		ServerInfo auth = ServersConnection.getBestServer(OlympaServer.AUTH, null);
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
}
