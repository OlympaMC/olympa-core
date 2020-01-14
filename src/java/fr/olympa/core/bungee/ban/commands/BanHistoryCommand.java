package fr.olympa.core.bungee.ban.commands;

import java.util.UUID;

import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.utils.Matcher;
import fr.olympa.core.bungee.api.command.BungeeCommand;
import fr.olympa.core.bungee.ban.commands.methods.IdHistory;
import fr.olympa.core.bungee.ban.commands.methods.IpHistory;
import fr.olympa.core.bungee.ban.commands.methods.PlayerHistory;
import fr.olympa.core.bungee.utils.BungeeConfigUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Plugin;

public class BanHistoryCommand extends BungeeCommand {

	public BanHistoryCommand(Plugin plugin) {
		super(plugin, "banhistory", OlympaCorePermissions.BAN_HISTORY_COMMAND, "banhist", "mutehist", "kickhist", "hist", "histban");
		this.minArg = 1;
		this.usageString = BungeeConfigUtils.getString("bungee.ban.messages.usagehistban");
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {

		if (Matcher.isInt(args[0])) {
			IdHistory.histban(sender, Integer.parseInt(args[0]));

		} else if (Matcher.isFakeIP(args[0])) {

			if (Matcher.isIP(args[0])) {
				IpHistory.histBan(sender, args[0]);
			} else {
				this.sendMessage(BungeeConfigUtils.getString("default.messages.ipinvalid").replaceAll("%ip%", args[0]));
				return;
			}

		} else if (Matcher.isFakeUUID(args[0])) {

			if (Matcher.isUUID(args[0])) {
				PlayerHistory.histBan(sender, null, UUID.fromString(args[0]));
			} else {
				this.sendMessage(BungeeConfigUtils.getString("default.messages.uuidinvalid").replaceAll("%uuid%", args[0]));
				return;
			}
		} else if (Matcher.isUsername(args[0])) {
			PlayerHistory.histBan(sender, args[0], null);

		} else {
			this.sendMessage(BungeeConfigUtils.getString("default.messages.typeunknown").replaceAll("%type%", args[0]));
			return;
		}

	}

}
