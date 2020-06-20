package fr.olympa.core.bungee.servers.commands;

import java.util.ArrayList;
import java.util.stream.Collectors;

import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.api.command.BungeeCommand;
import fr.olympa.core.bungee.servers.ScriptBashMc;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.TabExecutor;

public class StartServerCommand extends BungeeCommand implements TabExecutor {

	public StartServerCommand(Plugin plugin) {
		super(plugin, "startserv", OlympaCorePermissions.SERVER_START_COMMAND, "startserver");
		minArg = 1;
		usageString = "<" + plugin.getProxy().getServers().entrySet().stream().map(entry -> entry.getKey()).collect(Collectors.joining("|")) + ">";
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		ScriptBashMc.action("start", args[0], sender);
	}

	@Override
	public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
		if (args.length == 0) {
			return OlympaBungee.getInstance().getProxy().getServers().keySet();
		} else if (args.length == 1) {
			return Utils.startWords(args[0], OlympaBungee.getInstance().getProxy().getServers().keySet());
		}
		return new ArrayList<>();
	}
}
