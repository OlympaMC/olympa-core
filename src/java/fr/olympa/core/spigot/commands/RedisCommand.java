package fr.olympa.core.spigot.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.utils.OlympaJedisPubSub;

public class RedisCommand extends OlympaCommand {
	
	public RedisCommand(Plugin plugin) {
		super(plugin, "redis", OlympaCorePermissions.BUNGEE_REDIS_COMMAND, "listenredis");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (player != null) {
			sendError("Seule la console peut exécuter cette commande.");
			return false;
		}
		OlympaJedisPubSub.redisMode = !OlympaJedisPubSub.redisMode;
		sendSuccess("Le mode écoute redis est désormais %s.", OlympaJedisPubSub.redisMode ? "activé" : "désactivé");
		return false;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}
	
}
