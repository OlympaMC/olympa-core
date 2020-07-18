package fr.olympa.core.spigot.redis;

import org.bukkit.entity.Player;

import fr.olympa.api.customevents.AsyncOlympaPlayerChangeGroupEvent;
import fr.olympa.api.customevents.AsyncOlympaPlayerChangeGroupEvent.ChangeType;
import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.GsonCustomizedObjectTypeAdapter;
import fr.olympa.core.spigot.OlympaCore;
import redis.clients.jedis.JedisPubSub;

public class SpigotGroupChangedReceiver extends JedisPubSub {

	@Override
	public void onMessage(String channel, String message) {
		super.onMessage(channel, message);
		String[] args = message.split(";");
		String from = args[0];
		OlympaGroup groupChanged = OlympaGroup.getById(Integer.parseInt(args[2].split(":")[0]));
		ChangeType changeT = ChangeType.get(Integer.parseInt(args[3]));
		if (OlympaCore.getInstance() == null)
			return;
		OlympaPlayer olympaPlayer = GsonCustomizedObjectTypeAdapter.GSON.fromJson(args[1], OlympaPlayer.class);

		Player player = olympaPlayer.getPlayer();
		if (player == null)
			return;
		AccountProvider olympaAccount = new AccountProvider(olympaPlayer.getUniqueId());
		OlympaCore.getInstance().getServer().getPluginManager().callEvent(new AsyncOlympaPlayerChangeGroupEvent(player, changeT, olympaPlayer, groupChanged));
		olympaAccount.saveToCache(olympaPlayer);
		OlympaCore.getInstance().sendMessage("&a[DEBUG] PLAYER CHANGE GROUPE from Redis for " + olympaPlayer.getName());
		olympaAccount.sendModificationsReceive();
	}
}
