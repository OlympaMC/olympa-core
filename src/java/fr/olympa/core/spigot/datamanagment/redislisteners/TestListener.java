package fr.olympa.core.spigot.datamanagment.redislisteners;

import fr.olympa.core.spigot.OlympaCore;
import redis.clients.jedis.JedisPubSub;

public class TestListener extends JedisPubSub {

	@Override
	public void onMessage(String channel, String message) {
		OlympaCore.getInstance().sendMessage("Message Test from Redis : " + message);
	}
}
