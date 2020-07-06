package fr.olympa.core.bungee.ban.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.sql.MySQL;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.api.command.BungeeCommand;
import fr.olympa.core.bungee.ban.SanctionUtils;
import fr.olympa.core.bungee.ban.objects.BanExecute;
import fr.olympa.core.bungee.ban.objects.OlympaSanctionType;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.TabExecutor;

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
		BanExecute banArg = SanctionUtils.formatArgs(args);
		banArg.setSanctionType(OlympaSanctionType.BAN);
		if (sender instanceof ProxiedPlayer)
			banArg.setAuthor((ProxiedPlayer) sender);
		banArg.execute();
	}

	@Override
	public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
		if (args.length == 1) {
			List<String> postentielNames = Utils.startWords(args[0], MySQL.getAllPlayersNames().stream().collect(Collectors.toList()));
			return postentielNames;
		} else if (args.length == 2) {
			/*
			 * String i = new String(); java.util.regex.Matcher matcher =
			 * Pattern.compile("[0-9]+").matcher(args[1]); if (matcher.find()) { i =
			 * matcher.group(); }
			 */
			List<String> units = new ArrayList<>();
			for (List<String> unit : SanctionUtils.units)
				/*
				 * for (String u : unit) { units.add( i + u); }
				 */
				units.addAll(unit);
			System.out.println("Unit: " + String.join(", ", units));
			return Utils.startWords(args[1], units);
		} else if (args.length == 3) {
			List<String> reasons = Arrays.asList("Cheat", "Insulte", "Provocation", "Spam", "Harcèlement");
			return Utils.startWords(args[2], reasons);
		}
		return new ArrayList<>();
	}
}
