package fr.olympa.api.utils;

import fr.olympa.api.LinkSpigotBungee;
import redis.clients.jedis.JedisPubSub;

public abstract class OlympaJedisPubSub extends JedisPubSub {
	
	public static boolean redisMode = false;
	
	@Override
	public void onMessage(String channel, String message) {
		super.onMessage(channel, message);
		if (redisMode) LinkSpigotBungee.Provider.link.sendMessage("§c§lRedis §e" + channel + "§7 : §f" + message);
	}
	
}
