package fr.olympa.core.bungee.redis.receiver;

import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.spigot.OlympaCore;
import net.md_5.bungee.api.ProxyServer;
import redis.clients.jedis.JedisPubSub;

public class BungeeCommandReceiver extends JedisPubSub {

	@Override
	public void onMessage(String channel, String message) {
		String[] args = message.split(";");
		String serverFrom = args[0];
		if (!OlympaCore.getInstance().isServerName(serverFrom))
			return;
		String command = args[1];
		OlympaBungee.getInstance().sendRedis("&aCommande à exécuter reçue : &2" + command + "&a.");
		ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), command);
	}
}
