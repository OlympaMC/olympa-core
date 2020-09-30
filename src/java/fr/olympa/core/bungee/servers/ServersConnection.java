package fr.olympa.core.bungee.servers;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import fr.olympa.api.bungee.task.BungeeTaskManager;
import fr.olympa.api.server.OlympaServer;
import fr.olympa.core.bungee.OlympaBungee;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.ScheduledTask;

public class ServersConnection {

	private static Set<WaitingConnection> connect = new HashSet<>();

	public static void addConnection(WaitingConnection wc) {
		connect.add(wc);
	}

	public static boolean removeConnection(WaitingConnection wc) {
		return connect.remove(wc);
	}

	public static Set<WaitingConnection> getConnections(OlympaServer olympaServer) {
		return connect.stream().filter(wc -> wc.olympaServer.isSame(olympaServer)).collect(Collectors.toSet());
	}

	public static Set<WaitingConnection> getConnections(UUID uuid) {
		return connect.stream().filter(wc -> wc.uuid.equals(uuid)).collect(Collectors.toSet());
	}

	public static boolean canPlayerConnect(ServerInfo server) {
		MonitorInfo monitor = MonitorServers.getMonitor(server);
		return monitor != null && monitor.getStatus().canConnect();
	}

	public static ServerInfo getBestServer(OlympaServer olympaServer, ServerInfo except) {
		if (!olympaServer.hasMultiServers())
			return MonitorServers.getServers(olympaServer).values().stream().findFirst().map(MonitorInfo::getServerInfo).orElse(null);

		Map<ServerInfo, Integer> servers = MonitorServers.getServers(olympaServer).values().stream()
				.filter(x -> x.getStatus().canConnect() && (except == null || except.getName() != x.getName()) && (!olympaServer.hasMultiServers() || x.getMaxPlayers() * 0.9 - x.getOnlinePlayers() > 0))
				.collect(Collectors.toMap((si) -> si.getServerInfo(), (si) -> si.getMaxPlayers() - si.getOnlinePlayers()));
		Entry<ServerInfo, Integer> bestServer = servers.entrySet().stream().sorted(Map.Entry.comparingByValue()).findFirst().orElse(null);
		if (bestServer != null)
			return bestServer.getKey();
		// TODO create new server
		return null;
	}

	@SuppressWarnings("deprecation")
	public static ServerInfo getServerByNameOrIpPort(String nameOrIpPort) {
		Map<String, ServerInfo> servers = ProxyServer.getInstance().getServers();
		ServerInfo server = servers.get(nameOrIpPort);
		if (server == null) {
			String[] ipPort = nameOrIpPort.split(":");
			if (ipPort.length >= 2) {
				String ip = ipPort[0].replace("localhost", "127.0.0.1");
				server = servers.values().stream().filter(sr -> {
					return sr.getAddress().getAddress().getHostAddress().equals(ip) && sr.getAddress().getPort() == Integer.parseInt(ipPort[1]);
				}).findFirst().orElse(null);
			}
		}
		return server;
	}

	public static boolean removeTryToConnect(ProxiedPlayer player) {
		boolean b = false;
		Set<WaitingConnection> wcs = getConnections(player.getUniqueId());
		for (WaitingConnection wc : wcs) {
			ScheduledTask task = wc.task;
			if (task != null) {
				task.cancel();
				b = true;
			}
		}
		return b;
	}

	public static void tryConnect(ProxiedPlayer player, OlympaServer olympaServer) {
		removeTryToConnect(player);
		BungeeTaskManager taskHandler = OlympaBungee.getInstance().getTask();
		int taskId = OlympaBungee.getInstance().getTask().scheduleSyncRepeatingTask(new QueueSpigotTask(player, olympaServer), 0, 15, TimeUnit.SECONDS);
		addConnection(new WaitingConnection(player.getUniqueId(), olympaServer, taskHandler.getTask(taskId)));
	}
}
