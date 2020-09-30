package fr.olympa.core.bungee.servers.commands;

import java.util.function.Consumer;

import fr.olympa.api.bungee.command.BungeeCommand;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.utils.machine.OlympaRuntime;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Plugin;

@SuppressWarnings("deprecation")
public class RestartBungeeCommand extends BungeeCommand {

	public RestartBungeeCommand(Plugin plugin) {
		super(plugin, "bungeerestart", OlympaCorePermissions.SERVER_RESTART_COMMAND, "brestart");
		allowConsole = true;
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		Consumer<String> function = proxiedPlayer != null ? out -> sender.sendMessage(out) : null;
		Runtime.getRuntime().addShutdownHook(OlympaRuntime.action("sh start.sh", function));
		plugin.getProxy().stop();
	}
}
