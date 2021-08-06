package fr.olympa.core.spigot.redis.receiver;

import org.bukkit.Bukkit;

import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.core.common.provider.AccountProvider;
import fr.olympa.core.common.utils.GsonCustomizedObjectTypeAdapter;
import fr.olympa.core.spigot.OlympaCore;
import redis.clients.jedis.JedisPubSub;

public class BungeeSendOlympaPlayerReceiver extends JedisPubSub {

	@Override
	public void onMessage(String channel, String message) {
		super.onMessage(channel, message);
		String[] args = message.split(";");
		String playerName = args[0];
		if (Bukkit.getPlayer(playerName) == null)
			return;
		OlympaPlayer olympaPlayer = GsonCustomizedObjectTypeAdapter.GSON.fromJson(args[1], OlympaPlayer.class);
		new AccountProvider(olympaPlayer.getUniqueId()).saveToCache(olympaPlayer);
		OlympaCore.getInstance().sendRedis("Données reçues de §a", olympaPlayer.getName());
	}
}
