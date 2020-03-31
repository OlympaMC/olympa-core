package fr.olympa.core.spigot.security;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.help.HelpTopic;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.command.OlympaCommand;

public class HelpCommand extends OlympaCommand {

	public HelpCommand(Plugin plugin) {
		super(plugin, "?", "help");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		for (HelpTopic cmdLabel : Bukkit.getServer().getHelpMap().getHelpTopics()) {
			sendMessage(cmdLabel.getName() + " | " + cmdLabel.getShortText() + " | " + cmdLabel.getFullText(sender));
		}
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}

}
