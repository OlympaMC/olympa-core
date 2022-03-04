package fr.olympa.core.spigot;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.spigot.command.OlympaCommand;
import fr.olympa.api.utils.Prefix;

public class TestCommand extends OlympaCommand implements TabCompleter, Listener {

	public TestCommand(Plugin plugin) {
		super(plugin, "test");
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String commandLabel, String[] args) {
		sender.sendMessage(Prefix.INFO.formatMessage("Command '%s/%s' commandLabel '%s' args '%s'", command.getName(), command.getLabel(), commandLabel, String.join("|", args)));
		return null;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		sender.sendMessage("Hello world");
		return false;
	}

	@EventHandler
	public void onPlayerCommand(com.destroystokyo.paper.event.server.AsyncTabCompleteEvent event) {

	}
}
