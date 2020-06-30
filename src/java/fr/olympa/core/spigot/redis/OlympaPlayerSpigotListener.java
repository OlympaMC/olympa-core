package fr.olympa.core.spigot.redis;

import org.bukkit.entity.Player;

import fr.olympa.api.customevents.AsyncOlympaPlayerChangeGroupEvent;
import fr.olympa.api.customevents.AsyncOlympaPlayerChangeGroupEvent.ChangeType;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.GsonCustomizedObjectTypeAdapter;
import fr.olympa.core.spigot.OlympaCore;
import redis.clients.jedis.JedisPubSub;

public class OlympaPlayerSpigotListener extends JedisPubSub {

	@Override
	public void onMessage(String channel, String message) {
		String[] args = message.split(";");
		if (!OlympaCore.getInstance().isServerName(args[0]))
			return;
		OlympaPlayer olympaPlayer = GsonCustomizedObjectTypeAdapter.GSON.fromJson(args[1], OlympaPlayer.class);

		Player player = olympaPlayer.getPlayer();
		if (player == null)
			return;
		AccountProvider olympaAccount = new AccountProvider(olympaPlayer.getUniqueId());
		OlympaCore.getInstance().getServer().getPluginManager().callEvent(new AsyncOlympaPlayerChangeGroupEvent(player, ChangeType.ADD, olympaPlayer, olympaPlayer.getGroup()));
		olympaAccount.saveToCache(olympaPlayer);
		OlympaCore.getInstance().sendMessage("&a[DEBUG] PLAYER CHANGE GROUPE from Redis for " + olympaPlayer.getName());
		olympaAccount.sendModificationsReceive();
	}
}
