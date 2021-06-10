package fr.olympa.core.spigot.redis.receiver;

import java.util.UUID;

import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.common.provider.AccountProvider;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.core.spigot.redis.RedisSpigotSend;
import redis.clients.jedis.JedisPubSub;

public class SpigotOlympaPlayerReceiver extends JedisPubSub {

	@Override
	public void onMessage(String channel, String message) {
		String[] args = message.split(";");
		String serverFrom = args[0];
		if (!OlympaCore.getInstance().isServerName(serverFrom))
			return;
		String serverTo = args[1];
		OlympaPlayer olympaPlayer = AccountProvider.getter().get(UUID.fromString(args[2]));
		if (olympaPlayer != null)
			RedisSpigotSend.sendOlympaPlayerToOtherSpigot(olympaPlayer, serverTo);
	}
}
