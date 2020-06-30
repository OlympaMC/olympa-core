package fr.olympa.core.spigot.redis;

import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.GsonCustomizedObjectTypeAdapter;
import fr.olympa.core.spigot.OlympaCore;
import redis.clients.jedis.JedisPubSub;

public class GiveToOlympaPlayerListener extends JedisPubSub {
	
	@Override
	public void onMessage(String channel, String message) {
		String[] args = message.split(";");
		String serverFrom = args[0];
		String serverTo = args[1];
		if (!OlympaCore.getInstance().isServerName(serverTo))
			return;
		OlympaPlayer olympaPlayer = GsonCustomizedObjectTypeAdapter.GSON.fromJson(args[2], OlympaPlayer.class);
		new AccountProvider(olympaPlayer.getUniqueId()).saveToCache(olympaPlayer);
		OlympaCore.getInstance().sendMessage("&a[DEBUG] PLAYER SWITCH from Redis for " + olympaPlayer.getName());
	}
}
