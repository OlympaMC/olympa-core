package fr.olympa.core.bungee.commands;

import fr.olympa.api.bungee.command.BungeeCommand;
import fr.olympa.api.bungee.permission.OlympaBungeePermission;
import fr.olympa.api.utils.Prefix;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Plugin;

public class VoteCommand extends BungeeCommand {
	
	public VoteCommand(Plugin plugin) {
		super(plugin, "vote", "Affiche le lien vers le vote.", (OlympaBungeePermission) null);
	}
	
	@Override
	public void onCommand(CommandSender sender, String[] args) {
		sendHoverAndURL(Prefix.DEFAULT_GOOD, "§eVous pouvez voter pour Olympa sur §6§l§nwww.olympa.fr/vote", "§7Clique pour voter !", "https://olympa.fr/vote");
	}
	
}
