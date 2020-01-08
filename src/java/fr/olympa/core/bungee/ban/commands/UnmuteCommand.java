package fr.olympa.core.bungee.ban.commands;

import java.util.UUID;

import fr.olympa.api.objects.OlympaConsole;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.utils.Matcher;
import fr.olympa.core.bungee.api.command.BungeeCommand;
import fr.olympa.core.bungee.ban.commands.methods.UnmutePlayer;
import fr.olympa.core.bungee.utils.BungeeConfigUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

public class UnmuteCommand extends BungeeCommand {

	public UnmuteCommand(Plugin plugin) {
		super(plugin, "unmute", OlympaCorePermissions.BAN_UNMUTE_COMMAND, "umute");
		this.usageString = BungeeConfigUtils.getString("ban.messages.usageunmute");
		this.minArg = 2;
		this.register();
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
			UnmutePlayer.unBan(author, sender, null, args[0], args);

		} else if (Matcher.isFakeUUID(args[0])) {
			if (Matcher.isUUID(args[0])) {
				UnmutePlayer.unBan(author, sender, UUID.fromString(args[0]), null, args);

			} else {
				this.sendMessage(BungeeConfigUtils.getString("default.messages.uuidinvalid").replace("%uuid%", args[0]));
				return;
			}
		} else {
			this.sendMessage(BungeeConfigUtils.getString("default.messages.typeunknown").replace("%type%", args[0]));
			return;
		}

	}
}
