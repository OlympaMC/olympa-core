package fr.tristiisch.olympa.core.ban.commands;

import java.util.UUID;

import fr.tristiisch.emeraldmc.api.bungee.commands.BungeeCommand;
import fr.tristiisch.emeraldmc.api.bungee.utils.BungeeConfigUtils;
import fr.tristiisch.emeraldmc.api.commons.Matcher;
import fr.tristiisch.emeraldmc.api.commons.object.EmeraldConsole;
import fr.tristiisch.emeraldmc.api.commons.object.EmeraldGroup;
import fr.tristiisch.olympa.core.ban.commands.methods.UnbanIp;
import fr.tristiisch.olympa.core.ban.commands.methods.UnbanPlayer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

@SuppressWarnings("deprecation")
public class UnbanCommand extends BungeeCommand {

	public UnbanCommand(final Plugin plugin) {
		super(plugin, "unban", EmeraldGroup.RESPMODO, "uban");
		this.usageString = BungeeConfigUtils.getString("bungee.ban.messages.usageunban");
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

		if(Matcher.isFakeIP(args[0])) {
			if(Matcher.isIP(args[0])) {
				UnbanIp.unBan(author, sender, args[0], args);

			} else {
				sender.sendMessage(BungeeConfigUtils.getString("commun.messages.ipinvalid").replaceAll("%ip%", args[0]));
				return;
			}

		} else if(Matcher.isUsername(args[0])) {
			UnbanPlayer.unBan(author, sender, null, args[0], args);

		} else if(Matcher.isFakeUUID(args[0])) {

			if(Matcher.isUUID(args[0])) {
				UnbanPlayer.unBan(author, sender, UUID.fromString(args[0]), null, args);

			} else {
				sender.sendMessage(BungeeConfigUtils.getString("commun.messages.uuidinvalid").replaceAll("%uuid%", args[0]));
				return;
			}
		} else {
			sender.sendMessage(BungeeConfigUtils.getString("commun.messages.typeunknown").replaceAll("%type%", args[0]));
			return;
		}

	}

}
