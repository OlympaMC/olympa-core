package fr.olympa.core.spigot.report.commands;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.common.command.complex.Cmd;
import fr.olympa.api.common.command.complex.CommandContext;
import fr.olympa.api.common.player.OlympaPlayerInformations;
import fr.olympa.api.common.report.OlympaReport;
import fr.olympa.api.common.report.ReportReason;
import fr.olympa.api.common.report.ReportStatus;
import fr.olympa.api.common.report.ReportStatusInfo;
import fr.olympa.api.spigot.command.ComplexCommand;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.common.permission.list.OlympaCorePermissionsSpigot;
import fr.olympa.core.common.provider.AccountProvider;
import fr.olympa.core.spigot.report.ReportHandler;
import fr.olympa.core.spigot.report.ReportMsg;
import fr.olympa.core.spigot.report.connections.ReportMySQL;
import fr.olympa.core.spigot.report.gui.ReportGui;
import fr.olympa.core.spigot.report.gui.ReportGuiChoose;
import fr.olympa.core.spigot.report.gui.ReportGuiConfirm;

public class ReportCommand extends ComplexCommand {

	public ReportCommand(Plugin plugin) {
		super(plugin, "report", "Signale un joueur.", OlympaCorePermissionsSpigot.REPORT_COMMAND, "signale");
		addArgumentParser("REPORTREASON", (sender, arg) -> ReportReason.values().stream().map(ReportReason::getReasonOneWord).toList(), x -> ReportReason.getByReason(x.replace("_", " ")),
				x -> String.format("&4%s&c doit être une raison tel que &4%s&c", x, ReportReason.values().stream().map(ReportReason::getReasonOneWord).collect(Collectors.joining(", "))));
		addArgumentParser("REPORTSTATUS", (sender, arg) -> Arrays.stream(ReportStatus.values()).map(ReportStatus::getName).toList(), x -> ReportStatus.get(x),
				x -> String.format("&4%s&c doit être un status tel que &4%s&c", x, Arrays.stream(ReportStatus.values()).map(ReportStatus::getName).collect(Collectors.joining(", "))));
	}

	@Override
	public boolean noArguments(CommandSender sender) {
		if (sender instanceof Player) {
			ReportGuiChoose.open(player);
			return true;
		}
		return false;
	}

	@Cmd(args = { "PLAYERS", "REPORTREASON", "Informations complémentaire du report" }, min = 1, syntax = "<joueur> [status] [note]", otherArg = true)
	public void otherArg(CommandContext cmd) {
		Player player = this.player;
		OfflinePlayer target = cmd.getArgument(0);
		if (player != null) {
			if (target.getUniqueId().equals(player.getUniqueId())) {
				sendError("Tu ne peux pas te report toi même.");
				return;
			}
			try {
				List<OlympaReport> allReportsAuthors = ReportMySQL.getReportsByAuthor(this.getOlympaPlayer().getId(), 100);
				if (allReportsAuthors.size() >= 2 && allReportsAuthors.get(1).getTime() > Utils.getCurrentTimeInSeconds() - 10 * 60) {
					sendError("Tu peux faire seulement 2 reports toutes les 10 minutes.");
					return;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		ReportReason reportReason = null;
		String note = null;
		if (cmd.getArgumentsLength() > 1)
			reportReason = cmd.getArgument(1);
		if (cmd.getArgumentsLength() > 2)
			note = cmd.getFrom(2);
		if (player == null)
			try {
				ReportHandler.report(player, target, reportReason, note);
			} catch (SQLException e) {
				sendError(e);
				e.printStackTrace();
			}
		else if (reportReason != null)
			ReportGuiConfirm.open(player, target, reportReason, note);
		else
			ReportGui.open(player, target, note);
	}

	@Cmd(args = { "INTEGER", "REPORTSTATUS", "Observation ou motif de changement" }, min = 2, permissionName = "REPORT_CHANGE_COMMAND", syntax = "<idReport> <status> [note]")
	public void change(CommandContext cmd) {
		long idP = 0;
		if (player != null)
			idP = getOlympaPlayer().getId();
		int id = cmd.getArgument(0);
		OlympaReport report;
		try {
			report = ReportMySQL.getReport(id);
			if (report == null) {
				sender.sendMessage(Prefix.DEFAULT_BAD.formatMessage("Aucun report trouvé avec l'id &4%s&c.", id));
				return;
			}
			ReportStatus status = cmd.getArgument(1);
			ReportStatus oldStatus = report.getStatus();
			if (status == oldStatus) {
				sender.sendMessage(Prefix.DEFAULT_BAD.formatMessage("Le report &4n°%s&c a déjà le status %s&c.", report.getId(), oldStatus.getNameColored()));
				return;
			}
			String note = null;
			if (cmd.getArgumentsLength() > 2)
				note = cmd.getFrom(2);
			report.addStatusInfo(new ReportStatusInfo(note, idP, status));
			ReportMySQL.updateReport(report);
			String targetName = AccountProvider.getter().get(report.getTargetId()).getName();
			sender.sendMessage(Prefix.DEFAULT_GOOD.formatMessage("Le report &2n°%s&a envers &2%s&a est passé de %s&a à %s&a.", report.getId(), targetName, oldStatus.getNameColored(), report.getStatus().getNameColored()));
		} catch (SQLException e) {
			e.printStackTrace();
			sendError("Une erreur est survenu avec la base de données.");
		}
	}

	@Cmd(args = { "UUID|INTEGER|OLYMPA_PLAYERS_INFO", "INTEGER" }, aliases = { "seeauthor" }, registerAliasesInTab = true, permissionName = "REPORT_SEE_COMMAND", syntax = "<joueur | uuid | idReport>", min = 1)
	public void see(CommandContext cmd) {
		//		List<OlympaReport> reports = new ArrayList<>();
		OlympaPlayerInformations opi = null;
		long targetId = 0;
		int page = cmd.getArgumentsLength() > 1 ? cmd.<Integer>getArgument(1) : 1;
		//		try {
		if (cmd.getArgument(0) instanceof Integer) {
			targetId = cmd.<Integer>getArgument(0);
			opi = AccountProvider.getter().getPlayerInformations(targetId);
		} else if (cmd.getArgument(0) instanceof UUID) {
			opi = AccountProvider.getter().getPlayerInformations(cmd.<UUID>getArgument(0));
			if (opi != null)
				targetId = opi.getId();
		} else if (cmd.getArgument(0) instanceof OlympaPlayerInformations) {
			targetId = cmd.<OlympaPlayerInformations>getArgument(0).getId();
			opi = cmd.<OlympaPlayerInformations>getArgument(0);
		} else {
			sendUsage(cmd.label);
			return;
		}
		if (opi == null) {
			player.sendMessage(Prefix.DEFAULT_BAD.formatMessage("Le joueur est introuvable."));
			return;
		}
		//			if (reports.isEmpty())
		//				if (isSeeAuthor)
		//					reports.addAll(ReportMySQL.getReportsByAuthor(targetId, 0));
		//				else
		//					reports.addAll(ReportMySQL.getReportByTarget(targetId));
		//		} catch (SQLException e) {
		//			e.printStackTrace();
		//			sendError("Une erreur est survenu avec la base de données.");
		//			return;
		//		}
		//		String target = opi.getName();
		//		if (reports.isEmpty()) {
		//			player.sendMessage(Prefix.DEFAULT_BAD.formatMessage("Aucun report trouvé avec &4%s&c.", target));
		//			return;
		//		}
		if (cmd.isAlias("seeauthor"))
			ReportMsg.sendPanelAuthor(sender, opi, page);
		else
			ReportMsg.sendPanelTarget(sender, opi, page);
	}

	@Cmd(args = "INTEGER", permissionName = "REPORT_SEE_COMMAND", syntax = "<idReport>", min = 1)
	public void seeId(CommandContext cmd) {
		OlympaReport report = null;
		try {
			if (cmd.getArgument(0) instanceof Integer)
				report = ReportMySQL.getReport(cmd.<Integer>getArgument(0));
			else {
				sendUsage(cmd.label);
				return;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			sendError("Une erreur est survenu avec la base de données.");
			return;
		}
		if (report == null) {
			player.sendMessage(Prefix.DEFAULT_BAD.formatMessage("Aucun report trouvé avec l'id &4%s&c.", cmd.getArgument(0)));
			return;
		}
		ReportMsg.sendPanelId(sender, report);
	}

	@Cmd(args = "INTEGER", permissionName = "REPORT_SEE_COMMAND", syntax = "[page]", min = 0)
	public void seeLast(CommandContext cmd) {
		//		List<OlympaReport> reports = null;
		int page = 1;
		//		try {
		if (cmd.getArgumentsLength() > 0)
			if (cmd.getArgument(0) instanceof Integer)
				page = cmd.<Integer>getArgument(0);
			else {
				sendIncorrectSyntax(getCommand(getCommand()));
				return;
			}
		//			reports = ReportMySQL.getLastReports(100);
		//		} catch (SQLException e) {
		//			e.printStackTrace();
		//			sendError("Une erreur est survenu avec la base de données.");
		//			return;
		//		}
		//		if (reports == null) {
		//			player.sendMessage(Prefix.DEFAULT_BAD.formatMessage("Aucun report trouvé."));
		//			return;
		//		}
		ReportMsg.sendPanelLast(sender, page);
	}

	@Cmd(permissionName = "REPORT_SEE_COMMAND", min = 0)
	public void seeConnected(CommandContext cmd) {
		Stream<Entry<OlympaPlayerInformations, List<OlympaReport>>> reports = null;
		try {
			reports = ReportMySQL.getConnectedReports();
		} catch (SQLException e) {
			e.printStackTrace();
			sendError("Une erreur est survenu avec la base de données.");
			return;
		}
		if (reports == null) {
			player.sendMessage(Prefix.DEFAULT_BAD.formatMessage("Aucun report trouvé avec l'id &4%s&c.", cmd.getArgument(0)));
			return;
		}
		ReportMsg.sendPanelMax(sender, reports);
	}
}
