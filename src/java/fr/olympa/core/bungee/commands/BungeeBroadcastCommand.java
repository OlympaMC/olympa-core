package fr.olympa.core.bungee.commands;

import fr.olympa.api.bungee.command.BungeeCommand;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.utils.Prefix;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeeBroadcastCommand extends BungeeCommand {
	
	public BungeeBroadcastCommand(Plugin plugin) {
		super(plugin, "bungeebroadcast", "Permet d'envoyer un message sur tout le proxy.", OlympaCorePermissions.BUNGEE_BROADCAST_COMMAND);
	}
	
	@Override
	public void onCommand(CommandSender sender, String[] args) {
		int i = broadcastToAll(Prefix.DEFAULT, String.join(" ", args));
		sendSuccess("Message envoyé à %d entités (dont console).", i);
	}
	
}
