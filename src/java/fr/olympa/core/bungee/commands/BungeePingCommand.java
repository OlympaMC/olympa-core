package fr.olympa.core.bungee.commands;

import fr.olympa.api.bungee.command.BungeeCommand;
import fr.olympa.api.commun.permission.list.OlympaCorePermissionsBungee;
import fr.olympa.api.spigot.utils.TPSUtils;
import fr.olympa.api.utils.Prefix;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeePingCommand extends BungeeCommand {

	public BungeePingCommand(Plugin plugin) {
		super(plugin, "bungeeping", OlympaCorePermissionsBungee.BPING_COMMAND, "bping");
		addArgs(false, "JOUEUR");
		minArg = 0;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onCommand(CommandSender sender, String[] args) {
		ProxiedPlayer target;
		ProxiedPlayer player = getPlayer();
		if (args.length == 0) {
			if (player == null) {
				sendImpossibleWithConsole();
				return;
			}
			target = player;
		} else {
			target = ProxyServer.getInstance().getPlayer(args[0]);
			if (target == null) {
				sendUnknownPlayer(args[0]);
				return;
			}
		}
		if (player != null && player.getUniqueId().equals(target.getUniqueId()))
			sender.sendMessage(Prefix.DEFAULT_GOOD.formatMessage("Ton ping bungee est de &2%s&ams.", TPSUtils.getPingColor(target.getPing())));
		else
			sender.sendMessage(Prefix.DEFAULT_GOOD.formatMessage("Le ping bungee de &2%s&a est de &2%s&ams.", target.getName(), TPSUtils.getPingColor(target.getPing())));
		return;
	}

}
