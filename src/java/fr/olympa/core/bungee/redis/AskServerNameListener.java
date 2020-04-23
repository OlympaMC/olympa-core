package fr.olympa.core.bungee.redis;

import java.net.InetSocketAddress;

import fr.olympa.core.bungee.servers.MonitorServers;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import redis.clients.jedis.JedisPubSub;

public class AskServerNameListener extends JedisPubSub {

	@Override
	public void onMessage(String channel, String message) {
		String[] info = message.split(":");
		ServerInfo serverInfo = ProxyServer.getInstance().getServers().entrySet().stream().filter(entry -> {
			InetSocketAddress adress = entry.getValue().getAddress();
			return adress.getAddress().getHostAddress().equals(info[0]) && adress.getPort() == Integer.parseInt(info[1]);
		}).map(entry -> entry.getValue()).findFirst().orElse(null);
		if (serverInfo != null) {
			MonitorServers.updateServer(serverInfo);
			RedisBungeeSend.sendServerName(serverInfo);
		}
	}
}
