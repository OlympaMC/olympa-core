package fr.olympa.core.bungee.servers.commands;

import java.util.ArrayList;
import java.util.stream.Collectors;

import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.api.command.BungeeCommand;
import fr.olympa.core.bungee.servers.MonitorInfo;
import fr.olympa.core.bungee.servers.MonitorServers;
import fr.olympa.core.bungee.servers.ServerStartStop;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.TabExecutor;

public class StopServerCommand extends BungeeCommand implements TabExecutor {

	public StopServerCommand(Plugin plugin) {
		super(plugin, "stopserv", OlympaCorePermissions.SERVER_START_COMMAND, "stopserver");
		minArg = 1;
		usageString = "<" + plugin.getProxy().getServers().entrySet().stream().map(entry -> entry.getKey()).collect(Collectors.joining("|")) + ">";
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		ServerStartStop.action("stop", args[0], sender);
	}

	@Override
	public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
		if (args.length == 0) {
			return MonitorServers.getLastServerInfo().stream().map(MonitorInfo::getName).collect(Collectors.toList());
		} else if (args.length == 1) {
			return Utils.startWords(args[0], MonitorServers.getLastServerInfo().stream().map(MonitorInfo::getName).collect(Collectors.toList()));
		}
		return new ArrayList<>();
	}
}