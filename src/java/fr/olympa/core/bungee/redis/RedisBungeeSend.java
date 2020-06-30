package fr.olympa.core.bungee.redis;

import java.net.InetSocketAddress;
import java.util.UUID;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.RedisAccess;
import fr.olympa.api.server.OlympaServer;
import fr.olympa.api.server.ServerStatus;
import net.md_5.bungee.api.config.ServerInfo;
import redis.clients.jedis.Jedis;

public class RedisBungeeSend {
	
	public static void askGiveOlympaPlayer(ServerInfo serverFrom, ServerInfo serverTo, UUID uuid) {
		try (Jedis jedis = RedisAccess.INSTANCE.newConnection()) {
			jedis.publish("askGiveOlympaPlayer", serverFrom.getName() + ";" + serverTo.getName() + ";" + uuid.toString());
		}
		RedisAccess.INSTANCE.disconnect();
	}

	public static void sendOlympaPlayer(ServerInfo target, OlympaPlayer olympaPlayer) {
		try (Jedis jedis = RedisAccess.INSTANCE.newConnection()) {
			jedis.publish("olympaPlayer", target.getName() + ";" + olympaPlayer);
		}
		RedisAccess.INSTANCE.disconnect();
	}

	@SuppressWarnings("deprecation")
	public static void sendServerName(ServerInfo serverInfo) {
		LinkSpigotBungee.Provider.link.launchAsync(() -> {
			try (Jedis jedis = RedisAccess.INSTANCE.newConnection()) {
				InetSocketAddress adress = serverInfo.getAddress();
				jedis.publish("serverName", adress.getAddress().getHostAddress() + ";" + adress.getPort() + ";" + serverInfo.getName());
			}
			RedisAccess.INSTANCE.disconnect();
		});
	}
	
	public static void sendServerInfos(OlympaServer olympaServer, int players, ServerStatus status) {
		try (Jedis jedis = RedisAccess.INSTANCE.newConnection()) {
			jedis.publish("sendServersInfos", olympaServer.name() + ":" + players + ":" + status.getId());
		}
	}
}
