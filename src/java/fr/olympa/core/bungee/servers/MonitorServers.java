package fr.olympa.core.bungee.servers;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;

public class MonitorServers {

	private static Map<ServerInfo, ServerPing> servers = new HashMap<>();

	public static Map<ServerInfo, ServerPing> getServers() {
		return servers;
	}

	Plugin plugin;

	public MonitorServers(Plugin plugin) {
		this.plugin = plugin;
		plugin.getProxy().getScheduler().schedule(plugin, () -> this.getData(), 0, 10, TimeUnit.SECONDS);
	}

	private void getData() {
		servers.clear();
		this.plugin.getProxy().getScheduler().runAsync(this.plugin, () -> {
			Map<String, ServerInfo> allServers = ProxyServer.getInstance().getServers();
			allServers.values().stream().forEach(server -> server.ping((result, error) -> {
				servers.put(server, result);
			}));
		});
	}
}
