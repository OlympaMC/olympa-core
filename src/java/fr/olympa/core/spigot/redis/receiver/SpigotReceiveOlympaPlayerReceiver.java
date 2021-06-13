package fr.olympa.core.spigot.redis.receiver;

import org.apache.commons.lang.Validate;

import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.common.provider.AccountProvider;
import fr.olympa.api.utils.GsonCustomizedObjectTypeAdapter;
import fr.olympa.core.spigot.OlympaCore;
import redis.clients.jedis.JedisPubSub;

public class SpigotReceiveOlympaPlayerReceiver extends JedisPubSub {

	@Override
	public void onMessage(String channel, String message) {
		String[] args = message.split(";");
		String serverFrom = args[0];
		String serverTo = args[1];
		if (!OlympaCore.getInstance().isServerName(serverTo))
			return;
		OlympaPlayer olympaPlayer = GsonCustomizedObjectTypeAdapter.GSON.fromJson(args[2], OlympaPlayer.class);
		Validate.notNull(olympaPlayer);
		new AccountProvider(olympaPlayer.getUniqueId()).saveToCache(olympaPlayer);
		OlympaCore.getInstance().sendMessage("§7[Redis] §eDonnées de §a" + olympaPlayer.getName() + " §ereçues de §a" + serverFrom);
	}
}
