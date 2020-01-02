package fr.olympa.core.ban.commands;

import java.util.UUID;

import fr.olympa.core.ban.commands.methods.KickIp;
import fr.olympa.core.ban.commands.methods.KickPlayer;
import fr.tristiisch.emeraldmc.api.bungee.commands.BungeeCommand;
import fr.tristiisch.emeraldmc.api.bungee.utils.BungeeConfigUtils;
import fr.tristiisch.emeraldmc.api.commons.Matcher;
import fr.tristiisch.emeraldmc.api.commons.object.EmeraldConsole;
import fr.tristiisch.emeraldmc.api.commons.object.EmeraldGroup;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

@SuppressWarnings("deprecation")
public class KickCommand extends BungeeCommand {

	public KickCommand(Plugin plugin) {
		super(plugin, "kick", EmeraldGroup.MODERATEUR, "eject");
		this.usageString = BungeeConfigUtils.getString("bungee.ban.messages.usagekick");
		this.minArg = 1;
		this.register();
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		UUID author;
		if(sender instanceof ProxiedPlayer) {
			author = this.proxiedPlayer.getUniqueId();
		} else {
			author = OlympaConsole.getUniqueId();
		}

		if(Matcher.isUsername(args[0])) {
			KickPlayer.addKick(author, sender, args[0], null, args, this.emeraldPlayer);

		} else if(Matcher.isFakeIP(args[0])) {

			if(Matcher.isIP(args[0])) {
				KickIp.addKick(author, sender, args[0], args, this.emeraldPlayer);
			} else {
				sender.sendMessage(BungeeConfigUtils.getString("commun.messages.ipinvalid").replaceAll("%ip%", args[0]));
				return;
			}

		} else if(Matcher.isFakeUUID(args[0])) {

			if(Matcher.isUUID(args[0])) {
				KickPlayer.addKick(author, sender, null, UUID.fromString(args[0]), args, this.emeraldPlayer);
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
