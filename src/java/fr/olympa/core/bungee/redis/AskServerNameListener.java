package fr.olympa.core.bungee.redis;

import fr.olympa.core.bungee.servers.MonitorServers;
import fr.olympa.core.bungee.servers.ServersConnection;
import net.md_5.bungee.api.config.ServerInfo;
import redis.clients.jedis.JedisPubSub;

public class AskServerNameListener extends JedisPubSub {

	@Override
	public void onMessage(String channel, String message) {
		super.onMessage(channel, message);
		ServerInfo serverInfo = ServersConnection.getServerByNameOrIpPort(message);
		if (serverInfo != null) {
			MonitorServers.updateServer(serverInfo, true);
			RedisBungeeSend.sendServerName(serverInfo);
		}
	}
}
