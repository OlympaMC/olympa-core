package fr.olympa.core.bungee.commands;

import fr.olympa.api.bungee.command.BungeeCommand;
import fr.olympa.api.bungee.permission.OlympaBungeePermission;
import fr.olympa.api.utils.Prefix;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Plugin;

public class ShopCommand extends BungeeCommand {
	
	public ShopCommand(Plugin plugin) {
		super(plugin, "shop", "Affiche le lien vers la boutique.", (OlympaBungeePermission) null, "boutique");
	}
	
	@Override
	public void onCommand(CommandSender sender, String[] args) {
		sendHoverAndURL(Prefix.DEFAULT_GOOD, "§eLa boutique Olympa est accessible sur §6§l§nwww.olympa.fr/shop", "§7Clique pour accéder à la boutique !", "https://olympa.fr/shop");
	}
	
}
