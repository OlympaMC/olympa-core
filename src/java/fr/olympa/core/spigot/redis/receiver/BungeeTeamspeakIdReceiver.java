package fr.olympa.core.spigot.redis.receiver;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import fr.olympa.api.match.RegexMatcher;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import redis.clients.jedis.JedisPubSub;

public class BungeeTeamspeakIdReceiver extends JedisPubSub {

	@Override
	public void onMessage(String channel, String message) {
		String[] info = message.split(";");
		Player player = Bukkit.getPlayer((UUID) RegexMatcher.UUID.parse(info[0]));
		if (player == null || info.length < 2)
			return;
		AccountProvider account = new AccountProvider(player.getUniqueId());
		OlympaPlayer olympaPlayer = account.getFromCache();
		if (olympaPlayer == null)
			return;
		olympaPlayer.setTeamspeakId((long) RegexMatcher.INT.parse(info[1]));
		account.saveToRedis(olympaPlayer);
	}
}