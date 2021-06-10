package fr.olympa.core.bungee.redis.receiver;

import java.util.UUID;

import fr.olympa.core.bungee.redis.RedisBungeeSend;
import fr.olympa.core.bungee.servers.ServersConnection;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import redis.clients.jedis.JedisPubSub;

public class SpigotAskPlayerNameReceiver extends JedisPubSub {

	@Override
	public void onMessage(String channel, String message) {
		super.onMessage(channel, message);
		ServerInfo serverInfo = ServersConnection.getServerByNameOrIpPort(message);
		if (serverInfo == null)
			return;
		String[] args = message.split(";");
		String serverName = args[0];
		String playerUUID = args[1];
		ProxiedPlayer player = ProxyServer.getInstance().getPlayer(UUID.fromString(playerUUID));
		RedisBungeeSend.sendPlayerServer(serverName, playerUUID, player.getServer().getInfo().getName());
	}
}