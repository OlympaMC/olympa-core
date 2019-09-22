package fr.tristiisch.olympa.core.datamanagment.redis.listeners;

import org.bukkit.entity.Player;

import com.google.gson.Gson;

import fr.tristiisch.olympa.api.objects.OlympaPlayer;
import fr.tristiisch.olympa.api.permission.OlympaAccount;
import fr.tristiisch.olympa.api.plugin.OlympaPlugin;
import fr.tristiisch.olympa.core.datamanagment.redis.access.OlympaAccountObject;
import fr.tristiisch.olympa.core.groups.AsyncOlympaPlayerChangeGroupEvent;
import fr.tristiisch.olympa.core.groups.AsyncOlympaPlayerChangeGroupEvent.ChangeType;
import redis.clients.jedis.JedisPubSub;

public class OlympaPlayerListener extends JedisPubSub {

	@Override
	public void onMessage(String channel, String message) {
		OlympaPlayer olympaPlayer = new Gson().fromJson(message, OlympaPlayer.class);

		Player player = olympaPlayer.getPlayer();
		if (player == null) {
			return;
		}
		OlympaAccount olympaAccount = new OlympaAccountObject(olympaPlayer.getUniqueId());
		OlympaPlugin.getInstance().getServer().getPluginManager().callEvent(new AsyncOlympaPlayerChangeGroupEvent(player, ChangeType.ADD, olympaPlayer, olympaPlayer.getGroup()));
		olympaAccount.saveToCache(olympaPlayer);
		olympaAccount.sendModificationsReceive();
	}
}
