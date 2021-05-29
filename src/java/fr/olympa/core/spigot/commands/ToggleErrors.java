package fr.olympa.core.spigot.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.permission.list.OlympaAPIPermissionsSpigot;
import fr.olympa.core.spigot.redis.RedisSpigotSend;

public class ToggleErrors extends OlympaCommand {
	
	public ToggleErrors(Plugin plugin) {
		super(plugin, "errors", "Permet de désactiver/réactiver l'envoi des erreurs sur Discord.", OlympaAPIPermissionsSpigot.ERRORS_COMMAND, "discorderrors");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		sendSuccess("L'envoi des erreurs sur Discord est désormais %s", (RedisSpigotSend.errorsEnabled = !RedisSpigotSend.errorsEnabled) ? "§lactivé" : "§c§ldésactivé");
		return false;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}
	
}
