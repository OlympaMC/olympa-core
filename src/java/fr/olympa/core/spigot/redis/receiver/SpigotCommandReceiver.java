package fr.olympa.core.spigot.redis.receiver;

import org.bukkit.Bukkit;

import fr.olympa.core.spigot.OlympaCore;
import redis.clients.jedis.JedisPubSub;

public class SpigotCommandReceiver extends JedisPubSub {

	@Override
	public void onMessage(String channel, String message) {
		String[] args = message.split(";");
		String serverFrom = args[0];
		if (!OlympaCore.getInstance().isServerName(serverFrom))
			return;
		String command = args[1];
		OlympaCore.getInstance().sendRedis("§eCommande à exécuter : §2" + command);
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
	}
}
