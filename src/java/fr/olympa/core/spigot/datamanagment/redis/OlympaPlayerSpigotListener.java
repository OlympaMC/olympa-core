package fr.olympa.core.spigot.datamanagment.redis;

import org.bukkit.entity.Player;

import fr.olympa.api.groups.AsyncOlympaPlayerChangeGroupEvent;
import fr.olympa.api.groups.AsyncOlympaPlayerChangeGroupEvent.ChangeType;
import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.GsonCustomizedObjectTypeAdapter;
import fr.olympa.core.spigot.OlympaCore;
import redis.clients.jedis.JedisPubSub;

public class OlympaPlayerSpigotListener extends JedisPubSub {

	@Override
	public void onMessage(String channel, String message) {
		OlympaPlayer olympaPlayer = GsonCustomizedObjectTypeAdapter.GSON.fromJson(message, OlympaPlayer.class);

		Player player = olympaPlayer.getPlayer();
		if (player == null) {
			return;
		}
		AccountProvider olympaAccount = new AccountProvider(olympaPlayer.getUniqueId());
		OlympaCore.getInstance().getServer().getPluginManager().callEvent(new AsyncOlympaPlayerChangeGroupEvent(player, ChangeType.ADD, olympaPlayer, olympaPlayer.getGroup()));
		olympaAccount.saveToCache(olympaPlayer);
		OlympaCore.getInstance().sendMessage("&c[DEBUG] NEW DATA from Redis for " + olympaPlayer.getName());
	}
}