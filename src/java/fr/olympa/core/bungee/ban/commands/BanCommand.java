package fr.olympa.core.bungee.ban.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fr.olympa.api.bungee.command.BungeeCommand;
import fr.olympa.api.common.match.RegexMatcher;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.ban.SanctionUtils;
import fr.olympa.core.bungee.ban.execute.SanctionExecute;
import fr.olympa.core.bungee.ban.objects.OlympaSanctionStatus;
import fr.olympa.core.bungee.ban.objects.OlympaSanctionType;
import fr.olympa.core.common.permission.list.OlympaCorePermissionsBungee;
import fr.olympa.core.common.provider.AccountProvider;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Plugin;

public class BanCommand extends BungeeCommand {

	public BanCommand(Plugin plugin) {
		super(plugin, "ban", OlympaCorePermissionsBungee.BAN_BAN_COMMAND, "tempban", "bann");
		minArg = 2;
		usageString = "<joueur|uuid|id|ip> [temps] <motif>";
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		SanctionExecute banArg = SanctionExecute.formatArgs(this, args);
		banArg.setSanctionType(OlympaSanctionType.BAN);
		banArg.launchSanction(OlympaSanctionStatus.ACTIVE);
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, BungeeCommand command, String[] args) {
		switch (args.length) {
		case 1:
			return Utils.startWords(args[0], AccountProvider.getter().getSQL().getNamesBySimilarName(args[0]));
		case 2:
			List<String> units = new ArrayList<>();
			if (args[1].isBlank())
				return Arrays.asList("1h", "2h", "3h", "6h", "12h", "1j", "2j", "3j", "7j", "1mo", "1an");
			if (RegexMatcher.INT.is(args[1])) {
				Integer time = RegexMatcher.INT.extractAndParse(args[1]);
				if (time == null)
					return null;
				for (List<String> unit : SanctionUtils.units)
					for (String u : unit)
						units.add(time + u);
				return Utils.startWords(args[1], units);
			}
			break;
		case 3:
			List<String> reasons = Arrays.asList("Cheat", "Insulte", "Provocation", "Spam", "Harcèlement", "Publicité");
			return Utils.startWords(args[2], reasons);
		}
		return new ArrayList<>();
	}
}
