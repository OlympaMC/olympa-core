package fr.olympa.core.bungee.ban.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import fr.olympa.api.objects.OlympaConsole;
import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.utils.Matcher;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.api.command.BungeeCommand;
import fr.olympa.core.bungee.ban.BanUtils;
import fr.olympa.core.bungee.ban.commands.methods.BanIp;
import fr.olympa.core.bungee.ban.commands.methods.BanPlayer;
import fr.olympa.core.bungee.utils.BungeeConfigUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;

public class BanCommand extends BungeeCommand {

	public static OlympaPermission permToBandef;

	public BanCommand(Plugin plugin) {
		super(plugin, "ban", OlympaCorePermissions.BAN_BAN_COMMAND, "tempban");
		permToBandef = OlympaCorePermissions.BAN_BANDEF_COMMAND;
		this.minArg = 2;
		this.usageString = "<joueur|uuid|ip> [temps] <motif>";
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		UUID author;
		OlympaPlayer olympaPlayer = this.getOlympaPlayer();
		if (sender instanceof Player) {
			author = ((Player) sender).getUniqueId();
		} else {
			author = OlympaConsole.getUniqueId();
		}

		String arg = args[0];

		if (Matcher.isUsername(arg)) {
			BanPlayer.addBanPlayer(author, sender, arg, null, args, olympaPlayer);

		} else if (Matcher.isFakeIP(arg)) {

			if (Matcher.isIP(arg)) {
				BanIp.addBanIP(author, sender, arg, args, olympaPlayer);
			} else {
				this.sendMessage(BungeeConfigUtils.getString("ban.ipinvalid").replace("%ip%", arg));
				return;
			}

		} else if (Matcher.isFakeUUID(arg)) {

			if (Matcher.isUUID(arg)) {
				BanPlayer.addBanPlayer(author, sender, null, UUID.fromString(arg), args, olympaPlayer);
			} else {
				this.sendMessage(BungeeConfigUtils.getString("ban.uuidinvalid").replace("%uuid%", arg));
				return;
			}

		} else {
			this.sendMessage(BungeeConfigUtils.getString("ban.typeunknown").replace("%type%", arg));
			return;
		}
		return;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 1) {
			List<String> postentielNames = Utils.startWords(args[0], Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()));
			return postentielNames;
		} else if (args.length == 2) {
			List<String> units = new ArrayList<>();
			for (List<String> unit : BanUtils.units) {
				units.addAll(unit);
			}
			return Utils.startWords(args[1], units);
		} else if (args.length == 3) {
			List<String> reasons = Arrays.asList("Cheat", "Insulte", "Provocation", "Spam", "Harcèlement");
			return Utils.startWords(args[1], reasons);
		}
		return null;
	}
}
