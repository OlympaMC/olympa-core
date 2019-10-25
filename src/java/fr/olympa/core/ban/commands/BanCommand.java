package fr.olympa.core.ban.commands;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import fr.olympa.OlympaCore;
import fr.olympa.api.command.BanOlympaCommand;
import fr.olympa.api.config.CustomConfig;
import fr.olympa.api.objects.OlympaConsole;
import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.utils.Matcher;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.ban.BanUtils;
import fr.olympa.core.ban.commands.methods.BanIp;
import fr.olympa.core.ban.commands.methods.BanPlayer;

public class BanCommand extends BanOlympaCommand {

	public BanCommand(final Plugin plugin) {
		super(plugin, "ban", OlympaCorePermissions.BAN_BAN_COMMAND, "tempban");
		this.setMinArg(2);
		this.setUsageString("<joueur|uuid|ip> [temps] <motif>");
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
		CustomConfig config = OlympaCore.getInstance().getConfig();

		final String arg = args[0];

		if (Matcher.isUsername(arg)) {
			BanPlayer.addBanPlayer(author, sender, arg, null, args, olympaPlayer);

		} else if (Matcher.isFakeIP(arg)) {

			if (Matcher.isIP(arg)) {
				BanIp.addBanIP(author, sender, arg, args, olympaPlayer);
			} else {
				this.sendMessage(config.getString("ban.ipinvalid").replace("%ip%", arg));
				return true;
			}

		} else if (Matcher.isFakeUUID(arg)) {

			if (Matcher.isUUID(arg)) {
				BanPlayer.addBanPlayer(author, sender, null, UUID.fromString(arg), args, olympaPlayer);
			} else {
				this.sendMessage(config.getString("ban.uuidinvalid").replace("%uuid%", arg));
				return true;
			}

		} else {
			this.sendMessage(config.getString("ban.typeunknown").replace("%type%", arg));
			return true;
		}
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 1) {
			List<String> postentielNames = Utils.startWords(args[0], Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toSet()));
			return postentielNames;
		} else if (args.length == 2) {
			Set<String> units = new HashSet<>();
			for (List<String> unit : BanUtils.units) {
				units.addAll(unit);
			}
			return Utils.startWords(args[1], units);
		} else if (args.length == 3) {
			Set<String> reasons = new HashSet<>(Arrays.asList("Cheat", "Insulte", "Provocation", "Spam", "Harcèlement"));
			return Utils.startWords(args[1], reasons);
		}
		return null;
	}
}
