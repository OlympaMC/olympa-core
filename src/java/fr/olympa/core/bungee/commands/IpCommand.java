package fr.olympa.core.bungee.commands;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import fr.olympa.api.bungee.command.BungeeComplexCommand;
import fr.olympa.api.common.chat.TxtComponentBuilder;
import fr.olympa.api.common.command.complex.Cmd;
import fr.olympa.api.common.command.complex.CommandContext;
import fr.olympa.api.common.match.RegexMatcher;
import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.common.permission.list.OlympaCorePermissionsBungee;
import fr.olympa.core.common.provider.AccountProvider;
import net.md_5.bungee.api.plugin.Plugin;

public class IpCommand extends BungeeComplexCommand {

	public IpCommand(Plugin plugin) {
		super(plugin, "ip", "Informations sur les IP utilisées", OlympaCorePermissionsBungee.IP_COMMAND);
		minArg = 0;
	}

	@Cmd(otherArg = true, args = { "IP|OLYMPA_PLAYERS" })
	public void other(CommandContext cmd) {
		OlympaPlayer target = null;
		String potentielIP;
		boolean canSeeIP = OlympaCorePermissionsBungee.IP_COMMAND_SEE_IP.hasSenderPermissionBungee(sender);
		if (cmd.getArgumentsLength() != 0) {

			if (cmd.getArgument(0) instanceof OlympaPlayer) {
				target = cmd.getArgument(0);
				potentielIP = target.getIp();
			} else {
				potentielIP = cmd.getArgument(0);
				if (RegexMatcher.FAKE_IP.is(potentielIP))
					if (!RegexMatcher.IP.is(potentielIP)) {
						sendError("L'IP %s n'est pas valide. Elle doit suivre le format d'une IP classique tel que 172.0.0.1.", potentielIP);
						return;
					} else if (!canSeeIP) {
						sendDoNotHavePermission();
						return;
					}
			}
			//			potentielIP = cmd.getArgument(0);
			//			try {
			//				if (RegexMatcher.FAKE_IP.is(potentielIP)) {
			//					if (!RegexMatcher.IP.is(potentielIP)) {
			//						sendError("L'IP %s n'est pas valide. Elle doit suivre le format d'une IP classique tel que 172.0.0.1.", potentielIP);
			//						return;
			//					} else if (!canSeeIP) {
			//						sendDoNotHavePermission();
			//						return;
			//					}
			//				} else {
			//					target = AccountProvider.getter().get(potentielIP);
			//					if (target == null) {
			//						sendUnknownPlayer(potentielIP, MySQL.getNamesBySimilarChars(potentielIP));
			//						return;
			//					}
			//					potentielIP = target.getIp();
			//				}
			//			} catch (SQLException e) {
			//				sendError(e);
			//				e.printStackTrace();
			//				return;
			//			}
		} else if (proxiedPlayer != null) {
			target = getOlympaPlayer();
			potentielIP = target.getIp();
		} else {
			sendImpossibleWithConsole();
			return;
		}

		Map<Boolean, List<OlympaPlayer>> all;
		try {
			all = AccountProvider.getter().getSQL().getPlayersByAllIp(potentielIP);
		} catch (SQLException e) {
			sendError(e);
			e.printStackTrace();
			return;
		}
		//		targets = Stream.concat(all.get(true).stream(), all.get(false).stream()).collect(Collectors.toList());
		TxtComponentBuilder out = new TxtComponentBuilder(Prefix.DEFAULT_GOOD, "&6Info IP ", target != null && !canSeeIP ? target.getName() : potentielIP).extraSpliterBN();
		List<OlympaPlayer> tempTarget = all.get(true);
		if (!tempTarget.isEmpty()) {
			out.extra("&6Compte qui ont dernièrement utiliser la même IP :");
			out.extra("&7Pseudo         Group        Première co       Dernière co");
			for (OlympaPlayer t : tempTarget) {
				TxtComponentBuilder out2 = new TxtComponentBuilder("&e%s %s %s %s", t.getName(), t.getGroupNameColored(), Utils.tsToShortDur(t.getFirstConnection()),
						Utils.tsToShortDur(t.getLastConnection()));
				if (canSeeIP) {
					out2.onHoverText("&eClique pour copier l'IP &6" + t.getIp());
					out2.onClickCopy(t.getIp());
				}
				out.extra(out2);
			}
		}
		tempTarget = all.get(false);
		if (!tempTarget.isEmpty()) {
			out.extra("&6Compte qui ont déjà utiliser la même IP par le passé:");
			out.extra("&7Pseudo         Group        Première co       Dernière co");
			for (OlympaPlayer t : tempTarget) {
				TxtComponentBuilder out2 = new TxtComponentBuilder("&e%s %s %s %s", t.getName(), t.getGroupNameColored(), Utils.tsToShortDur(t.getFirstConnection()),
						Utils.tsToShortDur(t.getLastConnection()));
				if (canSeeIP) {
					out2.onHoverText("&eClique pour copier l'IP &6" + t.getIp());
					out2.onClickCopy(t.getIp());
				}
				out.extra(out2);
			}
		}
		sendMessage(out.build());
	}
	//
	//	@Override
	//	public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
	//		if (args.length == 1) {
	//			List<String> postentielNames;
	//			postentielNames = Utils.startWords(args[0], MySQL.getNamesBySimilarName(args[0]));
	//			return postentielNames;
	//		}
	//		return new ArrayList<>();
	//	}
}
