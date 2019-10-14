package fr.tristiisch.olympa.core.datamanagment.redis.listeners;

import java.util.UUID;
import java.util.function.Consumer;

import fr.tristiisch.olympa.OlympaCore;
import fr.tristiisch.olympa.api.provider.AccountProvider;
import redis.clients.jedis.JedisPubSub;

public class OlympaPlayerReceiveListener extends JedisPubSub {

	@Override
	public void onMessage(String channel, String message) {
		UUID uuid = UUID.fromString(message);
		Consumer<? super Boolean> consumer = AccountProvider.modificationReceive.get(uuid);
		if (consumer == null) {
			return;
		}
		consumer.accept(true);
		AccountProvider.modificationReceive.remove(uuid);
		OlympaCore.getTask().cancelTaskByName("waitModifications" + uuid);
	}
}
