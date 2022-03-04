package fr.olympa.core.spigot.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.spigot.command.OlympaCommand;
import fr.olympa.core.common.permission.list.OlympaCorePermissionsSpigot;
import fr.olympa.core.spigot.OlympaCore;

public class RestartCommand extends OlympaCommand {

	public RestartCommand(Plugin plugin) {
		super(plugin, "restart", "Redémarre le serveur.", OlympaCorePermissionsSpigot.SERVER_RESTART_COMMAND);
		allowConsole = true;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		OlympaCore.getInstance().restartServer(sender);
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}
}
