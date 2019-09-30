package fr.tristiisch.olympa.core.ban.commands;

import java.util.UUID;

import fr.tristiisch.emeraldmc.api.bungee.commands.BungeeCommand;
import fr.tristiisch.emeraldmc.api.bungee.utils.BungeeConfigUtils;
import fr.tristiisch.emeraldmc.api.commons.Matcher;
import fr.tristiisch.emeraldmc.api.commons.object.EmeraldConsole;
import fr.tristiisch.emeraldmc.api.commons.object.EmeraldGroup;
import fr.tristiisch.olympa.core.ban.commands.methods.UnmutePlayer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

public class UnmuteCommand extends BungeeCommand {

	public UnmuteCommand(final Plugin plugin) {
		super(plugin, "unmute", EmeraldGroup.MODERATEUR, "umute");
		this.usageString = BungeeConfigUtils.getString("bungee.ban.messages.usageunmute");
		this.minArg = 2;
		this.register();
	}

	@Override
	public void onCommand(final CommandSender sender, final String[] args) {
		UUID author;
		if(sender instanceof ProxiedPlayer) {
			author = this.proxiedPlayer.getUniqueId();
		} else {
			author = OlympaConsole.getUniqueId();
		}

		if(Matcher.isUsername(args[0])) {
			UnmutePlayer.unBan(author, sender, null, args[0], args);

		} else if(Matcher.isFakeUUID(args[0])) {
			if(Matcher.isUUID(args[0])) {
				UnmutePlayer.unBan(author, sender, UUID.fromString(args[0]), null, args);

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
