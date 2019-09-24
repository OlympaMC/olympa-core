package fr.tristiisch.olympa.core.datamanagment.redis.listeners;

import fr.tristiisch.olympa.api.plugin.OlympaPlugin;
import redis.clients.jedis.JedisPubSub;

public class TestListener extends JedisPubSub {

	@Override
	public void onMessage(String channel, String message) {
		OlympaPlugin.getInstance().sendMessage("Message Test from Redis : " + message);
	}
}
