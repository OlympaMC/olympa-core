package fr.olympa.core.bungee.servers.commands;

import fr.olympa.api.bungee.command.BungeeComplexCommand;
import fr.olympa.api.common.chat.TxtComponentBuilder;
import fr.olympa.api.common.command.complex.Cmd;
import fr.olympa.api.common.command.complex.CommandContext;
import fr.olympa.api.common.server.ServerInfoAdvancedBungee;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.common.permission.list.OlympaCorePermissionsBungee;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent.Reason;

public class ServerSwitchCommand extends BungeeComplexCommand {

	public ServerSwitchCommand(OlympaBungee plugin) {
		super(plugin, "serverswitch", "Change de serveurs.", OlympaCorePermissionsBungee.SERVER_SWITCH_COMMAND, "switch", "server");

	}

	@Cmd(otherArg = true, args = { "SERVERS", "PLAYERS" }, min = 1)
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
		ProxiedPlayer finalTarget = target;
		ServerInfoAdvancedBungee monitorInfo = OlympaBungee.getInstance().getMonitoring().getMonitor(server);
		if (monitorInfo == null) {
			sendError("Impossible d'aller vers le serveur §2%s§a, son état est inconnu.", Utils.capitalize(server.getName()));
			return;
		}
		String serverName = monitorInfo.getHumanName();
		if (!monitorInfo.getStatus().canConnect()) {
			if (target == getProxiedPlayer() && !monitorInfo.canConnect(olympaPlayer) && OlympaCorePermissionsBungee.SERVER_START_COMMAND.hasPermission(olympaPlayer))
				sendComponents(TxtComponentBuilder.of(Prefix.DEFAULT_BAD, "Impossible d'aller vers le serveur &4%s§a&c, il est fermé. &7[&2DEMARRER&7]",
						"/" + StartServerCommand.getCommandName() + " " + server.getName(), "&2Clique pour démarrer " + serverName, serverName));
			else
				sendError("Impossible d'aller vers le serveur §2%s§a, il est fermé.", serverName);
			return;
		}
		if (target == getProxiedPlayer() && !monitorInfo.canConnect(olympaPlayer)) {
			sendError("Tu n'as pas la permission de rejoindre §2%s§a.", serverName);
			return;
		}
		if (finalTarget.getServer() != null && finalTarget.getServer().getInfo().equals(server)) {
			if (target != getProxiedPlayer())
				sendError("%s est déjà sur §2%s§a.", target.getName(), serverName);
			else
				sendError("Tu es déjà sur §2%s§a.", serverName);
			return;
		}
		if (target != getProxiedPlayer())
			sendSuccess("Envoi de %s vers le serveur §2%s§a.", target.getName(), serverName);
		sendMessage(finalTarget, Prefix.DEFAULT_GOOD, "Téléportation sur le serveur §2%s§a.", serverName);
		finalTarget.connect(server, (succes, error) -> {
			if (succes) {
				if (finalTarget != getProxiedPlayer())
					sendSuccess("%s est désormais sur le serveur §2%s§a.", finalTarget.getName(), serverName);
				sendMessage(finalTarget, Prefix.DEFAULT_GOOD, "Bienvenue au §2%s§a.", serverName);
			} else if (finalTarget != getProxiedPlayer())
				sendError("Impossible d'aller vers le serveur §2%s§a pour %s: %s", serverName, finalTarget.getName(), error.getMessage());
			else if (error != null)
				sendError("Impossible d'aller vers le serveur §2%s§a: %s", serverName, error.getMessage());
		}, false, Reason.COMMAND, 10);
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
