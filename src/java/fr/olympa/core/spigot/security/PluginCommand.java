package fr.olympa.core.spigot.security;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.utils.Prefix;

public class PluginCommand extends OlympaCommand {

	public PluginCommand(Plugin plugin) {
		super(plugin, "plugin", "pl", "plugins");
		super.description = "Affiche la liste des plugins présents sur le serveur.";
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		sendMessage(Prefix.DEFAULT, "Les plugins de notre serveur sont développés par nos soins.");
		List<String> pluginsOlympa = new ArrayList<>();
		List<String> plugins = new ArrayList<>();
		for (Plugin p : Bukkit.getPluginManager().getPlugins()) {
			String name = p.getName();
			if (name.contains("Olympa") || p.getDescription().getAuthors().stream().anyMatch(x -> x.contains("SkytAsul") || x.contains("Tristiisch"))) {
				//plugins.add("§o*redacted*");
				pluginsOlympa.add(name);
			}else {
				plugins.add(name);
			}
		}
		//Prefix.NONE.sendMessage(sender, "&fPlugins (%s): &a%s", plugins.size(), String.join("&7, &a", plugins));
		sendMessage(Prefix.DEFAULT, "Plugins externes (%d): §a%s", plugins.size(), String.join("§7, §a", plugins));
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}

}
