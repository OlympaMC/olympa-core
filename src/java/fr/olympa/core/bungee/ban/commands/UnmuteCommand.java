package fr.olympa.core.bungee.ban.commands;

import java.util.UUID;

import fr.olympa.api.bungee.command.BungeeCommand;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.player.OlympaConsole;
import fr.olympa.api.utils.Matcher;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.ban.commands.methods.UnmutePlayer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;

public class UnmuteCommand extends BungeeCommand {

	public UnmuteCommand(OlympaBungee plugin) {
		super(plugin, "unmute", OlympaCorePermissions.BAN_UNMUTE_COMMAND, "umute");
		usageString = plugin.getConfig().getString("ban.messages.usageunmute");
		minArg = 2;
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		UUID author;
		if (sender instanceof ProxiedPlayer)
			author = proxiedPlayer.getUniqueId();
		else
			author = OlympaConsole.getUniqueId();
		Configuration config = OlympaBungee.getInstance().getConfig();

		if (Matcher.isUsername(args[0]))
			UnmutePlayer.unBan(author, sender, null, args[0], args);
		else if (Matcher.isFakeUUID(args[0])) {
			if (Matcher.isUUID(args[0]))
				UnmutePlayer.unBan(author, sender, UUID.fromString(args[0]), null, args);
			else {
				this.sendMessage(config.getString("default.messages.uuidinvalid").replace("%uuid%", args[0]));
				return;
			}
		} else {
			this.sendMessage(config.getString("default.messages.typeunknown").replace("%type%", args[0]));
			return;
		}

	}
}
