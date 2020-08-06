package fr.olympa.core.bungee.datamanagment;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.core.bungee.api.command.BungeeCommand;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Plugin;

@SuppressWarnings("deprecation")
public class GetUUIDCommand extends BungeeCommand {

	public GetUUIDCommand(Plugin plugin) {
		super(plugin, "getuuid", OlympaCorePermissions.GETUUID_COMMAND);
		minArg = 1;
		usageString = "[joueur]";
		description = "Donne l'uuid serveur d'un pseudo";
		allowConsole = true;
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		String playerName = args[0].toLowerCase();
		UUID uuidCrack;
		try {
			uuidCrack = UUID.nameUUIDFromBytes(("OfflinePlayer:" + playerName).getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			sender.sendMessage("Une erreur est survenue.");
			e.printStackTrace();
			return;
		}
		sender.sendMessage("UUID pour " + playerName + " " + uuidCrack.toString());
	}
}
