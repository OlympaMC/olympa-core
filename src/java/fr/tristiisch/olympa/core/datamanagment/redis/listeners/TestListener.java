package fr.tristiisch.olympa.core.datamanagment.redis.listeners;

import fr.tristiisch.olympa.OlympaCore;
import redis.clients.jedis.JedisPubSub;

public class TestListener extends JedisPubSub {

	@Override
	public void onMessage(String channel, String message) {
		OlympaCore.getInstance().sendMessage("Message Test from Redis : " + message);
	}
}
