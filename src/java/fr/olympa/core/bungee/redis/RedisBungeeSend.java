package fr.olympa.core.bungee.redis;

import java.net.InetSocketAddress;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.provider.RedisAccess;
import net.md_5.bungee.api.config.ServerInfo;
import redis.clients.jedis.Jedis;

public class RedisBungeeSend {

	public static void giveOlympaPlayer(ServerInfo serverFrom, ServerInfo serverTo) {
		try (Jedis jedis = RedisAccess.INSTANCE.newConnection()) {
			jedis.publish("giveOlympaPlayer", serverFrom.getName() + ";" + serverTo.getName());
		}
		RedisAccess.INSTANCE.disconnect();
	}

	@SuppressWarnings("deprecation")
	public static void sendServerName(ServerInfo serverInfo) {
		LinkSpigotBungee.Provider.link.launchAsync(() -> {
			try (Jedis jedis = RedisAccess.INSTANCE.newConnection()) {
				InetSocketAddress adress = serverInfo.getAddress();
				jedis.publish("sendServerName", adress.getAddress().getHostName() + ":" + adress.getPort() + ":" + serverInfo.getName());
			}
			RedisAccess.INSTANCE.disconnect();
		});
	}
}
