package fr.olympa.api.redis;

import redis.clients.jedis.JedisPubSub;

public class RedisTestListener extends JedisPubSub {

	@Override
	public void onMessage(String channel, String message) {
		System.out.println("[DEBUG] Message from Redis : " + message);
	}
}
