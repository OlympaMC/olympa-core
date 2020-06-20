package fr.olympa.core.bungee.servers.commands;

import java.util.ArrayList;
import java.util.Set;

import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.api.command.BungeeCommand;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.TabExecutor;

public class ServerSwitchCommand extends BungeeCommand implements TabExecutor {

	public ServerSwitchCommand(Plugin plugin) {
		super(plugin, "serverswitch", OlympaCorePermissions.SERVER_SWITCH_COMMAND, "switch", "server");
		allowConsole = false;
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		if (args.length == 0) {
			sendError("Syntaxe incorrecte.");
			return;
		}
		ServerInfo server = OlympaBungee.getInstance().getProxy().getServerInfo(args[0]);
		if (server == null) {
			sendMessage(Prefix.DEFAULT_BAD, "Le serveur &4" + args[0] + "&c n'existe pas.");
			return;
		}
		sendMessage(Prefix.DEFAULT_GOOD, "Téléportation sur le serveur &2" + Utils.capitalize(server.getName()) + "&a.");
		getProxiedPlayer().connect(server);
		return;

	}

	@Override
	public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
		Set<String> servers = OlympaBungee.getInstance().getProxy().getServers().keySet();
		if (args.length == 0) {
			return servers;
		} else if (args.length == 1) {
			return Utils.startWords(args[0], servers);
		}
		return new ArrayList<>();
	}
}
