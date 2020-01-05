package fr.olympa.core.datamanagment.redis.listeners;

import org.bukkit.entity.Player;

import com.google.gson.Gson;

import fr.olympa.OlympaCore;
import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.core.groups.AsyncOlympaPlayerChangeGroupEvent;
import fr.olympa.core.groups.AsyncOlympaPlayerChangeGroupEvent.ChangeType;
import redis.clients.jedis.JedisPubSub;

public class OlympaPlayerListener extends JedisPubSub {

	@Override
	public void onMessage(String channel, String message) {
		OlympaPlayer olympaPlayer = new Gson().fromJson(message, OlympaPlayer.class);

		Player player = olympaPlayer.getPlayer();
		if (player == null) {
			return;
		}
		AccountProvider olympaAccount = new AccountProvider(olympaPlayer.getUniqueId());
		OlympaCore.getInstance().getServer().getPluginManager().callEvent(new AsyncOlympaPlayerChangeGroupEvent(player, ChangeType.ADD, olympaPlayer, olympaPlayer.getGroup()));
		olympaAccount.saveToCache(olympaPlayer);
		// olympaAccount.sendModificationsReceive();
	}
}
