package fr.olympa.core.bungee.commands;

import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.spigot.TPSUtils;
import fr.olympa.core.bungee.api.command.BungeeCommand;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

public class BPingCommand extends BungeeCommand {

	public BPingCommand(Plugin plugin) {
		super(plugin, "bping", OlympaCorePermissions.BPING_COMMAND);
		addArgs(false, "joueur");
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
