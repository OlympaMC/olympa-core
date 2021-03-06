package fr.olympa.core.bungee.commands;

import fr.olympa.api.bungee.command.BungeeCommand;
import fr.olympa.api.common.machine.TpsMessageProvider;
import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.core.common.permission.list.OlympaCorePermissionsBungee;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeeLagCommand extends BungeeCommand {

	public BungeeLagCommand(Plugin plugin) {
		super(plugin, "bungeelag", OlympaCorePermissionsBungee.BUNGEE_LAG_COMMAND, "blag", "lagb", "btps", "tpsb");
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		sender.sendMessage(new TpsMessageProvider(super.<OlympaPlayer>getOlympaPlayerNullable()).getInfoMessage().build());
	}

}
