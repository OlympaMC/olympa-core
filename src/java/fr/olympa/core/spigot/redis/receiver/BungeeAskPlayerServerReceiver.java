package fr.olympa.core.spigot.redis.receiver;

import java.util.UUID;
import java.util.function.Consumer;

import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.core.spigot.redis.RedisSpigotSend;
import redis.clients.jedis.JedisPubSub;

public class BungeeAskPlayerServerReceiver extends JedisPubSub {

	@Override
	public void onMessage(String channel, String message) {
		String[] args = message.split(";");
		String serverFrom = args[0];
		if (!OlympaCore.getInstance().isServerName(serverFrom))
			return;
		UUID playerUUID = UUID.fromString(args[1]);
		String playerServer = args[2];
		Consumer<String> consumer;
		if ((consumer = RedisSpigotSend.askPlayerServer.getIfPresent(playerUUID)) != null) {
			consumer.accept(playerServer);
			RedisSpigotSend.askPlayerServer.invalidate(playerUUID);
		}
	}
}
