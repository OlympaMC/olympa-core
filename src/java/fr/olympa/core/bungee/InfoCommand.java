package fr.olympa.core.bungee;

import java.sql.SQLException;

import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.bungee.api.command.BungeeCommand;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Plugin;

public class InfoCommand extends BungeeCommand {

	public InfoCommand(Plugin plugin) {
		super(plugin, "info", OlympaCorePermissions.INFO_COMMAND);
		minArg = 1;
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		OlympaPlayer target = null;
		try {
			target = AccountProvider.get(args[0]);
			if (target == null) {
				sendUnknownPlayer(args[0]);
				return;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		sendMessage(Prefix.DEFAULT, "Info: " + target.getName());
	}

}
