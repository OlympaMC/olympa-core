package fr.olympa.core.spigot.redis;

import org.bukkit.Server;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.provider.RedisAccess;
import fr.olympa.core.spigot.OlympaCore;
import redis.clients.jedis.Jedis;

public class RedisSpigotSend {

	public static void askServerName() {
		LinkSpigotBungee.Provider.link.launchAsync(() -> {
			try (Jedis jedis = RedisAccess.INSTANCE.newConnection()) {
				Server server = OlympaCore.getInstance().getServer();
				Long l = jedis.publish("askServerName", server.getIp() + ":" + server.getPort());
				if (l == 0) {
					OlympaCore.getInstance().getTask().runTaskLater("askServerName", () -> RedisSpigotSend.askServerName(), 5 * 20);
				}
			}
			RedisAccess.INSTANCE.disconnect();
		});
	}
}
