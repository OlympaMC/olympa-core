package fr.tristiisch.olympa.core.datamanagment.redis.listeners;

import org.bukkit.entity.Player;

import com.google.gson.Gson;

import fr.tristiisch.olympa.api.objects.OlympaPlayer;
import fr.tristiisch.olympa.api.plugin.OlympaPlugin;
import fr.tristiisch.olympa.core.datamanagment.redis.access.Account;
import fr.tristiisch.olympa.core.permission.groups.customevents.AsyncOlympaPlayerChangeGroupEvent;
import fr.tristiisch.olympa.core.permission.groups.customevents.AsyncOlympaPlayerChangeGroupEvent.ChangeType;
import redis.clients.jedis.JedisPubSub;

public class OlympaPlayerListener extends JedisPubSub {

	@Override
	public void onMessage(String channel, String message) {
		OlympaPlayer olympaPlayer = new Gson().fromJson(message, OlympaPlayer.class);

		Player player = olympaPlayer.getPlayer();
		if (player == null) {
			return;
		}
		Account account = new Account(olympaPlayer.getUniqueId());
		OlympaPlugin.getInstance().getServer().getPluginManager().callEvent(new AsyncOlympaPlayerChangeGroupEvent(player, ChangeType.ADD, olympaPlayer, olympaPlayer.getGroup()));
		account.saveToCache(olympaPlayer);
		account.sendModificationsReceive();
	}
}
