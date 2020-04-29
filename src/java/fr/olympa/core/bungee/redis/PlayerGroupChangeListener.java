package fr.olympa.core.bungee.redis;

import java.util.UUID;

import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.GsonCustomizedObjectTypeAdapter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import redis.clients.jedis.JedisPubSub;

public class PlayerGroupChangeListener extends JedisPubSub {

	@Override
	public void onMessage(String channel, String message) {
		OlympaPlayer olympaPlayer = GsonCustomizedObjectTypeAdapter.GSON.fromJson(message, OlympaPlayer.class);

		UUID uuid = olympaPlayer.getUniqueId();
		ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);
		if (player == null) {
			return;
		}
		System.out.println("receive " + olympaPlayer.getName() + " from redis");
		AccountProvider olympaAccount = new AccountProvider(olympaPlayer.getUniqueId());
		olympaAccount.saveToCache(olympaPlayer);
	}
}
