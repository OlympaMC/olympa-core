package fr.olympa.core.bungee.servers;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableMap;

import fr.olympa.api.bungee.task.BungeeTaskManager;
import fr.olympa.api.common.module.OlympaModule;
import fr.olympa.api.common.server.OlympaServer;
import fr.olympa.api.common.server.ServerStatus;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.redis.RedisBungeeSend;
import fr.olympa.core.bungee.redis.receiver.SpigotAskMonitorInfoReceiver;
import net.md_5.bungee.api.config.ServerInfo;

public class MonitorServers {

	private static Map<OlympaServer, Map<Integer, MonitorInfoBungee>> olympaServers = Arrays.stream(OlympaServer.values()).collect(ImmutableMap.toImmutableMap(x -> x, x -> new HashMap<>()));
	private static Map<ServerInfo, MonitorInfoBungee> bungeeServers = new ConcurrentHashMap<>();

	public static Map<Integer, MonitorInfoBungee> getServers(OlympaServer server) {
		return olympaServers.get(server);
	}

	public static MonitorInfoBungee getMonitor(ServerInfo server) {
		return bungeeServers.get(server);
	}

	public static Collection<MonitorInfoBungee> getServers() {
		return bungeeServers.values();
	}

	public static Stream<MonitorInfoBungee> getServersSorted() {
		return bungeeServers.values().stream().sorted((o1, o2) -> {
			int i = Integer.compare(o1.getStatus().getId(), o2.getStatus().getId());
			if (i == 0 && o2.getOnlinePlayers() != null && o1.getOnlinePlayers() != null)
				i = Integer.compare(o2.getOnlinePlayers(), o1.getOnlinePlayers());
			if (i == 0)
				i = Integer.compare(o2.getPing(), o1.getPing());
			return i;
		});
	}

	public static Map<ServerInfo, MonitorInfoBungee> getServersMap() {
		return bungeeServers;
	}

	public static void updateServer(ServerInfo serverInfo, boolean instantUpdate) {
		updateServer(serverInfo, instantUpdate, null);
	}

	public static void updateServer(ServerInfo serverInfo, boolean instantUpdate, Consumer<ServerInfo> sucess) {
		long nano = System.nanoTime();
		serverInfo.ping((result, error) -> {
			if (OlympaModule.DEBUG)
				OlympaBungee.getInstance().sendMessage("&eDebug §7Serveur §e" + serverInfo.getName() + " a été ping.");
			MonitorInfoBungee info = new MonitorInfoBungee(serverInfo, nano, result, error);
			bungeeServers.put(serverInfo, info);
			MonitorInfoBungee previous = olympaServers.get(info.getOlympaServer()).put(info.getServerID(), info);
			ServerStatus previousStatus = previous == null ? ServerStatus.UNKNOWN : previous.getStatus();
			if (previousStatus != info.getStatus()) {
				OlympaBungee.getInstance().sendMessage("§7Serveur §e" + info.getName() + "§7 : " + previousStatus.getNameColored()
						+ " §7-> " + info.getStatus().getNameColored() + (info.getError() != null ? " (" + info.getError() + ")" : ""));
				if (instantUpdate)
					//					updateOlympaServer(info.getOlympaServer());
					RedisBungeeSend.sendServerInfos();
			}
			if (info.isOpen())
				ServersConnection.waitToStart.remove(serverInfo);
			if (sucess != null)
				sucess.accept(serverInfo);
		});
	}

	public static void updateServer(ServerInfo serverInfo, OlympaServer olympaServer, MonitorInfoBungee info) {
		bungeeServers.put(serverInfo, info);
		//		updateOlympaServer(olympaServer);
	}

	//	private static void updateOlympaServer(OlympaServer olympaServer) {
	//		Collection<MonitorInfoBungee> servers = olympaServers.get(olympaServer).values();
	//		if (servers.isEmpty())
	//			return;
	//		MonitorInfoBungee upper = servers.stream().sorted((x, y) -> Integer.compare(x.getStatus().getId(), y.getStatus().getId())).findFirst().orElse(null);
	//		int online = servers.stream().filter(x -> x.getStatus().canConnect()).mapToInt(x -> x.getOnlinePlayers()).sum();
	//		RedisBungeeSend.sendServerInfos(olympaServer, online, upper == null ? ServerStatus.UNKNOWN : upper.getStatus());
	//	}

	public static void init(OlympaBungee plugin) {
		new MonitorServers(plugin);
	}

	private MonitorServers(OlympaBungee plugin) {
		BungeeTaskManager task = plugin.getTask();
		task.scheduleSyncRepeatingTask("monitor_serveurs", () -> {
			for (ServerInfo serverInfo : plugin.getProxy().getServersCopy().values())
				updateServer(serverInfo, false);
			if (Utils.getCurrentTimeInSeconds() - SpigotAskMonitorInfoReceiver.lastTimeAsk > 30)
				RedisBungeeSend.sendServerInfos(bungeeServers.values());
			//			for (OlympaServer olympaServer : olympaServers.keySet())
			//				updateOlympaServer(olympaServer);
		}, 1, 15, TimeUnit.SECONDS);
	}
}
