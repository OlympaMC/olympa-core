package fr.olympa.core.ban.commands;

import java.util.List;
import java.util.UUID;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.command.BanOlympaCommand;
import fr.olympa.api.objects.OlympaConsole;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.spigot.core.ban.commands.methods.MuteIp;
import fr.olympa.spigot.core.ban.commands.methods.MutePlayer;

public class MuteCommand extends BanOlympaCommand {

	public MuteCommand(Plugin plugin) {
		super(plugin, "mute", OlympaPermission.BAN_MUTE_COMMAND, "tempmute");
		this.setMinArg(2);
		this.setUsageString("<joueur|uuid|ip> [temps] <motif>");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		UUID author;
		if (sender instanceof ProxiedPlayer) {
			author = this.proxiedPlayer.getUniqueId();
		} else {
			author = OlympaConsole.getUniqueId();
		}

		if (Matcher.isUsername(args[0])) {
			MutePlayer.addMute(author, sender, args[0], null, args, this.emeraldPlayer);

		} else if (Matcher.isFakeIP(args[0])) {

			if (Matcher.isIP(args[0])) {
				MuteIp.addMute(author, sender, args[0], args, this.emeraldPlayer);
			} else {
				this.sendMessage(BungeeConfigUtils.getString("commun.messages.ipinvalid").replaceAll("%ip%", args[0]));
				return;
			}

		} else if (Matcher.isFakeUUID(args[0])) {

			if (Matcher.isUUID(args[0])) {
				MutePlayer.addMute(author, sender, null, UUID.fromString(args[0]), args, this.emeraldPlayer);
			} else {
				this.sendMessage(BungeeConfigUtils.getString("commun.messages.uuidinvalid").replaceAll("%uuid%", args[0]));
				return;
			}

		} else {
			this.sendMessage(BungeeConfigUtils.getString("commun.messages.typeunknown").replaceAll("%type%", args[0]));
			return;
		}
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		// TODO Auto-generated method stub
		return null;
	}
}
