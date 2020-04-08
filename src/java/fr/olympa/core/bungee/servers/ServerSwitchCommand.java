package fr.olympa.core.bungee.servers;

import java.util.List;
import java.util.stream.Collectors;

import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.api.command.BungeeCommand;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Command;

public class ServerSwitchCommand extends BungeeCommand {

	public ServerSwitchCommand() {
		super(OlympaBungee.getInstance(), "serverswitch", OlympaCorePermissions.SERVER_SWITCH_COMMAND, "switch", "server");
		allowConsole = false;
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		if (args.length == 0) {
			sendErreur("Syntaxe incorrecte.");
			return;
		}
		for (ServerInfo server : MonitorServers.getServers().keySet()) {
			if (server.getName().equalsIgnoreCase(args[0])) {
				sendMessage(Prefix.DEFAULT_GOOD, "Téléportation sur le serveur &2" + Utils.capitalize(server.getName()) + "&a.");
				getProxiedPlayer().connect(server);
				return;
			}
		}
		sendMessage(Prefix.DEFAULT_BAD, "Le serveur &4" + args[0] + "&c n'existe pas.");
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		List<String> servers = MonitorServers.getServers().keySet().stream().map(ServerInfo::getName).collect(Collectors.toList());
		if (args.length == 0) {
			return servers;
		} else if (args.length == 1) {
			return Utils.startWords(args[0], servers);
		}
		return null;
	}
}
