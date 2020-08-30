package fr.olympa.core.spigot.redis.receiver;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import fr.olympa.core.spigot.OlympaCore;
import redis.clients.jedis.JedisPubSub;

public class BungeeAskPlayerServer extends JedisPubSub {

	public static Cache<UUID, Consumer<String>> data = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).build();

	@Override
	public void onMessage(String channel, String message) {
		super.onMessage(channel, message);
		String[] args = message.split(";");
		String serverName = args[0];
		if (!serverName.equals(OlympaCore.getInstance().getServerName()))
			return;
		UUID playerUUID = UUID.fromString(args[1]);
		String playerServer = args[2];
		Consumer<String> consumer;
		if ((consumer = data.getIfPresent(playerUUID)) != null) {
			consumer.accept(playerServer);
			data.invalidate(playerUUID);
		}
	}
}
