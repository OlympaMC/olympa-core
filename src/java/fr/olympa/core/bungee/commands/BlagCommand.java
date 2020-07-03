package fr.olympa.core.bungee.commands;

import fr.olympa.api.utils.machine.MachineUtils;
import fr.olympa.core.bungee.api.command.BungeeCommand;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Plugin;

public class BlagCommand extends BungeeCommand {

	public BlagCommand(Plugin plugin) {
		super(plugin, "blag", "btps", "bungee");
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		sender.sendMessage(MachineUtils.getInfos());
	}

}
