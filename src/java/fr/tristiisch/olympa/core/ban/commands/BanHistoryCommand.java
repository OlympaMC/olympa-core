package fr.tristiisch.olympa.core.ban.commands;

import java.util.UUID;

import fr.tristiisch.emeraldmc.api.bungee.commands.BungeeCommand;
import fr.tristiisch.emeraldmc.api.bungee.utils.BungeeConfigUtils;
import fr.tristiisch.emeraldmc.api.commons.Matcher;
import fr.tristiisch.emeraldmc.api.commons.object.EmeraldGroup;
import fr.tristiisch.olympa.core.ban.commands.methods.IdHistory;
import fr.tristiisch.olympa.core.ban.commands.methods.IpHistory;
import fr.tristiisch.olympa.core.ban.commands.methods.PlayerHistory;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Plugin;

public class BanHistoryCommand extends BungeeCommand {

	public BanHistoryCommand(final Plugin plugin) {
		super(plugin, "banhistory", EmeraldGroup.GUIDE, "banhist", "mutehist", "kickhist", "hist", "histban");
		this.minArg = 1;
		this.usageString = BungeeConfigUtils.getString("bungee.ban.messages.usagehistban");
		this.register();
	}

	@Override
	public void onCommand(final CommandSender sender, final String[] args) {

		if(Matcher.isInt(args[0])) {
			IdHistory.histban(sender, Integer.parseInt(args[0]));

		} else if(Matcher.isFakeIP(args[0])) {

			if(Matcher.isIP(args[0])) {
				IpHistory.histBan(sender, args[0]);
			} else {
				this.sendMessage(BungeeConfigUtils.getString("commun.messages.ipinvalid").replaceAll("%ip%", args[0]));
				return;
			}

		} else if(Matcher.isFakeUUID(args[0])) {

			if(Matcher.isUUID(args[0])) {
				PlayerHistory.histBan(sender, null, UUID.fromString(args[0]));
			} else {
				this.sendMessage(BungeeConfigUtils.getString("commun.messages.uuidinvalid").replaceAll("%uuid%", args[0]));
				return;
			}
		} else if(Matcher.isUsername(args[0])) {
			PlayerHistory.histBan(sender, args[0], null);

		} else {
			this.sendMessage(BungeeConfigUtils.getString("commun.messages.typeunknown").replaceAll("%type%", args[0]));
			return;
		}

	}

}
