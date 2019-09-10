package fr.tristiisch.olympa.core.datamanagment.redis.listeners;

import java.util.UUID;
import java.util.function.Consumer;

import fr.tristiisch.olympa.api.task.TaskManager;
import fr.tristiisch.olympa.core.datamanagment.redis.access.Account;
import redis.clients.jedis.JedisPubSub;

public class OlympaPlayerReceiveListener extends JedisPubSub {

	@Override
	public void onMessage(String channel, String message) {
		UUID uuid = UUID.fromString(message);
		Consumer<? super Boolean> consumer = Account.modificationReceive.get(uuid);
		if (consumer == null) {
			return;
		}
		consumer.accept(true);
		Account.modificationReceive.remove(uuid);
		TaskManager.cancelTaskByName("waitModifications" + uuid);
	}
}
