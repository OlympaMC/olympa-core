package fr.olympa.core.spigot.redis.receiver;

import redis.clients.jedis.JedisPubSub;

public class SpigotSendOlympaPlayerReceiver extends JedisPubSub {

	@Override
	public void onMessage(String channel, String message) {
		System.out.println("debug");
		//		String[] args = message.split(";");
		//		String serverFrom = args[0];
		//		if (!OlympaCore.getInstance().isServerName(serverFrom))
		//			return;
		//		String serverTo = args[1];
		//		OlympaPlayer olympaPlayer = AccountProvider.get(UUID.fromString(args[2]));
		//		Validate.notNull(olympaPlayer);
		//		RedisSpigotSend.sendOlympaPlayerToOtherSpigot(olympaPlayer, serverTo);
	}
}
