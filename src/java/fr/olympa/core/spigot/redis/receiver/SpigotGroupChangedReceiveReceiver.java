package fr.olympa.core.spigot.redis.receiver;

import java.util.UUID;
import java.util.function.Consumer;

import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.core.spigot.redis.RedisSpigotSend;
import redis.clients.jedis.JedisPubSub;

public class SpigotGroupChangedReceiveReceiver extends JedisPubSub {

	@Override
	public void onMessage(String channel, String message) {
		UUID uuid = UUID.fromString(message);
		Consumer<? super Boolean> consumer = RedisSpigotSend.modificationReceive.get(uuid);
		if (consumer == null)
			return;
		consumer.accept(true);
		RedisSpigotSend.modificationReceive.remove(uuid);
		OlympaCore.getInstance().getTask().cancelTaskByName("waitModifications" + uuid);
	}
}
