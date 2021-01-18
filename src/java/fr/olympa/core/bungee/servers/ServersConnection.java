package fr.olympa.core.bungee.servers;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.bungee.task.BungeeTaskManager;
import fr.olympa.api.server.OlympaServer;
import fr.olympa.api.server.ServerStatus;
import fr.olympa.api.utils.machine.OlympaRuntime;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.protocol.packet.KeepAlive;

public class ServersConnection {

	private static Set<WaitingConnection> connect = new HashSet<>();
	public static Set<ServerInfo> waitToStart = new HashSet<>();

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
		return getBestServer(olympaServer, except, null);
	}

	public static ServerInfo getBestServer(OlympaServer olympaServer, ServerInfo except, ProxiedPlayer w8forConnect) {
		if (!olympaServer.hasMultiServers())
			return MonitorServers.getServers(olympaServer).values().stream().findFirst().map(MonitorInfo::getServerInfo).orElse(null);

		Map<ServerInfo, Integer> servers = MonitorServers.getServers(olympaServer).values().stream()
				.filter(x -> x.getStatus().canConnect() && (except == null || !except.getName().equals(x.getName())) && (!olympaServer.hasMultiServers() || x.getMaxPlayers() * 0.9 - x.getOnlinePlayers() > 0))
				.collect(Collectors.toMap((si) -> si.getServerInfo(), (si) -> si.getMaxPlayers() - si.getOnlinePlayers()));
		ServerInfo bestServer = servers.entrySet().stream().sorted(Map.Entry.comparingByValue()).map(Entry::getKey).findFirst().orElse(null);
		if (bestServer != null)
			return bestServer;

		// Ouvre un serveur
		ServerInfo serverToOpen = MonitorServers.getServersMap().entrySet().stream().filter(e -> MonitorServers.getServers(olympaServer).values().stream()
				.anyMatch(mInfo -> mInfo.getName().equals(e.getKey().getName()) && mInfo.getStatus().equals(ServerStatus.CLOSE) && mInfo.isUsualError())).map(Entry::getKey).findFirst().orElse(null);
		if (serverToOpen != null && !waitToStart.contains(serverToOpen)) {
			waitToStart.add(serverToOpen);
			LinkSpigotBungee.Provider.link.getTask().runTaskLater(() -> {
				waitToStart.remove(serverToOpen);
			}, 1, TimeUnit.MINUTES);
			OlympaRuntime.actionForAllLines("start", serverToOpen.getName(), x -> readScriptMC(x)).start();
			if (w8forConnect != null)
				while (waitToStart.contains(serverToOpen))
					try {
						w8forConnect.getPendingConnection().unsafe().sendPacket(new KeepAlive(ThreadLocalRandom.current().nextLong()));
						Thread.sleep(5000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
		}
		return serverToOpen;
		// TODO create new server
	}

	public static void readScriptMC(String s) {

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
		BungeeTaskManager taskHandler = (BungeeTaskManager) LinkSpigotBungee.Provider.link.getTask();
		for (WaitingConnection wc : wcs) {
			ScheduledTask task = wc.task;
			if (task != null) {
				taskHandler.cancelTaskById(task.getId());
				System.out.println("Task removeTryToConnect cancel");
				b = true;
				wc.task = null;
			}
			ServersConnection.removeConnection(wc);
		}
		return b;
	}

	public static void tryConnect(ProxiedPlayer player, OlympaServer olympaServer) {
		removeTryToConnect(player);
		BungeeTaskManager taskHandler = (BungeeTaskManager) LinkSpigotBungee.Provider.link.getTask();
		ScheduledTask task = taskHandler.scheduleSyncRepeatingTaskAndGet("tryconnect_player_" + player.getUniqueId(), new QueueSpigotTask(player, olympaServer), 0, 20, TimeUnit.SECONDS);
		addConnection(new WaitingConnection(player.getUniqueId(), olympaServer, task));
	}
}
