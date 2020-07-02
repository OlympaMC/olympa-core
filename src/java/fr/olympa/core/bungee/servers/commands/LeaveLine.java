package fr.olympa.core.bungee.servers.commands;

import fr.olympa.api.utils.Prefix;
import fr.olympa.core.bungee.api.command.BungeeCommand;
import fr.olympa.core.bungee.servers.ServersConnection;
import fr.olympa.core.bungee.utils.BungeeUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Plugin;

public class LeaveLine extends BungeeCommand {
	
	public LeaveLine(Plugin plugin) {
		super(plugin, "leaveline", "quitfile");
		allowConsole = false;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onCommand(CommandSender sender, String[] args) {
		if (ServersConnection.removeTryToConnect(proxiedPlayer))
			proxiedPlayer.sendMessage(Prefix.DEFAULT_GOOD + BungeeUtils.color("Tu as quitté la file d'attente."));
		else
			proxiedPlayer.sendMessage(Prefix.DEFAULT_BAD + BungeeUtils.color("Tu n'es dans aucune file d'attente."));
	}
	
}