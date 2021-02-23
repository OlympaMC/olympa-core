package fr.olympa.core.bungee.commands;

import fr.olympa.api.bungee.command.BungeeCommand;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.utils.machine.MachineUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeeLagCommand extends BungeeCommand {

	public BungeeLagCommand(Plugin plugin) {
		super(plugin, "bungeelag", OlympaCorePermissions.BUNGEE_LAG_COMMAND, "blag", "btps", "bungee");
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		sender.sendMessage(MachineUtils.getInfos(proxiedPlayer == null));
	}

}
