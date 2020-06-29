package fr.olympa.core.bungee.servers;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.ImmutableMap;

import fr.olympa.api.server.OlympaServer;
import fr.olympa.api.server.ServerStatus;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.redis.RedisBungeeSend;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.TaskScheduler;

public class MonitorServers {
	
	private static Map<OlympaServer, Map<Integer, MonitorInfo>> olympaServers = Arrays.stream(OlympaServer.values()).collect(ImmutableMap.toImmutableMap(x -> x, x -> new HashMap<>()));
	private static Map<ServerInfo, MonitorInfo> bungeeServers = new HashMap<>();
	
	public static Map<Integer, MonitorInfo> getServers(OlympaServer server) {
		return olympaServers.get(server);
	}

	public static MonitorInfo getMonitor(ServerInfo server) {
		return bungeeServers.get(server);
	}

	public static Collection<MonitorInfo> getServers() {
		return bungeeServers.values();
	}

	public static void updateServer(ServerInfo serverInfo, boolean instantUpdate) {
		long nano = System.nanoTime();
		serverInfo.ping((result, error) -> {
			MonitorInfo info = new MonitorInfo(serverInfo, nano, result, error);
			bungeeServers.put(serverInfo, info);
			MonitorInfo previous = olympaServers.get(info.getOlympaServer()).put(info.getServerID(), info);
			ServerStatus previousStatus = previous == null ? ServerStatus.CLOSE : previous.getStatus();
			if (previousStatus != info.getStatus()) {
				OlympaBungee.getInstance().getLogger().info("Serveur " + info.getName() + " : " + previousStatus + " -> " + info.getStatus() + (info.getError() != null ? "(" + info.getError() + ")" : ""));
				if (instantUpdate) updateOlympaServer(info.getOlympaServer());
			}
		});
	}
	
	private static void updateOlympaServer(OlympaServer olympaServer) {
		Collection<MonitorInfo> servers = olympaServers.get(olympaServer).values();
		if (servers.isEmpty()) return;
		MonitorInfo upper = servers.stream().sorted((x, y) -> Integer.compare(y.getStatus().getId(), x.getStatus().getId())).findFirst().orElse(null);
		int online = servers.stream().filter(x -> x.getStatus().canConnect()).mapToInt(x -> x.getOnlinePlayers()).sum();
		RedisBungeeSend.sendServerInfos(olympaServer, online, upper == null ? ServerStatus.CLOSE : upper.getStatus());
	}

	public MonitorServers(Plugin plugin) {
		TaskScheduler schuduler = plugin.getProxy().getScheduler();
		schuduler.schedule(plugin, () -> {
			for (ServerInfo serverInfo : ProxyServer.getInstance().getServers().values()) {
				updateServer(serverInfo, false);
			}
			for (OlympaServer olympaServer : olympaServers.keySet()) {
				updateOlympaServer(olympaServer);
			}
		}, 1, 10, TimeUnit.SECONDS);
	}
}
