package fr.olympa.core.spigot.report;

import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import fr.olympa.api.LinkSpigotBungee;
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
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

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
		TextComponent out = new TextComponent(TextComponent.fromLegacyText(Prefix.DEFAULT_GOOD.formatMessage("Report de &2%s -> &2%s :", opAuthor.getName(), opTarget.getName())));
		ReportReason reason = report.getReason();

		out.addExtra(new TextComponent(TextComponent.fromLegacyText(String.format("&aN°&2%s", String.valueOf(report.getId())))));
		out.addExtra(new TextComponent(TextComponent.fromLegacyText(String.format("&aStatus %s", report.getStatus().getNameColored()))));
		out.addExtra(new TextComponent(TextComponent.fromLegacyText(String.format("&aServeur &2%s", report.getServerName()))));
		out.addExtra(new TextComponent(TextComponent.fromLegacyText(String.format("&aRaison &2%s", reason.getReason()))));
		String note = report.getNote();
		if (note != null && !note.isBlank())
			out.addExtra(new TextComponent(TextComponent.fromLegacyText(String.format("&aNote &2%s", note))));
		out.addExtra(new TextComponent(TextComponent.fromLegacyText(String.format("&aDate &2%s &a(%s)", Utils.timestampToDateAndHour(report.getTime()), Utils.timestampToDuration(report.getTime())))));
		List<ReportStatusInfo> statusInfo = report.getStatusInfo();
		if (statusInfo.size() > 1) {
			out.addExtra(new TextComponent(TextComponent.fromLegacyText("&aDerniers statuts &2")));
			int i = 0;
			for (ReportStatusInfo info : statusInfo) {
				TextComponent line = new TextComponent(TextComponent.fromLegacyText(report.getStatus().getNameColored()));
				line.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(String.join("\n", info.getLore()))));
				if (i++ < statusInfo.size())
					out.addExtra(new TextComponent(TextComponent.fromLegacyText("&a, ")));
				else
					out.addExtra(new TextComponent(TextComponent.fromLegacyText("&a.")));
				out.addExtra(line);
			}

		}
		out.addExtra(new TextComponent(TextComponent.fromLegacyText(
				String.format("&aDerniers statuts &2%s",
						statusInfo.stream().skip(1).map(rsi -> rsi.getStatus() + " &a(" + Utils.timestampToDuration(rsi.getTime()) + ")").collect(Collectors.joining("&a, &2"))))));

		out.addExtra("\n");
		sender.spigot().sendMessage(out);
	}

	public static void sendPanelTarget(CommandSender sender, String target, List<OlympaReport> reports) {
		TextComponent out = new TextComponent(TextComponent.fromLegacyText(Prefix.DEFAULT_GOOD.formatMessage("Report contre %s :", target)));
		reports.stream().forEach(r -> {
			OlympaPlayerInformations opTarget = AccountProvider.getPlayerInformations(r.getTargetId());
			ReportStatus status = r.getStatus();
			ReportReason reason = r.getReason();
			TextComponent line = new TextComponent(TextComponent.fromLegacyText(
					String.format("%s%s <- %s &e(%s) %s", status.getColor(), reason.getReasonUpper(), status.getName(), opTarget.getName(), Utils.tsToShortDur(r.getLastUpdate()))));
			line.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(String.join("\n", r.getLore()))));
			line.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/report seeid " + r.getId()));
			out.addExtra(line);
			out.addExtra("\n");
		});
		sender.spigot().sendMessage(out);
	}

	public static void sendPanelLast(CommandSender sender, List<OlympaReport> reports) {
		TextComponent out = new TextComponent(TextComponent.fromLegacyText(Prefix.DEFAULT_GOOD.formatMessage("%s Derniers reports :", reports.size())));
		reports.stream().forEach(r -> {
			OlympaPlayerInformations opTarget = AccountProvider.getPlayerInformations(r.getTargetId());
			OlympaPlayerInformations opAuthor = AccountProvider.getPlayerInformations(r.getAuthorId());
			ReportStatus status = r.getStatus();
			ReportReason reason = r.getReason();
			TextComponent line = new TextComponent(TextComponent.fromLegacyText(
					String.format("%s%s -> %s &e(%s)", status.getColor(), reason.getReasonUpper(), status.getName(), opAuthor.getName(), opTarget.getName(), Utils.timestampToDuration(r.getLastUpdate(), 1))));
			line.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(String.join("\n", r.getLore()))));
			line.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/report seeid " + r.getId()));
			out.addExtra(line);
			out.addExtra("\n");
		});
		sender.spigot().sendMessage(out);
	}

	public static void sendPanelMax(CommandSender sender, Stream<Entry<OlympaPlayerInformations, List<OlympaReport>>> reports) {
		TextComponent out = new TextComponent(TextComponent.fromLegacyText(Prefix.DEFAULT_GOOD.formatMessage("Joueurs connectés avec le plus de report non observer :")));
		reports.limit(10).forEach(entry -> {
			int rSize = entry.getValue().size();
			OlympaPlayerInformations opTarget = entry.getKey();
			TextComponent line = new TextComponent(TextComponent.fromLegacyText(
					String.format("&2%s &asignalement ouverts &2%s", rSize, opTarget.getName())));
			OlympaReport firstReport = entry.getValue().get(entry.getValue().size() - 1);
			List<String> lore = firstReport.getLore();
			lore.add("\n&4Clic pour voir tous les reports de &4" + opTarget.getName() + "&c !");
			line.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(String.join("\n", lore))));
			line.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/report see " + opTarget.getId()));
			out.addExtra(line);
			out.addExtra("\n");
		});
		sender.spigot().sendMessage(out);
	}

	public static void sendPanelAuthor(CommandSender sender, String author, List<OlympaReport> reports) {
		TextComponent out = new TextComponent(TextComponent.fromLegacyText(Prefix.DEFAULT_GOOD.formatMessage("Report de %s :", author)));
		reports.stream().forEach(r -> {
			OlympaPlayerInformations opAuthor = AccountProvider.getPlayerInformations(r.getAuthorId());
			ReportStatus status = r.getStatus();
			ReportReason reason = r.getReason();
			TextComponent line = new TextComponent(TextComponent.fromLegacyText(
					String.format("%s%s -> %s &e(%s)", status.getColor(), reason.getReasonUpper(), status.getName(), opAuthor.getName(), Utils.timestampToDuration(r.getLastUpdate(), 1))));
			line.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(String.join("\n", r.getLore()))));
			line.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/report seeid " + r.getId()));
			out.addExtra(line);
			out.addExtra("\n");
		});
		sender.spigot().sendMessage(out);
	}

}
