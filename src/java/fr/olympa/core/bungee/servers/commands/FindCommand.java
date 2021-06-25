package fr.olympa.core.bungee.servers.commands;

import java.util.stream.Collectors;

import fr.olympa.api.bungee.command.BungeeCommand;
import fr.olympa.api.common.permission.list.OlympaCorePermissionsBungee;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

public class FindCommand extends BungeeCommand {

	public FindCommand(Plugin plugin) {
		super(plugin, "find", "Trouve le serveur auquel un joueur est connecté.", OlympaCorePermissionsBungee.FIND_COMMAND);
		minArg = 1;
		usageString = "<joueur>";
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		ProxiedPlayer target = ProxyServer.getInstance().getPlayer(args[0]);
		if (target == null) {
			sendUnknownPlayer(args[0]);
			return;
		}
		sendSuccess("§eJoueur §6%s§e localisé sur §6§l%s§e.", target.getName(), target.getServer().getInfo().getName());
	}

	@Override
	public Iterable<String> onTabComplete(CommandSender sender, BungeeCommand command, String[] args) {
		String arg = args.length == 0 ? null : args[0].toLowerCase();
		return ProxyServer.getInstance().getPlayers().stream().map(ProxiedPlayer::getName).filter(name -> arg == null ? true : name.toLowerCase().startsWith(arg)).collect(Collectors.toList());
	}

}
