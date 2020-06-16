package fr.olympa.core.bungee.servers;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import fr.olympa.api.server.OlympaServer;
import fr.olympa.api.server.ServerStatus;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.utils.BungeeUtils;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.ScheduledTask;

public class ServersConnection {
	
	private static Map<ProxiedPlayer, ScheduledTask> connect = new HashMap<>();
	
	public static boolean canPlayerConnect(ServerInfo name) {
		MonitorInfo monitor = MonitorServers.get(name.getName());
		return monitor != null && !monitor.getStatus().equals(ServerStatus.CLOSE) && !monitor.getStatus().equals(ServerStatus.UNKNOWN);
	}
	
	public static ServerInfo getAuth() {
		return getAuth(null);
	}
	
	public static ServerInfo getAuth(ServerInfo noThis) {
		Map<ServerInfo, Integer> auths = MonitorServers.getLastServerInfo().stream().filter(si -> {
			return si.isOpen() && (noThis == null || noThis.getName() != si.getName()) && si.getName().startsWith("auth") && si.getMaxPlayers() - si.getOnlinePlayers() > 0;
		}).collect(Collectors.toMap((si) -> si.getServerInfo(), (si) -> si.getMaxPlayers() - si.getOnlinePlayers()));
		// TODO add sort by name1 name2 name3
		Entry<ServerInfo, Integer> auth = auths.entrySet().stream().sorted(Map.Entry.comparingByValue()).findFirst().orElse(null);
		if (auth != null)
			return auth.getKey();
		// TODO create new server
		return null;
	}
	
	public static ServerInfo getLobby() {
		return getLobby(null);
	}
	
	public static ServerInfo getLobby(ServerInfo noThis) {
		Map<ServerInfo, Integer> lobbys = MonitorServers.getLastServerInfo().stream().filter(si -> isValidLobby(si, noThis)).collect(Collectors.toMap((si) -> si.getServerInfo(), (si) -> si.getMaxPlayers() - si.getOnlinePlayers()));
		// TODO add sort by name1 name2 name3
		Entry<ServerInfo, Integer> lobby = lobbys.entrySet().stream().sorted(Map.Entry.comparingByValue()).findFirst().orElse(null);
		if (lobby != null) return lobby.getKey();
		// TODO create new server
		return null;
	}
	
	private static boolean isValidLobby(MonitorInfo server, ServerInfo notThis) {
		System.out.println(server.getName() + " " + server.getOnlinePlayers() + " " + server.getMaxPlayers());
		if (!server.isOpen()) return false;
		if (notThis != null && server.getName().equals(notThis.getName())) return false;
		if (!server.getName().startsWith("lobby")) return false;
		if (server.getMaxPlayers() * 0.9 - server.getOnlinePlayers() <= 0) return false;
		return true;
	}

	public static ServerInfo getServer(String name) {
		return MonitorServers.getLastServerInfo().stream().filter(si -> si.getError() == null && si.getName().startsWith(name)).map(MonitorInfo::getServerInfo).findFirst().orElse(null);
	}
	
	@SuppressWarnings("deprecation")
	public static ServerInfo getServerByNameOrIpPort(String nameOrIpPort) {
		Map<String, ServerInfo> servers = ProxyServer.getInstance().getServers();
		ServerInfo server = servers.get(nameOrIpPort);
		if (server == null) {
			String[] ipPort = nameOrIpPort.split(":");
			if (ipPort.length >= 2)
				server = servers.values().stream().filter(sr -> sr.getAddress().getAddress().getHostAddress().equals(ipPort[0]) && sr.getAddress().getPort() == Integer.parseInt(ipPort[1])).findFirst().orElse(null);
		}
		return server;
	}
	
	public static void removeTryToConnect(ProxiedPlayer player) {
		ScheduledTask task = connect.get(player);
		if (task != null) {
			task.cancel();
			connect.remove(player);
		}
	}
	
	public static void tryConnect(ProxiedPlayer player, OlympaServer olympaServer, ServerInfo server) {
		ScheduledTask task = ProxyServer.getInstance().getScheduler().schedule(OlympaBungee.getInstance(), () -> tryConnectTo(player, olympaServer, server), 0, 30, TimeUnit.SECONDS);
		connect.put(player, task);
	}
	
	@SuppressWarnings("deprecation")
	private static void tryConnectTo(ProxiedPlayer player, OlympaServer olympaServer, ServerInfo server) {
		if (olympaServer != null)
			switch (olympaServer) {
			case LOBBY:
				server = ServersConnection.getLobby();
				break;
			case AUTH:
				server = ServersConnection.getAuth();
				break;
			default:
				server = getServer(olympaServer.getName());
				break;
			}
		if (server == null) {
			player.sendMessage(Prefix.DEFAULT_BAD + BungeeUtils.color("Aucun serveur " + olympaServer.getNameCaps() + " n'est actuellement disponible, merci de patienter..."));
			return;
		}
		String serverName = Utils.capitalize(server.getName());
		if (!canPlayerConnect(server)) {
			player.sendMessage(Prefix.DEFAULT_BAD + BungeeUtils.color("Tu es en file d'attente pour rejoindre le serveur &4" + serverName + "&c..."));
			return;
		}
		player.sendMessage(Prefix.DEFAULT_GOOD + BungeeUtils.color("Tentative de connexion au serveur &2" + serverName + "&a..."));
		player.connect(server);
		return;
		
	}
}
