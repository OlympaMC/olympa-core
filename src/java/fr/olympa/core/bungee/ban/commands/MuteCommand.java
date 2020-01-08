package fr.olympa.core.bungee.ban.commands;

import java.util.UUID;

import fr.olympa.api.objects.OlympaConsole;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.utils.Matcher;
import fr.olympa.core.bungee.api.command.BungeeCommand;
import fr.olympa.core.bungee.ban.commands.methods.MuteIp;
import fr.olympa.core.bungee.ban.commands.methods.MutePlayer;
import fr.olympa.core.bungee.utils.BungeeConfigUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

public class MuteCommand extends BungeeCommand {

	public MuteCommand(Plugin plugin) {
		super(plugin, "mute", OlympaCorePermissions.BAN_MUTE_COMMAND, "tempmute");
		this.minArg = 2;
		this.usageString = "<joueur|uuid|ip> [temps] <motif>";
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		UUID author;
		if (sender instanceof ProxiedPlayer) {
			author = this.proxiedPlayer.getUniqueId();
		} else {
			author = OlympaConsole.getUniqueId();
		}

		if (Matcher.isUsername(args[0])) {
			MutePlayer.addMute(author, sender, args[0], null, args, this.olympaPlayer);

		} else if (Matcher.isFakeIP(args[0])) {

			if (Matcher.isIP(args[0])) {
				MuteIp.addMute(author, sender, args[0], args, this.olympaPlayer);
			} else {
				this.sendMessage(BungeeConfigUtils.getString("commun.messages.ipinvalid").replaceAll("%ip%", args[0]));
				return;
			}

		} else if (Matcher.isFakeUUID(args[0])) {

			if (Matcher.isUUID(args[0])) {
				MutePlayer.addMute(author, sender, null, UUID.fromString(args[0]), args, this.olympaPlayer);
			} else {
				this.sendMessage(BungeeConfigUtils.getString("commun.messages.uuidinvalid").replaceAll("%uuid%", args[0]));
				return;
			}

		} else {
			this.sendMessage(BungeeConfigUtils.getString("commun.messages.typeunknown").replaceAll("%type%", args[0]));
			return;
		}
	}
}
