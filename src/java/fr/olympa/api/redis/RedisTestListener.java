package fr.olympa.api.redis;

import fr.olympa.core.spigot.OlympaCore;
import redis.clients.jedis.JedisPubSub;

public class RedisTestListener extends JedisPubSub {

	@Override
	public void onMessage(String channel, String message) {
		OlympaCore.getInstance().sendMessage("[DEBUG] Message from Redis : " + message);
	}
}
