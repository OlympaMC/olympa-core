package fr.olympa.core.spigot.redis.receiver;

import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;

import fr.olympa.api.LinkSpigotBungee;
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
		Player player = olympaPlayer.getPlayer();
		if (player == null || !player.isOnline())
			LinkSpigotBungee.Provider.link.sendMessage("§7[Redis] §eDonnées de §a%s §ereçues de §a%s, mais il n'est pas connecté", olympaPlayer.getName(), serverFrom);
		else {
			new AccountProvider(olympaPlayer.getUniqueId()).saveToCache(olympaPlayer);
			LinkSpigotBungee.Provider.link.sendMessage("§7[Redis] §eDonnées de §a%s §ereçues de §a%s", olympaPlayer.getName(), serverFrom);
		}
	}
}
