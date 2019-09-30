package fr.tristiisch.olympa.core.ban.commands;

import java.util.List;
import java.util.UUID;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import fr.tristiisch.olympa.api.command.BanOlympaCommand;
import fr.tristiisch.olympa.api.objects.OlympaConsole;
import fr.tristiisch.olympa.api.objects.OlympaPlayer;
import fr.tristiisch.olympa.api.permission.OlympaPermission;
import fr.tristiisch.olympa.api.utils.Matcher;
import fr.tristiisch.olympa.core.ban.commands.methods.BanIp;
import fr.tristiisch.olympa.core.ban.commands.methods.BanPlayer;

public class BanCommand extends BanOlympaCommand {

	public BanCommand(final Plugin plugin) {
		super(plugin, "ban", OlympaPermission.BAN_COMMAND, "tempban");
		this.minArg = 2;
		this.usageString = "<joueur|uuid|ip> [temps] <motif>";
		this.register();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		UUID author;
		OlympaPlayer olympaPlayer = this.getOlympaPlayer();
		if (sender instanceof Player) {
			author = this.player.getUniqueId();
		} else {
			author = OlympaConsole.getUniqueId();
		}

		final String arg = args[0];

		if (Matcher.isUsername(arg)) {
			BanPlayer.addBanUsername(author, sender, arg, null, args, olympaPlayer);

		} else if (Matcher.isFakeIP(arg)) {

			if (Matcher.isIP(arg)) {
				BanIp.addBanIP(author, sender, arg, args, olympaPlayer);
			} else {
				this.sendMessage(BungeeConfigUtils.getString("commun.messages.ipinvalid").replaceAll("%ip%", arg));
				return true;
			}

		} else if (Matcher.isFakeUUID(arg)) {

			if (Matcher.isUUID(arg)) {
				BanPlayer.addBanUsername(author, sender, null, UUID.fromString(arg), args, olympaPlayer);
			} else {
				this.sendMessage(BungeeConfigUtils.getString("commun.messages.uuidinvalid").replaceAll("%uuid%", arg));
				return true;
			}

		} else {
			this.sendMessage(BungeeConfigUtils.getString("commun.messages.typeunknown").replaceAll("%type%", arg));
			return;
		}
	}

	@Override
	public List<String> onTabComplete(org.bukkit.command.CommandSender sender, Command cmd, String label, String[] args) {
		// TODO Auto-generated method stub
		return null;
	}
}
