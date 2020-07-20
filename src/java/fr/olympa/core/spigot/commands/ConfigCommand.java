package fr.olympa.core.spigot.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.core.spigot.OlympaCore;

public class ConfigCommand extends OlympaCommand {

	public ConfigCommand(Plugin plugin) {
		super(plugin, "config", "Recharge la config", OlympaCorePermissions.CONFIG_COMMAND);
		allowConsole = false;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		sendSuccess("Configuration reload.");
		OlympaCore.getInstance().getConfig().load();
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}
}
