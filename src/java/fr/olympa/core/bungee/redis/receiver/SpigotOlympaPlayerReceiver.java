package fr.olympa.core.bungee.redis.receiver;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.core.common.provider.AccountProvider;
import fr.olympa.core.common.utils.GsonCustomizedObjectTypeAdapter;
import redis.clients.jedis.JedisPubSub;

public class SpigotOlympaPlayerReceiver extends JedisPubSub {

	@Override
	public void onMessage(String channel, String message) {
		OlympaPlayer olympaPlayer = GsonCustomizedObjectTypeAdapter.GSON.fromJson(message, OlympaPlayer.class);
		new AccountProvider(olympaPlayer.getUniqueId()).saveToCache(olympaPlayer);
		LinkSpigotBungee.getInstance().sendRedis("§eDonnées de §a%s §ereçues", olympaPlayer.getName());
	}
}
