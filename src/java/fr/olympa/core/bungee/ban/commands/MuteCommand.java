package fr.olympa.core.bungee.ban.commands;

import java.util.UUID;

import fr.olympa.api.objects.OlympaConsole;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.utils.Matcher;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.api.command.BungeeCommand;
import fr.olympa.core.bungee.ban.commands.methods.MuteIp;
import fr.olympa.core.bungee.ban.commands.methods.MutePlayer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;

public class MuteCommand extends BungeeCommand {

	public MuteCommand(Plugin plugin) {
		super(plugin, "mute", OlympaCorePermissions.BAN_MUTE_COMMAND, "tempmute");
		minArg = 2;
		usageString = "<joueur|uuid|ip> [temps] <motif>";
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		UUID author;
		if (sender instanceof ProxiedPlayer) {
			author = proxiedPlayer.getUniqueId();
		} else {
			author = OlympaConsole.getUniqueId();
		}

		Configuration config = OlympaBungee.getInstance().getConfig();
		if (Matcher.isUsername(args[0])) {
			MutePlayer.addMute(author, sender, args[0], null, args, olympaPlayer);

		} else if (Matcher.isFakeIP(args[0])) {

			if (Matcher.isIP(args[0])) {
				MuteIp.addMute(author, sender, args[0], args, olympaPlayer);
			} else {
				this.sendMessage(Prefix.DEFAULT_BAD, config.getString("default.ipinvalid").replaceAll("%ip%", args[0]));
				return;
			}

		} else if (Matcher.isFakeUUID(args[0])) {

			if (Matcher.isUUID(args[0])) {
				MutePlayer.addMute(author, sender, null, UUID.fromString(args[0]), args, olympaPlayer);
			} else {
				this.sendMessage(Prefix.DEFAULT_BAD, config.getString("default.uuidinvalid").replaceAll("%uuid%", args[0]));
				return;
			}

		} else {
			this.sendMessage(Prefix.DEFAULT_BAD, config.getString("default.typeunknown").replaceAll("%type%", args[0]));
			return;
		}
	}
}
