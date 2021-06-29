package fr.olympa.core.bungee.servers;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableMap;

import fr.olympa.api.bungee.task.BungeeTaskManager;
import fr.olympa.api.common.module.OlympaModule;
import fr.olympa.api.common.redis.RedisClass;
import fr.olympa.api.common.server.OlympaServer;
import fr.olympa.api.common.server.ServerInfoAdvanced;
import fr.olympa.api.common.server.ServerInfoAdvancedBungee;
import fr.olympa.api.common.server.ServerStatus;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.redis.receiver.SpigotAskMonitorInfoReceiver;
import io.netty.handler.timeout.ReadTimeoutException;
import net.md_5.bungee.api.config.ServerInfo;

public class MonitorServers {

	private static Map<OlympaServer, Map<Integer, ServerInfoAdvancedBungee>> olympaServers = Arrays.stream(OlympaServer.values()).collect(ImmutableMap.toImmutableMap(x -> x, x -> new HashMap<>()));
	private static Map<ServerInfo, ServerInfoAdvancedBungee> bungeeServers = new ConcurrentHashMap<>();

	public static Map<Integer, ServerInfoAdvancedBungee> getServers(OlympaServer server) {
		return olympaServers.get(server);
	}

	public static Map<OlympaServer, Map<Integer, ServerInfoAdvancedBungee>> getServersByType() {
		return olympaServers;
	}

	public static ServerInfoAdvancedBungee getMonitor(ServerInfo server) {
		return bungeeServers.get(server);
	}

	public static Collection<ServerInfoAdvancedBungee> getServers() {
		return bungeeServers.values();
	}

	public static Stream<ServerInfoAdvancedBungee> getServersSorted() {
		return bungeeServers.values().stream().sorted((o1, o2) -> {
			int i = Integer.compare(o1.getOlympaServer().ordinal(), o2.getOlympaServer().ordinal());
			if (i == 0 && o2.getOnlinePlayers() != null && o1.getOnlinePlayers() != null)
				i = Integer.compare(o1.getStatus().getId(), o2.getStatus().getId());
			if (i == 0 && o2.getOnlinePlayers() != null && o1.getOnlinePlayers() != null)
				i = Integer.compare(o2.getOnlinePlayers(), o1.getOnlinePlayers());
			if (i == 0)
				i = Integer.compare(o2.getPing(), o1.getPing());
			return i;
		});
	}

	public static Map<ServerInfo, ServerInfoAdvancedBungee> getServersMap() {
		return bungeeServers;
	}

	public static void updateServer(ServerInfo serverInfo, boolean instantUpdate) {
		updateServer(serverInfo, instantUpdate, null);
	}

	public static void updateServer(ServerInfo serverInfo, boolean instantUpdate, Consumer<ServerInfo> sucess) {
		long nano = System.nanoTime();
		serverInfo.ping((result, error) -> {
			ServerInfoAdvancedBungee oldMonitor = getMonitor(serverInfo);
			if (OlympaModule.DEBUG)
				OlympaBungee.getInstance().sendMessage("&7Debug Serveur &e%s&7 a été ping.", serverInfo.getName());
			if (oldMonitor != null && error instanceof ReadTimeoutException && ServerInfoAdvancedBungee.addReadTimeException(serverInfo))
				return;
			ServerInfoAdvancedBungee.removeReadTimeException(serverInfo);
			ServerInfoAdvancedBungee info = ServerInfoAdvancedBungee.getFromPingServer(serverInfo, nano, result, error);
			bungeeServers.put(serverInfo, info);
			olympaServers.get(info.getOlympaServer()).put(info.getServerId(), info);
			ServerStatus previousStatus = oldMonitor == null ? ServerStatus.UNKNOWN : oldMonitor.getStatus();
			if (previousStatus != info.getStatus()) {
				OlympaBungee.getInstance().sendMessage("§7Serveur §e" + info.getName() + "§7 : " + previousStatus.getNameColored()
						+ " §7-> " + info.getStatus().getNameColored() + (info.getError() != null ? " (" + info.getError() + ")" : ""));
				if (instantUpdate)
					RedisClass.SERVER_INFO_ADVANCED.sendServerInfos(Arrays.asList(info));
			}
			if (info.isOpen())
				ServersConnection.waitToStart.remove(serverInfo);
			if (sucess != null)
				sucess.accept(serverInfo);
		});
	}

	public static void updateServer(ServerInfo serverInfo, OlympaServer olympaServer, ServerInfoAdvancedBungee info) {
		bungeeServers.put(serverInfo, info);
	}

	public static void init(OlympaBungee plugin) {
		new MonitorServers(plugin);
	}

	private MonitorServers(OlympaBungee plugin) {
		BungeeTaskManager task = plugin.getTask();
		task.scheduleSyncRepeatingTask("monitor_serveurs", () -> {
			for (ServerInfo serverInfo : plugin.getProxy().getServersCopy().values())
				updateServer(serverInfo, false);
			if (Utils.getCurrentTimeInSeconds() - SpigotAskMonitorInfoReceiver.lastTimeAsk > 30)
				RedisClass.SERVER_INFO_ADVANCED.sendServerInfos(bungeeServers.values().stream().map(mib -> (ServerInfoAdvanced) mib).collect(Collectors.toList()));
		}, 1, 20, TimeUnit.SECONDS);
	}
}
