package fr.olympa.core.spigot.commands;

import java.util.List;
import java.util.function.Consumer;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.utils.machine.OlympaRuntime;

public class RestartCommand extends OlympaCommand {

	public RestartCommand(Plugin plugin) {
		super(plugin, "restart", "Redémarre le serveur", OlympaCorePermissions.SERVER_RESTART_COMMAND);
		allowConsole = true;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Consumer<String> function = player != null ? out -> sender.sendMessage(out) : null;
		Runtime.getRuntime().addShutdownHook(OlympaRuntime.action("sh start.sh", function));
		plugin.getServer().shutdown();
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}
}