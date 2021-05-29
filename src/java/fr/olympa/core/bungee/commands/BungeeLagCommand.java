package fr.olympa.core.bungee.commands;

import fr.olympa.api.bungee.command.BungeeCommand;
import fr.olympa.api.permission.list.OlympaCorePermissionsBungee;
import fr.olympa.core.bungee.machine.BungeeInfo;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeeLagCommand extends BungeeCommand {

	public BungeeLagCommand(Plugin plugin) {
		super(plugin, "bungeelag", OlympaCorePermissionsBungee.BUNGEE_LAG_COMMAND, "blag", "lagb", "btps", "tpsb");
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		sender.sendMessage(new BungeeInfo(proxiedPlayer == null).getInfoMessage().build());
	}

}
