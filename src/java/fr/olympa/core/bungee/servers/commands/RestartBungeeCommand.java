package fr.olympa.core.bungee.servers.commands;

import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.core.bungee.api.command.BungeeCommand;
import fr.olympa.core.bungee.servers.OlympaRuntime;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Plugin;

public class RestartBungeeCommand extends BungeeCommand {

	public RestartBungeeCommand(Plugin plugin) {
		super(plugin, "restartbungee", OlympaCorePermissions.SERVER_RESTART_COMMAND, "restartb");
		allowConsole = true;
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		Runtime.getRuntime().addShutdownHook(OlympaRuntime.action("sh start.sh", sender));
		plugin.getProxy().stop();
	}
}
