package fr.olympa.core.bungee.redis.receiver;

import fr.olympa.api.chat.TxtComponentBuilder;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.bungee.OlympaBungee;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import redis.clients.jedis.JedisPubSub;

public class SpigotServerSwitchReceiver2 extends JedisPubSub {

	@Override
	public void onMessage(String channel, String message) {
		super.onMessage(channel, message);
		String[] args = message.split(":");
		OlympaBungee instance = OlympaBungee.getInstance();
		ProxiedPlayer player = OlympaBungee.getInstance().getProxy().getPlayer(args[0]);
		String serverName = args[1];
		Callback<Boolean> callback = (result, error) -> {
			if (result)
				player.sendMessage(TxtComponentBuilder.of(Prefix.DEFAULT_GOOD, "Connexion au serveur %s établie !", serverName));
			else
				player.sendMessage(TxtComponentBuilder.of(Prefix.DEFAULT_BAD, "Echec de la connexion au serveur &4%s&c: &4%s&c. ", serverName, error.getMessage()));
		};
		System.out.println(String.format("[REDIS] Demande de serveur switch %s sur le serv %s.", player.getName(), serverName));
		player.connect(instance.getProxy().getServersCopy().get(serverName), callback, false, 60000);
	}

}
