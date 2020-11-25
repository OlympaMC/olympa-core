package fr.olympa.core.bungee.ban.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fr.olympa.api.bungee.command.BungeeCommand;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.sql.MySQL;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.ban.SanctionUtils;
import fr.olympa.core.bungee.ban.execute.SanctionExecute;
import fr.olympa.core.bungee.ban.objects.OlympaSanctionStatus;
import fr.olympa.core.bungee.ban.objects.OlympaSanctionType;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.TabExecutor;

public class BanCommand extends BungeeCommand implements TabExecutor {

	public BanCommand(Plugin plugin) {
		super(plugin, "ban", OlympaCorePermissions.BAN_BAN_COMMAND, "tempban");
		minArg = 2;
		usageString = "<joueur|uuid|id|ip> [temps] <motif>";
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		SanctionExecute banArg = SanctionExecute.formatArgs(args);
		banArg.setSanctionType(OlympaSanctionType.BAN);
		if (sender instanceof ProxiedPlayer)
			banArg.setAuthor(getOlympaPlayer());
		banArg.launchSanction(this, OlympaSanctionStatus.ACTIVE);
	}

	@Override
	public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
		switch (args.length) {
		case 1:
			return Utils.startWords(args[0], MySQL.getNamesBySimilarName(args[0]));
		case 2:
			List<String> units = new ArrayList<>();
			for (List<String> unit : SanctionUtils.units)
				for (String u : unit)
					for (int i = 1; i < 20; i++)
						units.add(i + u);
			return Utils.startWords(args[1], units);
		case 3:
			List<String> reasons = Arrays.asList("Cheat", "Insulte", "Provocation", "Spam", "Harcèlement", "Publicité");
			return Utils.startWords(args[2], reasons);
		default:
			return new ArrayList<>();
		}
	}
}
