package fr.olympa.core.bungee.commands;

import fr.olympa.api.bungee.command.BungeeCommand;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.common.permission.list.OlympaCorePermissionsBungee;
import fr.olympa.core.common.redis.RedisAccess;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Plugin;
import redis.clients.jedis.JedisPubSub;

public class RedisCommand extends BungeeCommand {

	private JedisPubSub sub = null;

	public RedisCommand(Plugin plugin) {
		super(plugin, "redis", OlympaCorePermissionsBungee.BUNGEE_REDIS_COMMAND, "listenredis");
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		if (proxiedPlayer != null) {
			sendError("Seule la console peut exécuter cette commande.");
			return;
		}
		if (sub != null) {
			sub.punsubscribe();
			sub = null;
			sendSuccess("Le mode écoute redis est désormais désactivé.");
		} else {
			new Thread(() -> RedisAccess.INSTANCE.connect().psubscribe(sub == null ? sub = new RedisSub() : sub, "*"), "Redis listen sub").start();
			sendSuccess("Le mode écoute redis est désormais activé.");
		}
	}

	class RedisSub extends JedisPubSub {

		@Override
		public void onPMessage(String pattern, String channel, String message) {
			super.onMessage(channel, message);
			OlympaBungee.getInstance().sendMessage("§c§lRedis §e" + channel + "§7 : §f" + message);
		}
	}

}
