package fr.olympa.core.bungee.redis.receiver;

import fr.olympa.api.server.OlympaServer;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.servers.ServersConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import redis.clients.jedis.JedisPubSub;

public class SpigotServerSwitchReceiver extends JedisPubSub {

	@Override
	public void onMessage(String channel, String message) {
		super.onMessage(channel, message);
		String[] args = message.split(":");
		ProxiedPlayer player = OlympaBungee.getInstance().getProxy().getPlayer(args[0]);
		OlympaServer server = OlympaServer.valueOf(args[1]);
		System.out.println(String.format("[REDIS] Demande de serveur switch %s sur le serv %s.", player.getName(), server.getNameCaps()));
		ServersConnection.tryConnect(player, server);
	}

}
