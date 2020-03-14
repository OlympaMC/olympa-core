package fr.olympa.core.bungee.servers;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;

public class MonitorServers {
	
	public class Ping {
		ServerPing serverPing;
		int ping;
		Throwable error;

		public Ping(ServerPing serverPing, int ping, Throwable error) {
			this.serverPing = serverPing;
			this.ping = ping;
			this.error = error;
		}

		public Throwable getError() {
			return this.error;
		}
		
		public int getPing() {
			return this.ping;
		}

		public ServerPing getServerPing() {
			return this.serverPing;
		}
	}

	private static Map<ServerInfo, Ping> servers = new HashMap<>();
	
	public static Map<ServerInfo, Ping> getServers() {
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
			for (ServerInfo server : allServers.values()) {
				long nano = System.nanoTime();
				server.ping((result, error) -> {
					servers.put(server, new Ping(result, Math.round((System.nanoTime() - nano) / 1000000), error));
				});
			}
		});
	}
}
