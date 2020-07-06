package fr.olympa.core.spigot.redis;

import java.util.UUID;

import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.OlympaJedisPubSub;
import fr.olympa.core.spigot.OlympaCore;

public class SendOlympaPlayerReceiver extends OlympaJedisPubSub {

	@Override
	public void onMessage(String channel, String message) {
		String[] args = message.split(";");
		String serverFrom = args[0];
		if (!OlympaCore.getInstance().isServerName(serverFrom))
			return;
		String serverTo = args[1];
		OlympaPlayer olympaPlayer = AccountProvider.get(UUID.fromString(args[2]));
		RedisSpigotSend.giveOlympaPlayer(olympaPlayer, serverTo);
	}
}
