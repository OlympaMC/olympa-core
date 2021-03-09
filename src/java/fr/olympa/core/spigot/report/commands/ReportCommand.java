package fr.olympa.core.spigot.report.commands;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.api.command.complex.ComplexCommand;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.player.OlympaPlayerInformations;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.report.OlympaReport;
import fr.olympa.api.report.ReportReason;
import fr.olympa.api.report.ReportStatus;
import fr.olympa.api.report.ReportStatusInfo;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.spigot.report.ReportHandler;
import fr.olympa.core.spigot.report.ReportMsg;
import fr.olympa.core.spigot.report.connections.ReportMySQL;
import fr.olympa.core.spigot.report.gui.ReportGui;
import fr.olympa.core.spigot.report.gui.ReportGuiConfirm;

public class ReportCommand extends ComplexCommand {

	public ReportCommand(Plugin plugin) {
		super(plugin, "report", "Signale un joueur", OlympaCorePermissions.REPORT_COMMAND, "signale");
		addArgumentParser("REPORTREASON", sender -> ReportReason.values().stream().map(ReportReason::getReasonClear).collect(Collectors.toList()), x -> {
			return ReportReason.getByReason(x.replace("_", " "));
		}, x -> String.format("&4%s&c doit être une raison tel que &4%s&c", x, ReportReason.values().stream().map(ReportReason::getReasonClear).collect(Collectors.joining(", "))));

		addArgumentParser("REPORTSTATUS", sender -> Arrays.asList(ReportStatus.values()).stream().map(ReportStatus::getName).collect(Collectors.toList()), x -> {
			return ReportStatus.get(x);
		}, x -> String.format("&4%s&c doit être un status tel que &4%s&c", x, Arrays.asList(ReportStatus.values()).stream().map(ReportStatus::getName).collect(Collectors.joining(", "))));
	}

	//	@Override
	//	public boolean noArguments(CommandSender sender) {
	//		if (sender instanceof Player) {
	//			ReportGuiChoose.open(player);
	//			return true;
	//		}
	//		return false;
	//	}

	@Cmd(args = { "PLAYERS", "REPORTREASON", "Informations complémentaire du report" }, min = 1, syntax = "<joueur> [status] [note]", otherArg = true)
	public void wrongArg(CommandContext cmd) {
		Player player = this.player;
		OfflinePlayer target = cmd.getArgument(0);
		if (player != null && target.getUniqueId().equals(player.getUniqueId()))
			sendError("Tu ne peux pas te report toi même. (enfin si mais que pour tester)");
		//			return; TODO REMOVE comment
		ReportReason reportReason = null;
		String note = null;
		if (cmd.getArgumentsLength() > 1)
			reportReason = cmd.getArgument(1);
		if (cmd.getArgumentsLength() > 2)
			note = cmd.getFrom(2);
		if (player == null) {
			try {
				ReportHandler.report(player, target, reportReason, note);
			} catch (SQLException e) {
				sendError(e);
				e.printStackTrace();
			}
			return;
		}
		if (reportReason != null)
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
			String targetName = AccountProvider.get(report.getTargetId()).getName();
			sender.sendMessage(Prefix.DEFAULT_GOOD.formatMessage("Le report &2n°%s&a envers &2%s&a est passé de %s&a à %s&a.", report.getId(), targetName, oldStatus.getNameColored(), report.getStatus().getNameColored()));
		} catch (SQLException e) {
			e.printStackTrace();
			sendError("Une erreur est survenu avec la base de donnés.");
		}
	}

	@Cmd(args = "INTEGER|UUID|OFFLINE_PLAYERS", aliases = { "seeauthor" }, registerAliasesInTab = true, permissionName = "REPORT_SEE_COMMAND", syntax = "<joueur | uuid | idReport>", min = 1)
	public void see(CommandContext cmd) {
		List<OlympaReport> reports = new ArrayList<>();
		OlympaPlayer op = null;
		long targetId = 0;
		try {
			if (cmd.getArgument(0) instanceof OfflinePlayer)
				targetId = (op = new AccountProvider(cmd.<OfflinePlayer>getArgument(0).getUniqueId()).get()).getId();
			else if (cmd.getArgument(0) instanceof UUID)
				targetId = (op = new AccountProvider(cmd.<UUID>getArgument(0)).get()).getId();
			else if (cmd.getArgument(0) instanceof Integer)
				targetId = cmd.<Integer>getArgument(0);
			else {
				sendUsage(cmd.label);
				return;
			}
			if (reports.isEmpty())
				if (cmd.label.equals("seeauthor"))
					reports.addAll(ReportMySQL.getReportsByAuthor(targetId));
				else
					reports.addAll(ReportMySQL.getReportByTarget(targetId));
		} catch (SQLException e) {
			e.printStackTrace();
			sendError("Une erreur est survenu avec la base de donnés.");
			return;
		}
		String target = op != null ? op.getName() : cmd.getArgument(0) instanceof Integer ? String.valueOf(cmd.<Integer>getArgument(0)) : cmd.<String>getArgument(0);
		if (reports.isEmpty()) {
			player.sendMessage(Prefix.DEFAULT_BAD.formatMessage("Aucun report trouvé avec &4%s&c.", target));
			return;
		}
		if (cmd.getArgument(0) instanceof Integer)
			ReportMsg.sendPanelId(sender, reports.get(0));
		else if (cmd.label.equals("seeauthor"))
			ReportMsg.sendPanelAuthor(sender, target, reports);
		else
			ReportMsg.sendPanelTarget(sender, target, reports);
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
			sendError("Une erreur est survenu avec la base de donnés.");
			return;
		}
		if (report == null) {
			player.sendMessage(Prefix.DEFAULT_BAD.formatMessage("Aucun report trouvé avec l'id &4%s&c.", cmd.getArgument(0)));
			return;
		}
		ReportMsg.sendPanelId(sender, report);
	}

	@Cmd(args = "INTEGER", permissionName = "REPORT_SEE_COMMAND", syntax = "[startNumber] [limite]", min = 0)
	public void seeLast(CommandContext cmd) {
		List<OlympaReport> reports = null;
		int limit = -1;
		int startNumber = -1;
		try {
			if (cmd.getArgumentsLength() > 0) {
				if (cmd.getArgument(0) instanceof Integer)
					startNumber = cmd.<Integer>getArgument(0);
				if (cmd.getArgumentsLength() > 1 && cmd.getArgument(1) instanceof Integer)
					limit = cmd.<Integer>getArgument(1);
				if (limit == -1 || startNumber == -1) {
					sendIncorrectSyntax(getCommand(getCommand()));
					return;
				}
			}
			if (limit == -1)
				limit = 10;
			if (startNumber == -1)
				startNumber = 0;
			reports = ReportMySQL.getLastReports(startNumber, limit);
		} catch (SQLException e) {
			e.printStackTrace();
			sendError("Une erreur est survenu avec la base de donnés.");
			return;
		}
		if (reports == null) {
			player.sendMessage(Prefix.DEFAULT_BAD.formatMessage("Aucun report trouvé avec offset = &4%s&c & limit = &4%s&c.", startNumber, limit));
			return;
		}
		ReportMsg.sendPanelLast(sender, reports);
	}

	@Cmd(permissionName = "REPORT_SEE_COMMAND", min = 0)
	public void seeConnected(CommandContext cmd) {
		Stream<Entry<OlympaPlayerInformations, List<OlympaReport>>> reports = null;
		try {
			reports = ReportMySQL.getConnectedReports();
		} catch (SQLException e) {
			e.printStackTrace();
			sendError("Une erreur est survenu avec la base de donnés.");
			return;
		}
		if (reports == null) {
			player.sendMessage(Prefix.DEFAULT_BAD.formatMessage("Aucun report trouvé avec l'id &4%s&c.", cmd.getArgument(0)));
			return;
		}
		ReportMsg.sendPanelMax(sender, reports);
	}
}
