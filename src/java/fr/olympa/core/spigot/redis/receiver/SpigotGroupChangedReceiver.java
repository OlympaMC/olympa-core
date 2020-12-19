package fr.olympa.core.spigot.redis.receiver;

import org.bukkit.entity.Player;

import fr.olympa.api.customevents.AsyncOlympaPlayerChangeGroupEvent;
import fr.olympa.api.customevents.AsyncOlympaPlayerChangeGroupEvent.ChangeType;
import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.GsonCustomizedObjectTypeAdapter;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.core.spigot.redis.RedisSpigotSend;
import redis.clients.jedis.JedisPubSub;

public class SpigotGroupChangedReceiver extends JedisPubSub {

	@Override
	public void onMessage(String channel, String message) {
		String[] args = message.split(";");
		String from = args[0];
		if (from.equals(OlympaCore.getInstance().getServerName()))
			return;
		OlympaPlayer newOlympaPlayer = GsonCustomizedObjectTypeAdapter.GSON.fromJson(args[1], OlympaPlayer.class);
		Player player = newOlympaPlayer.getPlayer();
		if (player == null)
			return;
		String[] infoGroup = args[2].split(":");
		OlympaGroup groupChanged = OlympaGroup.getById(Integer.parseInt(infoGroup[0]));
		long timestamp = Integer.parseInt(infoGroup[1]);
		ChangeType state = ChangeType.get(Integer.parseInt(args[3]));
		AccountProvider olympaAccount = new AccountProvider(newOlympaPlayer.getUniqueId());
		OlympaPlayer oldOlympaPlayer = olympaAccount.getFromCache();
		oldOlympaPlayer.getGroups().clear();
		oldOlympaPlayer.getGroups().putAll(newOlympaPlayer.getGroups());
		olympaAccount.saveToCache(oldOlympaPlayer);
		olympaAccount.saveToRedis(oldOlympaPlayer);
		//olympaAccount.saveToDb(oldOlympaPlayer);

		OlympaCore.getInstance().getServer().getPluginManager().callEvent(new AsyncOlympaPlayerChangeGroupEvent(player, state, newOlympaPlayer, null, timestamp, groupChanged));
		RedisSpigotSend.sendModificationsReceive(newOlympaPlayer.getUniqueId());
		OlympaCore.getInstance().sendMessage("&a[Redis] PLAYER change groupe for " + newOlympaPlayer.getName() + " to " + oldOlympaPlayer.getGroupsToHumainString() + " from server " + from);
	}
}
