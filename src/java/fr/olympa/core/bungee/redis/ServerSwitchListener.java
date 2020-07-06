package fr.olympa.core.bungee.redis;

import fr.olympa.api.server.OlympaServer;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.servers.ServersConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import redis.clients.jedis.JedisPubSub;

public class ServerSwitchListener extends JedisPubSub {

	@Override
	public void onMessage(String channel, String message) {
		String[] args = message.split(":");
		ProxiedPlayer player = OlympaBungee.getInstance().getProxy().getPlayer(args[0]);
		OlympaServer server = OlympaServer.valueOf(args[1]);
		ServersConnection.tryConnect(player, server);
	}

}