package fr.olympa.core.spigot.report;

import java.util.List;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.common.chat.TxtComponentBuilder;
import fr.olympa.api.common.command.PaginatorDatabase;
import fr.olympa.api.common.player.OlympaPlayerInformations;
import fr.olympa.api.common.report.OlympaReport;
import fr.olympa.api.common.report.ReportStatus;
import fr.olympa.api.common.report.ReportStatusInfo;
import fr.olympa.api.common.report.ReportUtils;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.common.permission.list.OlympaCorePermissionsBungee;
import fr.olympa.core.common.permission.list.OlympaCorePermissionsSpigot;
import fr.olympa.core.common.provider.AccountProvider;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.core.spigot.redis.RedisSpigotSend;
import fr.olympa.core.spigot.report.connections.ReportMySQL;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.Server;

public class ReportMsg {

	//	private static Cache<String, List<OlympaReport>> cache = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.MINUTES).build();

	public static void sendAlert(OlympaReport report, String authorName, String targetName, String targetServer) {
		BaseComponent out;
		if (LinkSpigotBungee.getInstance().isSpigot()) {
			out = ReportUtils.getAlert(report, authorName, targetName, targetServer, OlympaCore.getInstance().getServerName());
			Bukkit.getConsoleSender().spigot().sendMessage(out);
			OlympaCorePermissionsSpigot.REPORT_SEE_NOTIF.getPlayers(players -> players.forEach(p -> p.spigot().sendMessage(out)));
		} else {
			ProxyServer.getInstance().getConsole().sendMessage(ReportUtils.getAlert(report, authorName, targetName, targetServer, null));
			OlympaCorePermissionsBungee.REPORT_SEEREPORT_OTHERSERV.getPlayersBungee(players -> {
				if (players != null)
					players.forEach(p -> {
						Server staffServer = p.getServer();
						if (staffServer == null || !staffServer.getInfo().getName().equals(targetServer))
							p.sendMessage(ReportUtils.getAlert(report, authorName, targetName, targetServer, p.getServer().getInfo().getName()));
					});
			});
		}
	}

	public static void sendAlert(OlympaReport report) {
		OlympaPlayerInformations targetOlympaPlayer;
		targetOlympaPlayer = AccountProvider.getter().getPlayerInformations(report.getTargetId());
		report.resolveAuthorName();
		RedisSpigotSend.askPlayerServer(targetOlympaPlayer.getUUID(), t -> sendAlert(report, report.getAuthorName(), targetOlympaPlayer.getName(), t));
	}

	public static void sendPanelId(CommandSender sender, OlympaReport report) {
		report.resolveAll();
		TxtComponentBuilder out = new TxtComponentBuilder(Prefix.DEFAULT_GOOD, "Report de &2%s -> &2%s :", report.getAuthorName(), report.getTargetName()).extraSpliterBN();
		String id = String.valueOf(report.getId());
		out.extra("&aN??&2%s", id);
		out.extra("&aStatut %s", report.getStatus().getNameColored());
		out.extra("&aServeur &2%s", report.getServerName());
		out.extra("&aRaison &2%s", report.getReasonName());
		String note = report.getNote();
		if (note != null && !note.isBlank())
			out.extra("&aNote &2%s", note);
		out.extra("&aDate &2%s &a(%s)", Utils.timestampToDateAndHour(report.getTime()), Utils.timestampToDuration(report.getTime()));
		out.extra(new TxtComponentBuilder().extraSpliter(" ").extra(new TxtComponentBuilder("&6[&eChanger Statut]").onClickSuggest("/report change " + id + " "),
				new TxtComponentBuilder("&6[&eTous&6]").onClickCommand("/report see " + report.getTargetName())));
		List<ReportStatusInfo> statusInfo = report.getStatusInfo();
		//		if (statusInfo.size() > 1) {
		//			out.extra(new TxtComponentBuilder("&aDerniers status &2"));
		//			int i = 0;
		//			for (ReportStatusInfo info : statusInfo) {
		//				TxtComponentBuilder line = new TxtComponentBuilder(report.getStatus().getNameColored());
		//				line.onHoverText(String.join("\n", info.getLore()));
		//				if (i++ < statusInfo.size())
		//					out.extra(new TxtComponentBuilder("&a, "));
		//				else
		//					out.extra(new TxtComponentBuilder("&a."));
		//				out.extra(line);
		//			}
		//
		//		}

		if (statusInfo.size() > 1)
			out.extra(new TxtComponentBuilder("&aStatuts pr??c??dent &2%s", statusInfo.stream().limit(statusInfo.size() - 1l)
					.map(rsi -> rsi.getStatus().getNameColored() + (!Objects.isNull(rsi.getTime()) ? " &a(" + Utils.timestampToDuration(rsi.getTime()) + ")" : ""))
					.collect(Collectors.joining("&a, &2")))).extraSpliterBN();
		sender.spigot().sendMessage(out.build());
	}

	public static void sendPanelTarget(CommandSender sender, OlympaPlayerInformations target, int page) {
		PaginatorDatabase<OlympaReport> paginator = new PaginatorDatabase<>(10, String.format("Reports contre %s", target.getName()), ReportMySQL.TABLE, ReportMySQL.COLUMN_TIME, false) {
			@Override
			protected BaseComponent getObjectDescription(OlympaReport r) {
				r.resolveAll();
				ReportStatus status = r.getStatus();
				TxtComponentBuilder txtBuildeur = new TxtComponentBuilder("#%d %s%s -> %s &e(%s) %s", r.getId(), status.getColor(), r.getReasonNameUpper(), status.getName(), r.getAuthorName(), Utils.tsToShortDur(r.getLastUpdate()));
				txtBuildeur.onHoverText(String.join("\n", r.getLore()));
				txtBuildeur.onClickCommand("/report seeId " + r.getId());
				return txtBuildeur.build();
			}

			@Override
			protected String getCommand(int page) {
				return "/report see " + target.getName() + " " + page;
			}
		};
		paginator.setWhat(ReportMySQL.COLUMN_TARGET_ID, target.getId());
		OlympaCore.getInstance().getTask().runTaskAsynchronously(() -> sender.spigot().sendMessage(paginator.getPage(page)));
		//		TxtComponentBuilder out = new TxtComponentBuilder(String.format("Report%s contre %s (%d):", Utils.withOrWithoutS(reports.size()), target, reports.size())).extraSpliterBN();
		//		reports.stream().forEach(r -> {
		//			r.resolveAuthorName();
		//			ReportStatus status = r.getStatus();
		//			TxtComponentBuilder line = new TxtComponentBuilder("%s%s -> %s &e(%s) %s", status.getColor(), r.getReasonNameUpper(), status.getName(), r.getAuthorName(), Utils.tsToShortDur(r.getLastUpdate()));
		//			line.onHoverText(String.join("\n", r.getLore()));
		//			line.onClickCommand("/report seeId " + r.getId());
		//			out.extra(line);
		//		});
	}

	//	public static void sendPanelLast(CommandSender sender, List<OlympaReport> reports, int page) {
	public static void sendPanelLast(CommandSender sender, int page) {
		PaginatorDatabase<OlympaReport> paginator = new PaginatorDatabase<>(10, "Derniers report", ReportMySQL.TABLE, ReportMySQL.COLUMN_TIME, false) {
			@Override
			protected BaseComponent getObjectDescription(OlympaReport r) {
				r.resolveAll();
				ReportStatus status = r.getStatus();
				TxtComponentBuilder txtBuildeur = new TxtComponentBuilder("#%d %s%s -> %s &e(%s) de %s", r.getId(), status.getColor(), r.getReasonNameUpper(), status.getName(), r.getTargetName(), r.getAuthorName(),
						Utils.tsToShortDur(r.getLastUpdate()));
				txtBuildeur.onHoverText(String.join("\n", r.getLore()));
				txtBuildeur.onClickCommand("/report seeId " + r.getId());
				return txtBuildeur.build();
			}
			@Override
			protected String getCommand(int page) {
				return "/report seeLast " + page;
			}
		};
		OlympaCore.getInstance().getTask().runTaskAsynchronously(() -> sender.spigot().sendMessage(paginator.getPage(page)));
		//		Paginator<OlympaReport> paginator = new Paginator<>(10, "Derniers report") {
		//			@Override
		//			protected List<OlympaReport> getObjects() {
		//				return reports;
		//			}
		//
		//			@Override
		//			protected BaseComponent getObjectDescription(OlympaReport r) {
		//				r.resolveAll();
		//				ReportStatus status = r.getStatus();
		//				TxtComponentBuilder txtBuildeur = new TxtComponentBuilder("#%d %s%s -> %s &e(%s) de %s", r.getId(), status.getColor(), r.getReasonNameUpper(), status.getName(), r.getTargetName(), r.getAuthorName(),
		//						Utils.tsToShortDur(r.getLastUpdate()));
		//				txtBuildeur.onHoverText(String.join("\n", r.getLore()));
		//				txtBuildeur.onClickCommand("/report seeId " + r.getId());
		//				return txtBuildeur.build();
		//			}
		//
		//			@Override
		//			protected String getCommand(int page) {
		//				return "/report seeLast " + page;
		//			}
		//		};
		/*TxtComponentBuilder out = new TxtComponentBuilder(Prefix.DEFAULT_GOOD.formatMessage("%s Derniers report%s :", reports.size(), Utils.withOrWithoutS(reports.size()))).extraSpliterBN();
		reports.stream().forEach(r -> {
			r.resolveAll();
			ReportStatus status = r.getStatus();
			TxtComponentBuilder txtBuildeur = new TxtComponentBuilder("%s%s -> %s &e(%s) de %s", status.getColor(), r.getReasonNameUpper(), status.getName(), r.getTargetName(), r.getAuthorName(),
					Utils.tsToShortDur(r.getLastUpdate()));
			txtBuildeur.onHoverText(String.join("\n", r.getLore()));
			txtBuildeur.onClickCommand("/report seeId " + r.getId());
			out.extra(txtBuildeur);
		});
		sender.spigot().sendMessage(out.build());*/
	}

	public static void sendPanelMax(CommandSender sender, Stream<Entry<OlympaPlayerInformations, List<OlympaReport>>> reports) {
		TxtComponentBuilder out = new TxtComponentBuilder(Prefix.DEFAULT_GOOD.formatMessage("Joueurs connect??s avec le plus de report non trait??s :")).extraSpliterBN();
		reports.sorted((e1, e2) -> e1.getValue().size() - e2.getValue().size()).limit(10).forEach(entry -> {
			int rSize = entry.getValue().size();
			OlympaPlayerInformations opTarget = entry.getKey();
			TxtComponentBuilder line = new TxtComponentBuilder("&2%s &asignalement ouverts &2%s", rSize, opTarget.getName());
			OlympaReport firstReport = entry.getValue().get(entry.getValue().size() - 1);
			List<String> lore = firstReport.getLore();
			lore.add("\n&4Clique pour voir tous les reports de &4" + opTarget.getName() + "&c !");
			line.onHoverText(String.join("\n", lore));
			line.onClickCommand("/report see " + opTarget.getName());
			out.extra(line);
		});
		sender.spigot().sendMessage(out.build());
	}

	public static void sendPanelAuthor(CommandSender sender, OlympaPlayerInformations target, int page) {
		PaginatorDatabase<OlympaReport> paginator = new PaginatorDatabase<>(10, String.format("Reports de %s", target.getName()), ReportMySQL.TABLE, ReportMySQL.COLUMN_TIME, false) {
			@Override
			protected BaseComponent getObjectDescription(OlympaReport r) {
				r.resolveAll();
				ReportStatus status = r.getStatus();
				TxtComponentBuilder txtBuildeur = new TxtComponentBuilder("%s%s -> %s &e(%s) %s", status.getColor(), r.getReasonNameUpper(), status.getName(), r.getTargetName(), Utils.tsToShortDur(r.getLastUpdate()));
				txtBuildeur.onHoverText(String.join("\n", r.getLore()));
				txtBuildeur.onClickCommand("/report seeId " + r.getId());
				return txtBuildeur.build();
			}

			@Override
			protected String getCommand(int page) {
				return "/report seeAuthor " + target.getName() + " " + page;
			}
		};
		paginator.setWhat(ReportMySQL.COLUMN_AUTHOR_ID, target.getId());
		OlympaCore.getInstance().getTask().runTaskAsynchronously(() -> sender.spigot().sendMessage(paginator.getPage(page)));
		//		TxtComponentBuilder out = new TxtComponentBuilder(Prefix.DEFAULT_GOOD.formatMessage("Report%s de %s (%d) :", Utils.withOrWithoutS(reports.size()), author, reports.size())).extraSpliterBN();
		//		reports.stream().forEach(r -> {
		//			r.resolveTargetName();
		//			ReportStatus status = r.getStatus();
		//			TxtComponentBuilder line = new TxtComponentBuilder("%s%s <- %s &e(%s)", status.getColor(), r.getReasonNameUpper(), status.getName(), r.getTargetName(), Utils.timestampToDuration(r.getLastUpdate(), 1));
		//			line.onHoverText(String.join("\n", r.getLore()));
		//			line.onClickCommand("/report seeId " + r.getId());
		//			out.extra(line);
		//		});
		//		sender.spigot().sendMessage(out.build());
	}

}
