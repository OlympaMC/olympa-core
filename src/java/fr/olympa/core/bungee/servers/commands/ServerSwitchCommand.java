package fr.olympa.core.bungee.servers.commands;

import fr.olympa.api.bungee.command.BungeeCommand;
import fr.olympa.api.command.CommandArgument;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.OlympaBungee;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.TabExecutor;

public class ServerSwitchCommand extends BungeeCommand implements TabExecutor {

	public ServerSwitchCommand(Plugin plugin) {
		super(plugin, "serverswitch", OlympaCorePermissions.SERVER_SWITCH_COMMAND, "switch", "server");
		addCommandArguments(true, new CommandArgument("SERVERS"));
		addCommandArguments(false, new CommandArgument("PLAYERS"));
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
		ProxiedPlayer player = null;
		if (args.length == 1) {
			if (isConsole()) {
				sendImpossibleWithConsole();
				return;
			}
			player = getProxiedPlayer();
		}else {
			player = ProxyServer.getInstance().getPlayer(args[1]);
			if (player == null) {
				sendUnknownPlayer(args[1]);
				return;
			}
		}
		String serverName = Utils.capitalize(server.getName());
		if (player != getProxiedPlayer()) sendSuccess("Envoi de %s vers le serveur §2%s§a.", player.getName(), serverName);
		sendMessage(player, Prefix.DEFAULT_GOOD, "Téléportation sur le serveur §2%s§a.", serverName);
		player.connect(server);
	}
}
