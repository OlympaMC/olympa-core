package fr.olympa.core.spigot.redis;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.provider.RedisAccess;
import fr.olympa.core.spigot.OlympaCore;
import redis.clients.jedis.Jedis;

public class RedisSpigotSend {

	public static void askServerName() {
		LinkSpigotBungee.Provider.link.launchAsync(() -> {
			try (Jedis jedis = RedisAccess.INSTANCE.newConnection()) {
				String serverName = OlympaCore.getInstance().getServerName();
				long l = jedis.publish("askServerName", serverName);
				if (l == 0) {
					OlympaCore.getInstance().getTask().runTaskLater("askServerName", () -> RedisSpigotSend.askServerName(), 5 * 20);
				}
			}
			RedisAccess.INSTANCE.disconnect();
		});
	}

	public static void sendShutdown() {
		try (Jedis jedis = RedisAccess.INSTANCE.newConnection()) {
			String serverName = OlympaCore.getInstance().getServerName();
			jedis.publish("shutdown", serverName);
		}
		RedisAccess.INSTANCE.disconnect();
	}
}
