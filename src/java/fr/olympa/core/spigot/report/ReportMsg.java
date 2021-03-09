package fr.olympa.core.spigot.report;

import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.chat.TxtComponentBuilder;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.player.OlympaPlayerInformations;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.report.OlympaReport;
import fr.olympa.api.report.ReportReason;
import fr.olympa.api.report.ReportStatus;
import fr.olympa.api.report.ReportStatusInfo;
import fr.olympa.api.report.ReportUtils;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.core.spigot.redis.RedisSpigotSend;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;

@SuppressWarnings("deprecation")
public class ReportMsg {

	//	private static Cache<String, List<OlympaReport>> cache = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.MINUTES).build();

	public static void sendAlert(OlympaReport report, String authorName, String targetName, String targetServer) {
		BaseComponent out;
		if (LinkSpigotBungee.Provider.link.isSpigot()) {
			out = ReportUtils.getAlert(report, authorName, targetName, targetServer, OlympaCore.getInstance().getServerName());
			Bukkit.getConsoleSender().spigot().sendMessage(out);
			OlympaCorePermissions.REPORT_SEEREPORT.getPlayers(players -> {
				players.forEach(p -> p.spigot().sendMessage(out));
			});
		} else {
			ProxyServer.getInstance().getConsole().sendMessage(ReportUtils.getAlert(report, authorName, targetName, targetServer, null));
			OlympaCorePermissions.REPORT_SEEREPORT_OTHERSERV.getPlayersBungee(players -> {
				if (players != null)
					players.forEach(p -> p.sendMessage(ReportUtils.getAlert(report, authorName, targetName, targetServer, p.getServer().getInfo().getName())));
			});
		}
	}

	public static void sendAlert(OlympaReport report) {
		OlympaPlayerInformations targetOlympaPlayer;
		OlympaPlayerInformations authorOlympaPlayer;
		targetOlympaPlayer = AccountProvider.getPlayerInformations(report.getTargetId());
		authorOlympaPlayer = AccountProvider.getPlayerInformations(report.getAuthorId());
		RedisSpigotSend.askPlayerServer(targetOlympaPlayer.getUUID(), t -> sendAlert(report, authorOlympaPlayer.getName(), targetOlympaPlayer.getName(), t));
	}

	public static void sendPanelId(CommandSender sender, OlympaReport report) {
		OlympaPlayerInformations opTarget = AccountProvider.getPlayerInformations(report.getTargetId());
		OlympaPlayerInformations opAuthor = AccountProvider.getPlayerInformations(report.getAuthorId());
		TxtComponentBuilder out = new TxtComponentBuilder(Prefix.DEFAULT_GOOD, "Report de &2%s -> &2%s :", opAuthor.getName(), opTarget.getName()).extraSpliterBN();
		ReportReason reason = report.getReason();

		out.extra(new TxtComponentBuilder("&aN°&2%s", String.valueOf(report.getId())));
		out.extra(new TxtComponentBuilder("&aStatus %s", report.getStatus().getNameColored()));
		out.extra(new TxtComponentBuilder("&aServeur &2%s", report.getServerName()));
		out.extra(new TxtComponentBuilder("&aRaison &2%s", reason.getReason()));
		String note = report.getNote();
		if (note != null && !note.isBlank())
			out.extra(new TxtComponentBuilder("&aNote &2%s", note));
		out.extra(new TxtComponentBuilder("&aDate &2%s &a(%s)", Utils.timestampToDateAndHour(report.getTime()), Utils.timestampToDuration(report.getTime())));
		List<ReportStatusInfo> statusInfo = report.getStatusInfo();
		if (statusInfo.size() > 1) {
			out.extra(new TxtComponentBuilder("&aDerniers statuts &2"));
			int i = 0;
			for (ReportStatusInfo info : statusInfo) {
				TxtComponentBuilder line = new TxtComponentBuilder(report.getStatus().getNameColored());
				line.onHoverText(String.join("\n", info.getLore()));
				if (i++ < statusInfo.size())
					out.extra(new TxtComponentBuilder("&a, "));
				else
					out.extra(new TxtComponentBuilder("&a."));
				out.extra(line);
			}

		}
		out.extra(new TxtComponentBuilder("&aDerniers statuts &2%s",
				statusInfo.stream().skip(1).map(rsi -> rsi.getStatus() + " &a(" + Utils.timestampToDuration(rsi.getTime()) + ")").collect(Collectors.joining("&a, &2")))).extraSpliterBN();
		sender.spigot().sendMessage(out.build());
	}

	public static void sendPanelTarget(CommandSender sender, String target, List<OlympaReport> reports) {
		TxtComponentBuilder out = new TxtComponentBuilder(Prefix.DEFAULT_GOOD.formatMessage("Report contre %s :", target));
		reports.stream().forEach(r -> {
			OlympaPlayerInformations opTarget = AccountProvider.getPlayerInformations(r.getTargetId());
			ReportStatus status = r.getStatus();
			ReportReason reason = r.getReason();
			TxtComponentBuilder line = new TxtComponentBuilder("%s%s -> %s &e(%s) %s", status.getColor(), reason.getReasonUpper(), status.getName(), opTarget.getName(), Utils.tsToShortDur(r.getLastUpdate()));
			line.onHoverText(String.join("\n", r.getLore()));
			line.onClickCommand("/report seeid " + r.getId());
			out.extra(line);
		});
		sender.spigot().sendMessage(out.build());
	}

	public static void sendPanelLast(CommandSender sender, List<OlympaReport> reports) {
		TxtComponentBuilder out = new TxtComponentBuilder(Prefix.DEFAULT_GOOD.formatMessage("%s Derniers reports :", reports.size())).extraSpliterBN();
		reports.stream().forEach(r -> {
			OlympaPlayerInformations opTarget = AccountProvider.getPlayerInformations(r.getTargetId());
			OlympaPlayerInformations opAuthor = AccountProvider.getPlayerInformations(r.getAuthorId());
			ReportStatus status = r.getStatus();
			ReportReason reason = r.getReason();
			TxtComponentBuilder txtBuildeur = new TxtComponentBuilder("%s%s -> %s &e(%s) de %s", status.getColor(), reason.getReasonUpper(), status.getName(), opTarget.getName(), opAuthor.getName(),
					Utils.tsToShortDur(r.getLastUpdate()));
			txtBuildeur.onHoverText("\n", r.getLore());
			txtBuildeur.onClickCommand("/report seeid " + r.getId());
			out.extra(txtBuildeur);
		});
		sender.spigot().sendMessage(out.build());
	}

	public static void sendPanelMax(CommandSender sender, Stream<Entry<OlympaPlayerInformations, List<OlympaReport>>> reports) {
		TxtComponentBuilder out = new TxtComponentBuilder(Prefix.DEFAULT_GOOD.formatMessage("Joueurs connectés avec le plus de report non observer :")).extraSpliterBN();
		reports.limit(10).forEach(entry -> {
			int rSize = entry.getValue().size();
			OlympaPlayerInformations opTarget = entry.getKey();
			TxtComponentBuilder line = new TxtComponentBuilder("&2%s &asignalement ouverts &2%s", rSize, opTarget.getName());
			OlympaReport firstReport = entry.getValue().get(entry.getValue().size() - 1);
			List<String> lore = firstReport.getLore();
			lore.add("\n&4Clic pour voir tous les reports de &4" + opTarget.getName() + "&c !");
			line.onHoverText(String.join("\n", lore));
			line.onClickCommand("/report see " + opTarget.getId());
			out.extra(line);
			out.extra("\n");
		});
		sender.spigot().sendMessage(out.build());
	}

	public static void sendPanelAuthor(CommandSender sender, String author, List<OlympaReport> reports) {
		TxtComponentBuilder out = new TxtComponentBuilder(Prefix.DEFAULT_GOOD.formatMessage("Report de %s :", author)).extraSpliterBN();
		reports.stream().forEach(r -> {
			OlympaPlayerInformations opAuthor = AccountProvider.getPlayerInformations(r.getAuthorId());
			ReportStatus status = r.getStatus();
			ReportReason reason = r.getReason();
			TxtComponentBuilder line = new TxtComponentBuilder("%s%s <- %s &e(%s)", status.getColor(), reason.getReasonUpper(), status.getName(), opAuthor.getName(), Utils.timestampToDuration(r.getLastUpdate(), 1));
			line.onHoverText(String.join("\n", r.getLore()));
			line.onClickCommand("/report seeid " + r.getId());
			out.extra(line);
		});
		sender.spigot().sendMessage(out.build());
	}

}
