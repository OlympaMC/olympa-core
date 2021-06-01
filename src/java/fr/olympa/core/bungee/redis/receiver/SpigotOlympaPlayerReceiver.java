package fr.olympa.core.bungee.redis.receiver;

import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.commun.provider.AccountProvider;
import fr.olympa.api.utils.GsonCustomizedObjectTypeAdapter;
import fr.olympa.core.bungee.OlympaBungee;
import redis.clients.jedis.JedisPubSub;

public class SpigotOlympaPlayerReceiver extends JedisPubSub {

	@Override
	public void onMessage(String channel, String message) {
		OlympaPlayer olympaPlayer = GsonCustomizedObjectTypeAdapter.GSON.fromJson(message, OlympaPlayer.class);
		new AccountProvider(olympaPlayer.getUniqueId()).saveToCache(olympaPlayer);
		OlympaBungee.getInstance().sendMessage("&a[DEBUG] RECEIVE PLAYER FROM SPIGOT " + olympaPlayer.getName());
	}
}
