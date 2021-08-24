package fr.olympa.core.bungee.ban.commands;

import java.sql.SQLException;
import java.util.List;

import fr.olympa.api.bungee.command.BungeeCommand;
import fr.olympa.api.common.chat.TxtComponentBuilder;
import fr.olympa.api.common.match.RegexMatcher;
import fr.olympa.api.common.sanction.OlympaSanctionType;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.ban.BanMySQL;
import fr.olympa.core.bungee.ban.objects.OlympaSanction;
import fr.olympa.core.common.permission.list.OlympaCorePermissionsBungee;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Plugin;

public class BanListCommand extends BungeeCommand {

	public BanListCommand(Plugin plugin) {
		super(plugin, "banlist", "Affiche les 10 dernières sanctions", OlympaCorePermissionsBungee.BAN_BANLIST_COMMAND);
		addArgs(false, "10", "20", "30", "40", "50");
		minArg = 0;
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		List<OlympaSanction> sanctions = null;
		int numbers = 10;
		if (args.length != 0 && RegexMatcher.INT.is(args[0]))
			numbers = RegexMatcher.INT.parse(args[0]);
		try {
			sanctions = BanMySQL.getLastSanctions(numbers);
		} catch (SQLException e) {
			sendError(e);
			e.printStackTrace();
			return;
		}
		TxtComponentBuilder builder = new TxtComponentBuilder(Prefix.DEFAULT_GOOD, "Les %s dernières sanctions.", numbers != sanctions.size() ? numbers + "(" + sanctions.size() + ")" : String.valueOf(numbers))
				.extraSpliterBN();
		for (OlympaSanction sanction : sanctions) {
			String names;
			try {
				names = sanction.getPlayersNames();
			} catch (SQLException e) {
				names = "sql error";
				e.printStackTrace();
			}
			builder.extra(new TxtComponentBuilder("#%d - %s %s %s %s %s", sanction.getId(), sanction.getStatus().getNameColored(),
					sanction.getType().getName(!sanction.isPermanent()), names, sanction.getReason(),
					sanction.getExpires() > 0 ? Utils.timeToDuration(sanction.getBanTime()) : sanction.isPermanent() ? "&cPermanant" : "")
					.onHoverText(sanction.toBaseComplement()).onClickCommand("/histban %d", sanction.getId()));
		}
		sender.sendMessage(builder.build());
	}
}
