package fr.olympa.core.bungee.servers.commands;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import fr.olympa.api.bungee.command.BungeeCommand;
import fr.olympa.api.common.bash.OlympaRuntime;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.common.permission.list.OlympaCorePermissionsBungee;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Plugin;

@SuppressWarnings("deprecation")
public class StartServerCommand extends BungeeCommand {

	private static String commandName;

	public static String getCommandName() {
		return commandName;
	}

	public StartServerCommand(Plugin plugin) {
		super(plugin, "startserver", OlympaCorePermissionsBungee.SERVER_START_COMMAND);
		commandName = command;
		minArg = 1;
		usageString = "<" + plugin.getProxy().getServers().entrySet().stream().map(entry -> entry.getKey()).collect(Collectors.joining("|")) + ">";
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		Consumer<String> function = proxiedPlayer != null ? out -> sender.sendMessage(out) : null;
		OlympaRuntime.action("start", args[0], function).start();
	}

	@Override
	public Iterable<String> onTabComplete(CommandSender sender, BungeeCommand command, String[] args) {
		if (args.length == 0)
			return OlympaBungee.getInstance().getProxy().getServers().keySet();
		else if (args.length == 1)
			return Utils.startWords(args[0], OlympaBungee.getInstance().getProxy().getServers().keySet());
		return new ArrayList<>();
	}
}
