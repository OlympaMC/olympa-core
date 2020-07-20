package fr.olympa.core.spigot.redis.receiver;

import java.util.UUID;

import org.apache.commons.lang.Validate;

import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.core.spigot.redis.RedisSpigotSend;
import redis.clients.jedis.JedisPubSub;

public class SpigotSendOlympaPlayerReceiver extends JedisPubSub {

	@Override
	public void onMessage(String channel, String message) {
		super.onMessage(channel, message);
		String[] args = message.split(";");
		String serverFrom = args[0];
		if (!OlympaCore.getInstance().isServerName(serverFrom))
			return;
		String serverTo = args[1];
		OlympaPlayer olympaPlayer = AccountProvider.get(UUID.fromString(args[2]));
		Validate.notNull(olympaPlayer);
		RedisSpigotSend.sendOlympaPlayerToOtherSpigot(olympaPlayer, serverTo);
	}
}