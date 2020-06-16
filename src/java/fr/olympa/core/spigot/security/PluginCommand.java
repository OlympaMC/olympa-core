package fr.olympa.core.spigot.security;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.utils.Prefix;

public class PluginCommand extends OlympaCommand {

	public PluginCommand(Plugin plugin) {
		super(plugin, "plugin", "pl", "plugins");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		sendMessage(Prefix.DEFAULT, "Les plugins de notre serveur sont développés par nos soins.");
		sendMessage(Prefix.DEFAULT, "Plugins externes utilisés : §a%s", Arrays.stream(Bukkit.getPluginManager().getPlugins()).filter(x -> !x.getName().contains("Olympa")).map(x -> x.getName()).collect(Collectors.joining(", ")));
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}

}
