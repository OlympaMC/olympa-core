package fr.olympa.core.bungee.ban.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import fr.olympa.api.objects.OlympaConsole;
import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.utils.Matcher;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.api.command.BungeeCommand;
import fr.olympa.core.bungee.ban.BanUtils;
import fr.olympa.core.bungee.ban.commands.methods.BanIp;
import fr.olympa.core.bungee.ban.commands.methods.BanPlayer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.TabExecutor;
import net.md_5.bungee.config.Configuration;

public class BanCommand extends BungeeCommand implements TabExecutor {

	public static OlympaPermission permToBandef;

	public BanCommand(Plugin plugin) {
		super(plugin, "ban", OlympaCorePermissions.BAN_BAN_COMMAND, "tempban");
		permToBandef = OlympaCorePermissions.BAN_BANDEF_COMMAND;
		minArg = 2;
		usageString = "<joueur|uuid|ip> [temps] <motif>";
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		UUID author;
		OlympaPlayer olympaPlayer = getOlympaPlayer();
		if (sender instanceof ProxiedPlayer) {
			author = ((ProxiedPlayer) sender).getUniqueId();
		} else {
			author = OlympaConsole.getUniqueId();
		}

		String arg = args[0];

		Configuration config = OlympaBungee.getInstance().getConfig();
		if (Matcher.isUsername(arg)) {
			BanPlayer.addBanPlayer(author, sender, arg, null, args, olympaPlayer);

		} else if (Matcher.isFakeIP(arg)) {

			if (Matcher.isIP(arg)) {
				BanIp.addBanIP(author, sender, arg, args, olympaPlayer);
			} else {
				this.sendMessage(Prefix.DEFAULT_BAD, config.getString("default.ipinvalid").replace("%ip%", arg));
				return;
			}

		} else if (Matcher.isFakeUUID(arg)) {

			if (Matcher.isUUID(arg)) {
				BanPlayer.addBanPlayer(author, sender, null, UUID.fromString(arg), args, olympaPlayer);
			} else {
				this.sendMessage(Prefix.DEFAULT_BAD, config.getString("default.uuidinvalid").replace("%uuid%", arg));
				return;
			}

		} else {
			this.sendMessage(Prefix.DEFAULT_BAD, config.getString("default.typeunknown").replace("%type%", arg));
			return;
		}
		return;
	}

	@Override
	public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
		if (args.length == 1) {
			List<String> postentielNames = Utils.startWords(args[0], OlympaBungee.getInstance().getProxy().getPlayers().stream().map(ProxiedPlayer::getName).collect(Collectors.toSet()));
			return postentielNames;
		} else if (args.length == 2) {
			/*
			 * String i = new String(); java.util.regex.Matcher matcher =
			 * Pattern.compile("[0-9]+").matcher(args[1]); if (matcher.find()) { i =
			 * matcher.group(); }
			 */
			List<String> units = new ArrayList<>();
			for (List<String> unit : BanUtils.units) {
				/*
				 * for (String u : unit) { units.add( i + u); }
				 */
				units.addAll(unit);
			}
			System.out.println("Unit: " + String.join(", ", units));
			return Utils.startWords(args[1], units);
		} else if (args.length == 3) {
			List<String> reasons = Arrays.asList("Cheat", "Insulte", "Provocation", "Spam", "Harcèlement");
			return Utils.startWords(args[2], reasons);
		}
		return new ArrayList<>();
	}
}
