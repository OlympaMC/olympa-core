package fr.olympa.core.bungee.servers;

import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.api.command.BungeeCommand;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.config.ServerInfo;

public class ServerSwitchCommand extends BungeeCommand {

	public ServerSwitchCommand() {
		super(OlympaBungee.getInstance(), "serverswitch", OlympaCorePermissions.SERVER_SWITCH_COMMAND, "switch");
		this.allowConsole = false;
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		if (args.length == 0) {
			sendErreur("Syntaxe incorrecte.");
			return;
		}
		for (ServerInfo server : MonitorServers.getServers().keySet()) {
			if (server.getName().equalsIgnoreCase(args[0])) {
				getProxiedPlayer().connect(server);
				return;
			}
		}
	}

}
