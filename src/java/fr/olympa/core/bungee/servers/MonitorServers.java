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
import fr.olympa.core.bungee.api.task.BungeeTask;
import fr.olympa.core.bungee.redis.RedisBungeeSend;
import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue.Consumer;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ServerInfo;

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
		updateServer(serverInfo, instantUpdate, null);
	}

	public static void updateServer(ServerInfo serverInfo, boolean instantUpdate, Consumer<ServerInfo> sucess) {
		serverInfo.ping(new Callback<ServerPing>() {
			private long nano = System.nanoTime();

			@Override
			public void done(ServerPing result, Throwable error) {
				MonitorInfo info = new MonitorInfo(serverInfo, nano, result, error);
				bungeeServers.put(serverInfo, info);
				MonitorInfo previous = olympaServers.get(info.getOlympaServer()).put(info.getServerID(), info);
				ServerStatus previousStatus = previous == null ? ServerStatus.UNKNOWN : previous.getStatus();
				if (previousStatus != info.getStatus()) {
					OlympaBungee.getInstance()
							.sendMessage("§7Serveur §e" + info.getName() + "§7 : " + previousStatus.getNameColored() + " §7-> " + info.getStatus().getNameColored() + (info.getError() != null ? " (" + info.getError() + ")" : ""));
					if (instantUpdate)
						updateOlympaServer(info.getOlympaServer());
				}
				if (sucess != null)
					sucess.accept(serverInfo);
			}
		});
	}

	public static void updateServer(ServerInfo serverInfo, OlympaServer olympaServer, MonitorInfo info) {
		bungeeServers.put(serverInfo, info);
		updateOlympaServer(olympaServer);
	}

	private static void updateOlympaServer(OlympaServer olympaServer) {
		Collection<MonitorInfo> servers = olympaServers.get(olympaServer).values();
		if (servers.isEmpty())
			return;
		MonitorInfo upper = servers.stream().sorted((x, y) -> Integer.compare(x.getStatus().getId(), y.getStatus().getId())).findFirst().orElse(null);
		int online = servers.stream().filter(x -> x.getStatus().canConnect()).mapToInt(x -> x.getOnlinePlayers()).sum();
		RedisBungeeSend.sendServerInfos(olympaServer, online, upper == null ? ServerStatus.UNKNOWN : upper.getStatus());
	}

	private void updateAllServeurs(BungeeTask task) {
		for (ServerInfo serverInfo : ProxyServer.getInstance().getServers().values())
			updateServer(serverInfo, false);
		for (OlympaServer olympaServer : olympaServers.keySet())
			updateOlympaServer(olympaServer);
		task.runTaskLater(() -> updateAllServeurs(task), 10, TimeUnit.SECONDS);
	}

	public MonitorServers(OlympaBungee plugin) {
		BungeeTask task = plugin.getTask();
		task.runTaskAsynchronously(() -> updateAllServeurs(task));
	}
}
