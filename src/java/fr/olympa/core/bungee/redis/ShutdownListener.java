package fr.olympa.core.bungee.redis;

import fr.olympa.api.utils.OlympaJedisPubSub;
import fr.olympa.core.bungee.servers.MonitorServers;
import fr.olympa.core.bungee.servers.ServersConnection;
import net.md_5.bungee.api.config.ServerInfo;

public class ShutdownListener extends OlympaJedisPubSub {

	@Override
	public void onMessage(String channel, String message) {
		ServerInfo serverInfo = ServersConnection.getServerByNameOrIpPort(message);
		if (serverInfo != null) {
			MonitorServers.updateServer(serverInfo, true);
		}
	}
}
