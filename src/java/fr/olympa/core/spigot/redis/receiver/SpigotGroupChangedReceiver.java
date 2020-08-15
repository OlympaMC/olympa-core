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
		if (from.equals(OlympaCore.getInstance().getServerName())) return;
		OlympaPlayer olympaPlayer = GsonCustomizedObjectTypeAdapter.GSON.fromJson(args[1], OlympaPlayer.class);
		String[] infoGroup = args[2].split(":");
		OlympaGroup groupChanged = OlympaGroup.getById(Integer.parseInt(infoGroup[0]));
		long timestamp = Integer.parseInt(infoGroup[1]);
		ChangeType state = ChangeType.get(Integer.parseInt(args[3]));
		Player player = olympaPlayer.getPlayer();
		if (player == null)
			return;
		AccountProvider olympaAccount = new AccountProvider(olympaPlayer.getUniqueId());
		OlympaCore.getInstance().getServer().getPluginManager().callEvent(new AsyncOlympaPlayerChangeGroupEvent(player, state, olympaPlayer, groupChanged));
		olympaAccount.saveToCache(olympaPlayer);
		RedisSpigotSend.sendModificationsReceive(olympaPlayer.getUniqueId());
		OlympaCore.getInstance().sendMessage("&a[Redis] PLAYER change groupe for " + olympaPlayer.getName() + " from server " + from);
	}
}
