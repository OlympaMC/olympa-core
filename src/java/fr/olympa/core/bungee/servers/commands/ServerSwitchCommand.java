package fr.olympa.core.bungee.servers.commands;

import fr.olympa.api.bungee.command.BungeeComplexCommand;
import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.api.permission.list.OlympaCorePermissionsBungee;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.OlympaBungee;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class ServerSwitchCommand extends BungeeComplexCommand {

	public ServerSwitchCommand(OlympaBungee plugin) {
		super(plugin, "serverswitch", "Change de serveurs.", OlympaCorePermissionsBungee.SERVER_SWITCH_COMMAND, "switch", "server");
		//		MonitorServers.getServers()

	}

	@Cmd (otherArg = true, args = { "SERVERS", "OLYMPA_PLAYERS" }, min = 1)
	public void otherArg(CommandContext cmd) {
		ProxiedPlayer target = null;
		ServerInfo server = cmd.getArgument(0);
		if (cmd.getArgumentsLength() == 1) {
			if (isConsole()) {
				sendImpossibleWithConsole();
				return;
			}
			target = getProxiedPlayer();
		} else
			target = cmd.getArgument(1);
		String serverName = Utils.capitalize(server.getName());
		if (target != getProxiedPlayer())
			sendSuccess("Envoi de %s vers le serveur §2%s§a.", target.getName(), serverName);
		sendMessage(target, Prefix.DEFAULT_GOOD, "Téléportation sur le serveur §2%s§a.", serverName);
		target.connect(server);
	}
	/*
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
			} else {
				player = ProxyServer.getInstance().getPlayer(args[1]);
				if (player == null) {
					sendUnknownPlayer(args[1]);
					return;
				}
			}
			String serverName = Utils.capitalize(server.getName());
			if (player != getProxiedPlayer())
				sendSuccess("Envoi de %s vers le serveur §2%s§a.", player.getName(), serverName);
			sendMessage(player, Prefix.DEFAULT_GOOD, "Téléportation sur le serveur §2%s§a.", serverName);
			player.connect(server);
		}*/
}
