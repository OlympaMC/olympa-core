package fr.olympa.core.bungee.servers.commands;

import java.util.function.Consumer;

import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.utils.machine.OlympaRuntime;
import fr.olympa.core.bungee.api.command.BungeeCommand;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Plugin;

@SuppressWarnings("deprecation")
public class RestartBungeeCommand extends BungeeCommand {

	public RestartBungeeCommand(Plugin plugin) {
		super(plugin, "restartbungee", OlympaCorePermissions.SERVER_RESTART_COMMAND, "restartb");
		allowConsole = true;
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		Consumer<String> function = proxiedPlayer != null ? out -> sender.sendMessage(out) : null;
		Runtime.getRuntime().addShutdownHook(OlympaRuntime.action("sh start.sh", function));
		plugin.getProxy().stop();
	}
}
