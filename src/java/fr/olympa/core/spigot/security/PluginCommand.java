package fr.olympa.core.spigot.security;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.utils.Prefix;

public class PluginCommand extends OlympaCommand {

	public PluginCommand(Plugin plugin) {
		super(plugin, "plugin", "pl");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		sendMessage(Prefix.DEFAULT, "Tous nos plugins sont développés par nos soins.");
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}

}
