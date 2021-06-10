package fr.olympa.core.spigot.redis.receiver;

import redis.clients.jedis.JedisPubSub;

public class BungeeAskSomething extends JedisPubSub {

	@Override
	public void onMessage(String channel, String message) {

	}
}
