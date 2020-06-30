package fr.olympa.core.bungee.ban.commands;

import java.util.UUID;

import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.utils.Matcher;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.api.command.BungeeCommand;
import fr.olympa.core.bungee.ban.commands.methods.IdHistory;
import fr.olympa.core.bungee.ban.commands.methods.IpHistory;
import fr.olympa.core.bungee.ban.commands.methods.PlayerHistory;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.config.Configuration;

public class BanHistoryCommand extends BungeeCommand {
	
	public BanHistoryCommand(OlympaBungee plugin) {
		super(plugin, "banhistory", OlympaCorePermissions.BAN_HISTORY_COMMAND, "banhist", "mutehist", "kickhist", "hist", "histban");
		minArg = 1;
		Configuration config = plugin.getConfig();
		usageString = config.getString("ban.usagehistban");
	}
	
	@Override
	public void onCommand(CommandSender sender, String[] args) {
		
		Configuration config = OlympaBungee.getInstance().getConfig();
		if (Matcher.isInt(args[0]))
			IdHistory.histban(sender, Integer.parseInt(args[0]));
		else if (Matcher.isFakeIP(args[0])) {
			
			if (Matcher.isIP(args[0]))
				IpHistory.histBan(sender, args[0]);
			else {
				this.sendMessage(Prefix.DEFAULT_BAD, config.getString("default.ipinvalid").replaceAll("%ip%", args[0]));
				return;
			}
			
		} else if (Matcher.isFakeUUID(args[0])) {
			
			if (Matcher.isUUID(args[0]))
				PlayerHistory.histBan(sender, null, UUID.fromString(args[0]));
			else {
				this.sendMessage(Prefix.DEFAULT_BAD, config.getString("default.uuidinvalid").replaceAll("%uuid%", args[0]));
				return;
			}
		} else if (Matcher.isUsername(args[0]))
			PlayerHistory.histBan(sender, args[0], null);
		else {
			this.sendMessage(Prefix.DEFAULT_BAD, config.getString("default.typeunknown").replaceAll("%type%", args[0]));
			return;
		}
		
	}
	
}
