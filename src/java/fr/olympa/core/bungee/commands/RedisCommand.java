package fr.olympa.core.bungee.commands;

import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.utils.OlympaJedisPubSub;
import fr.olympa.core.bungee.api.command.BungeeCommand;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Plugin;

public class RedisCommand extends BungeeCommand {
	
	public RedisCommand(Plugin plugin) {
		super(plugin, "redis", OlympaCorePermissions.BUNGEE_REDIS_COMMAND, "listenredis");
	}
	
	@Override
	public void onCommand(CommandSender sender, String[] args) {
		if (proxiedPlayer != null) {
			sendError("Seule la console peut exécuter cette commande.");
			return;
		}
		OlympaJedisPubSub.redisMode = !OlympaJedisPubSub.redisMode;
		sendSuccess("Le mode écoute redis est désormais %s.", OlympaJedisPubSub.redisMode ? "activé" : "désactivé");
	}
	
}
