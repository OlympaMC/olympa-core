package fr.olympa.core.bungee.ban.commands;

import java.sql.SQLException;
import java.util.List;

import fr.olympa.api.bungee.command.BungeeCommand;
import fr.olympa.api.common.chat.TxtComponentBuilder;
import fr.olympa.api.common.match.RegexMatcher;
import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.ban.BanMySQL;
import fr.olympa.core.bungee.ban.objects.OlympaSanction;
import fr.olympa.core.bungee.ban.objects.OlympaSanctionType;
import fr.olympa.core.common.permission.list.OlympaCorePermissionsBungee;
import fr.olympa.core.common.provider.AccountProvider;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Plugin;

public class BanListAuthorCommand extends BungeeCommand {

	public BanListAuthorCommand(Plugin plugin) {
		super(plugin, "banlistauthor", "Affiche les 10 dernières sanctions d'un modérateur", OlympaCorePermissionsBungee.BAN_BANLISTAUTHOR_COMMAND);
		addArgs(true, "JOUEUR");
		minArg = 0;
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		OlympaPlayer target = null;
		if (args.length != 0) {
			try {
				if (RegexMatcher.UUID.is(args[0]))
					target = new AccountProvider(RegexMatcher.UUID.parse(args[0])).get();
				else if (RegexMatcher.INT.is(args[0]))
					target = AccountProvider.getter().get(RegexMatcher.INT.parse(args[0]));
				else {
					target = AccountProvider.getter().get(args[0]);
					if (target == null) {
						sendUnknownPlayer(args[0], AccountProvider.getter().getSQL().getNamesBySimilarChars(args[0]));
						return;
					}
				}
			} catch (SQLException e) {
				sendError(e);
				e.printStackTrace();
				return;
			}
			if (target == null) {
				sendComponents(Prefix.DEFAULT_BAD.formatMessageB("&4%s n'est pas un argument de type UUID, INT ou Pseudo.", args[0]));
				return;
			}
		} else if (proxiedPlayer != null)
			target = getOlympaPlayer();
		else {
			sendImpossibleWithConsole();
			return;
		}
		List<OlympaSanction> sanctions;
		try {
			sanctions = BanMySQL.getSanctionByAuthor(target.getId());
		} catch (SQLException e) {
			sendError(e);
			e.printStackTrace();
			return;
		}
		TxtComponentBuilder builder = new TxtComponentBuilder(Prefix.DEFAULT_GOOD, "Toute les sanctions faites par %s : %d.", target.getName(), sanctions.size())
				.extraSpliterBN();
		for (OlympaSanction sanction : sanctions) {
			String names;
			try {
				names = sanction.getTargetsNames();
			} catch (SQLException e) {
				names = "sql error";
				e.printStackTrace();
			}
			builder.extra(new TxtComponentBuilder("%d - %s %s %s %s %s", sanction.getId(), sanction.getStatus().getNameColored(),
					sanction.getType().getName(!sanction.isPermanent()), names, sanction.getReason(),
					sanction.getExpires() > 0 ? Utils.timeToDuration(sanction.getBanTime()) : sanction.getType() != OlympaSanctionType.KICK ? "&cPermanant" : "")
					.onHoverText(sanction.toBaseComplement()).onClickCommand("/histban %d", sanction.getId()));
		}
		sender.sendMessage(builder.build());
	}
}
