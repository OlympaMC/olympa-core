package fr.olympa.core.bungee.servers;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.bungee.task.BungeeTaskManager;
import fr.olympa.api.common.bash.OlympaRuntime;
import fr.olympa.api.common.match.MatcherPattern;
import fr.olympa.api.common.match.RegexMatcher;
import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.common.server.OlympaServer;
import fr.olympa.api.common.server.ServerInfoAdvanced;
import fr.olympa.api.common.server.ServerInfoAdvancedBungee;
import fr.olympa.api.common.server.ServerStatus;
import fr.olympa.api.common.sort.Sorting;
import fr.olympa.api.spigot.utils.ProtocolAPI;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.common.provider.AccountProvider;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.protocol.packet.KeepAlive;

public class ServersConnection {

	private static Set<WaitingConnection> connect = new HashSet<>();
	public static Set<ServerInfo> waitToStart = new HashSet<>();

	public static void addConnection(WaitingConnection wc) {
		Set<WaitingConnection> list = connect.stream().filter(wc2 -> wc2.uuid.equals(wc.uuid)).collect(Collectors.toSet());
		if (!list.isEmpty())
			connect.removeAll(list);
		connect.add(wc);
	}

	private static boolean removeConnection(WaitingConnection wc) {
		return connect.remove(wc);
	}

	public static Set<WaitingConnection> getConnections(OlympaServer olympaServer) {
		return connect.stream().filter(wc -> wc.olympaServer.isSame(olympaServer)).collect(Collectors.toSet());
	}

	public static WaitingConnection getConnections(UUID uuid) {
		Set<WaitingConnection> list = connect.stream().filter(wc -> wc.uuid.equals(uuid)).collect(Collectors.toSet());
		WaitingConnection wc = null;
		if (!list.isEmpty())
			wc = list.iterator().next();
		// TODO remove this after masse test
		if (list.size() > 1) {
			list.remove(wc);
			connect.removeAll(list);
			new Exception("DEBUG > C'est pas normal qu'il y est plusieurs WaitingConnection de " + uuid).printStackTrace();
		}
		// ------------------------------
		return wc;
	}

	public static boolean canPlayerConnect(ServerInfo server) {
		ServerInfoAdvanced monitor = OlympaBungee.getInstance().getMonitoring().getMonitor(server);
		return monitor != null && monitor.getStatus().canConnect();
	}

	public static ServerInfo getBestServer(OlympaServer olympaServer, ServerInfo except) {
		return getBestServer(olympaServer, except, null);
	}

	public static ServerInfo getBestServer(OlympaServer olympaServer, ServerInfo except, @Nullable ProxiedPlayer w8forConnect) {
		if (!olympaServer.hasMultiServers())
			return OlympaBungee.getInstance().getMonitoring().getServers(olympaServer).values().stream().findFirst().map(ServerInfoAdvancedBungee::getServerInfo).orElse(null);

		List<ServerInfoAdvancedBungee> servers = OlympaBungee.getInstance().getMonitoring().getServers(olympaServer).values().stream()
				.filter(x -> x.hasMinimalInfo() && x.getStatus().canConnect() && (except == null || !except.getName().equals(x.getName()))
						&& (!x.getOlympaServer().hasMultiServers() || x.getMaxPlayers() * 0.9 - x.getOnlinePlayers() > 0))
				.sorted(new Sorting<>(Map.of(server -> server.getOnlinePlayers(), false, server -> server.getServerId(), true)))
				.collect(Collectors.toList());
		if (!servers.isEmpty()) {
			if (w8forConnect != null)
				try {
					OlympaPlayer olympaPlayer = new AccountProvider(w8forConnect.getUniqueId()).get();
					ProtocolAPI version = ProtocolAPI.getHighestVersion(w8forConnect.getPendingConnection().getVersion());
					if (olympaPlayer != null && version != null) {
						ServerInfoAdvancedBungee serv = servers.stream().filter(sr -> sr.canConnect(olympaPlayer, version)).findFirst().orElse(null);
						if (serv != null)
							return serv.getServerInfo();
					}
				} catch (SQLException e) {
					e.printStackTrace();
					return null;
				}
			return servers.get(0).getServerInfo();
		}

		// Ouvre un serveur
		ServerInfo serverToOpen = OlympaBungee.getInstance().getMonitoring().getServersMap().entrySet().stream().filter(e -> (except == null || !e.getKey().getName().equals(except.getName()))
				&& OlympaBungee.getInstance().getMonitoring().getServers(olympaServer).values().stream().anyMatch(mInfo -> mInfo.getName().equals(e.getKey().getName())
						&& mInfo.getStatus().equals(ServerStatus.CLOSE) && mInfo.isUsualError()))
				.map(Entry::getKey).findFirst().orElse(null);
		if (serverToOpen != null) {
			waitToStart.contains(serverToOpen);
			if (!waitToStart.contains(serverToOpen) && waitToStart.size() <= 2) {
				waitToStart.add(serverToOpen);
				LinkSpigotBungee.getInstance().getTask().runTaskLater(() -> {
					waitToStart.remove(serverToOpen);
				}, 1, TimeUnit.MINUTES);
				OlympaRuntime.action("start", serverToOpen.getName()).start();
			}
			if (w8forConnect != null) {
				while (waitToStart.contains(serverToOpen))
					try {
						if (!w8forConnect.isConnected())
							break;
						w8forConnect.getPendingConnection().unsafe().sendPacket(new KeepAlive(ThreadLocalRandom.current().nextLong()));
						LinkSpigotBungee.getInstance().sendMessage("&6Wait serveur %s to start, %s's thread is sleeping for 5s", serverToOpen.getName(), w8forConnect.getName());
						Thread.sleep(5000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				if (!w8forConnect.isConnected())
					return null;
				return serverToOpen;
			}
		}
		return null;
		// TODO create new server
	}

	@SuppressWarnings("deprecation")
	@Nullable
	public static ServerInfo getServerByNameOrIpPort(String nameOrIpPort) {
		Map<String, ServerInfo> servers = ProxyServer.getInstance().getServers();
		ServerInfo server = servers.get(nameOrIpPort);
		if (server == null) {
			String[] ipPort = nameOrIpPort.split(":");
			if (ipPort.length >= 2) {
				String ip = ipPort[0].replace("localhost", "127.0.0.1");
				MatcherPattern<Integer> regexInt = RegexMatcher.INT;
				if (!regexInt.startWith(ipPort[1]))
					throw new IllegalAccessError(String.format("%s need to be a INT (a port like 25565) but it is not.", ipPort[1]));
				Integer port = regexInt.extractAndParse(ipPort[1]);
				server = servers.values().stream().filter(sr -> {
					return sr.getAddress().getAddress().getHostAddress().equals(ip) && sr.getAddress().getPort() == port;
				}).findFirst().orElse(null);
			}
		}
		return server;
	}

	public static boolean removeTryToConnect(ProxiedPlayer player) {
		return removeTryToConnect(player, false);
	}

	public static boolean removeTryToConnect(ProxiedPlayer player, boolean isPostLoginEvent) {
		boolean b = false;
		WaitingConnection wc = getConnections(player.getUniqueId());
		if (wc == null)
			return b;
		if (wc.isChangeServer == isPostLoginEvent && wc.isChangeServer) {
			wc.isChangeServer = false;
			return b;
		}
		BungeeTaskManager taskHandler = (BungeeTaskManager) LinkSpigotBungee.getInstance().getTask();
		ScheduledTask task = wc.task;
		if (task != null) {
			taskHandler.cancelTaskById(task.getId());
			b = true;
			wc.task = null;
		}
		ServersConnection.removeConnection(wc);
		return b;
	}

	public static void tryConnect(ProxiedPlayer player, OlympaServer olympaServer, boolean isChangeServer) {
		removeTryToConnect(player);
		if (!player.isConnected())
			return;
		BungeeTaskManager taskHandler = (BungeeTaskManager) LinkSpigotBungee.getInstance().getTask();
		ScheduledTask task = taskHandler.scheduleSyncRepeatingTaskAndGet("tryconnect_player_" + player.getUniqueId(), new QueueSpigotServerTask(player, olympaServer), 3, 20, TimeUnit.SECONDS);
		addConnection(new WaitingConnection(player.getUniqueId(), olympaServer, task, isChangeServer));
	}
}
