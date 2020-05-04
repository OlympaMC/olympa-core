package fr.olympa.core.bungee.servers;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;

public class MonitorServers {

	private static Cache<Integer, Set<MonitorInfo>> serversInfo = CacheBuilder.newBuilder().maximumSize(60).build();

	public static MonitorInfo get(String name) {
		return getLastServerInfo().stream().filter(si -> si.getName().startsWith(name.toLowerCase())).findFirst().orElse(null);
	}

	public static Entry<Integer, Set<MonitorInfo>> getLastInfo() {
		ConcurrentMap<Integer, Set<MonitorInfo>> all = serversInfo.asMap();
		Iterator<Entry<Integer, Set<MonitorInfo>>> iterator = all.entrySet().iterator();
		Entry<Integer, Set<MonitorInfo>> entry = null;
		while (iterator.hasNext()) {
			entry = iterator.next();
		}
		return entry;
	}

	public static Set<MonitorInfo> getLastServerInfo() {
		Entry<Integer, Set<MonitorInfo>> entry = getLastInfo();
		if (entry == null) {
			return null;
		}
		return entry.getValue();
	}

	public static ConcurrentMap<Integer, Set<MonitorInfo>> getServerInfo() {
		return serversInfo.asMap();
	}

	public static boolean isServerOpen(String name) {
		MonitorInfo serv = get(name);
		return serv != null && serv.isOpen();
	}

	public static void updateServer(ServerInfo serverInfo) {
		Entry<Integer, Set<MonitorInfo>> entry = getLastInfo();
		Set<MonitorInfo> servs = getLastServerInfo();
		long nano = System.nanoTime();
		serverInfo.ping((result, error) -> {
			servs.removeIf(e -> e.serverName.equals(serverInfo.getName()));
			servs.add(new MonitorInfo(serverInfo, nano, result, error));
			serversInfo.put(entry.getKey(), servs);
		});
	}

	public MonitorServers(Plugin plugin) {
		getData(plugin, 1);
	}

	private void getData(Plugin plugin, int i) {
		plugin.getProxy().getScheduler().runAsync(plugin, () -> {
			Map<String, ServerInfo> allServers = ProxyServer.getInstance().getServers();
			Set<MonitorInfo> serversList = new HashSet<>();
			for (ServerInfo serverInfo : allServers.values()) {
				long nano = System.nanoTime();
				serverInfo.ping((result, error) -> {
					serversList.add(new MonitorInfo(serverInfo, nano, result, error));
				});
			}
			serversInfo.put(i, serversList);
			plugin.getProxy().getScheduler().schedule(plugin, () -> getData(plugin, i + 1), 10, TimeUnit.SECONDS);
		});
	}
}
