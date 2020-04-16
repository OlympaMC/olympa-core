package fr.olympa.core.bungee.ban.commands;

import java.util.UUID;

import fr.olympa.api.objects.OlympaConsole;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.utils.Matcher;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.bungee.api.command.BungeeCommand;
import fr.olympa.core.bungee.ban.commands.methods.KickIp;
import fr.olympa.core.bungee.ban.commands.methods.KickPlayer;
import fr.olympa.core.bungee.utils.BungeeConfigUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

public class KickCommand extends BungeeCommand {

	public KickCommand(Plugin plugin) {
		super(plugin, "kick", OlympaCorePermissions.BAN_KICK_COMMAND, "eject");
		usageString = BungeeConfigUtils.getString("ban.messages.usagekick");
		minArg = 1;
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		UUID author;
		if (sender instanceof ProxiedPlayer) {
			author = proxiedPlayer.getUniqueId();
		} else {
			author = OlympaConsole.getUniqueId();
		}

		if (Matcher.isUsername(args[0])) {
			KickPlayer.addKick(author, sender, args[0], null, args, olympaPlayer);

		} else if (Matcher.isFakeIP(args[0])) {

			if (Matcher.isIP(args[0])) {
				KickIp.addKick(author, sender, args[0], args, olympaPlayer);
			} else {
				sendMessage(Prefix.DEFAULT_BAD, BungeeConfigUtils.getString("default.ipinvalid").replaceAll("%ip%", args[0]));
				return;
			}

		} else if (Matcher.isFakeUUID(args[0])) {

			if (Matcher.isUUID(args[0])) {
				KickPlayer.addKick(author, sender, null, UUID.fromString(args[0]), args, olympaPlayer);
			} else {
				sendMessage(Prefix.DEFAULT_BAD, BungeeConfigUtils.getString("default.uuidinvalid").replaceAll("%uuid%", args[0]));
				return;
			}

		} else {
			sendMessage(Prefix.DEFAULT_BAD, BungeeConfigUtils.getString("default.typeunknown").replaceAll("%type%", args[0]));
			return;
		}
	}
}
