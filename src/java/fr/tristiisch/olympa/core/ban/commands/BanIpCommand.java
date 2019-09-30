package fr.tristiisch.olympa.core.ban.commands;

import java.util.UUID;

import fr.tristiisch.emeraldmc.api.bungee.commands.BungeeCommand;
import fr.tristiisch.emeraldmc.api.bungee.utils.BungeeConfigUtils;
import fr.tristiisch.emeraldmc.api.commons.Matcher;
import fr.tristiisch.emeraldmc.api.commons.datamanagment.sql.MySQL;
import fr.tristiisch.emeraldmc.api.commons.object.EmeraldConsole;
import fr.tristiisch.emeraldmc.api.commons.object.EmeraldGroup;
import fr.tristiisch.emeraldmc.api.commons.object.EmeraldPlayer;
import fr.tristiisch.olympa.core.ban.commands.methods.BanIp;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

@SuppressWarnings("deprecation")
public class BanIpCommand extends BungeeCommand {

	public BanIpCommand(final Plugin plugin) {
		super(plugin, "banip", EmeraldGroup.MODERATEUR, "tempbanip");
		this.minArg = 2;
		this.usageString = BungeeConfigUtils.getString("bungee.ban.messages.usageban");
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

			final EmeraldPlayer emeraldTarget = MySQL.getPlayer(args[0]);
			if(emeraldTarget != null) {
				BanIp.addBanIP(author, sender, emeraldTarget.getIp(), args, this.emeraldPlayer);
			}

		} else if(Matcher.isFakeIP(args[0])) {

			if(Matcher.isIP(args[0])) {
				BanIp.addBanIP(author, sender, args[0], args, this.emeraldPlayer);

			} else {
				sender.sendMessage(BungeeConfigUtils.getString("commun.messages.ipinvalid").replaceAll("%ip%", args[0]));
				return;
			}

		} else if(Matcher.isFakeUUID(args[0])) {

			if(Matcher.isUUID(args[0])) {
				final EmeraldPlayer emeraldTarget = MySQL.getPlayer(UUID.fromString(args[0]));
				if(emeraldTarget != null) {
					BanIp.addBanIP(author, sender, emeraldTarget.getIp(), args, this.emeraldPlayer);
				}

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
