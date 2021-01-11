package fr.olympa.core.bungee.security;

import fr.olympa.api.bungee.command.BungeeCommand;
import fr.olympa.api.permission.OlympaCorePermissions;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Plugin;

public class SecurityCommand extends BungeeCommand {
	
	public SecurityCommand(Plugin plugin) {
		super(plugin, "security", "Permet de configurer les paramètres de sécurité du bungee.", OlympaCorePermissions.BUNGEE_SECURITY_COMMAND);
	}
	
	@Override
	public void onCommand(CommandSender sender, String[] args) {
		// TODO
	}
	
}
