package fr.olympa.core.bungee.ban.commands;

import fr.olympa.api.bungee.command.BungeeComplexCommand;
import fr.olympa.api.common.permission.list.OlympaCorePermissionsBungee;
import net.md_5.bungee.api.plugin.Plugin;

public class BanMenuCommand extends BungeeComplexCommand {

	public BanMenuCommand(Plugin plugin) {
		super(plugin, "banmenu", "Toutes les autres fonctionnalités de la gestion des bans", OlympaCorePermissionsBungee.BAN_BANMENU_COMMAND);
	}

}
