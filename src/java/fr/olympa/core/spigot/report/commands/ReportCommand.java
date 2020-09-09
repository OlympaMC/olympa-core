package fr.olympa.core.spigot.report.commands;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.api.command.complex.ComplexCommand;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.report.OlympaReport;
import fr.olympa.api.report.ReportReason;
import fr.olympa.api.report.ReportStatus;
import fr.olympa.api.report.ReportStatusInfo;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.spigot.report.ReportMsg;
import fr.olympa.core.spigot.report.connections.ReportMySQL;
import fr.olympa.core.spigot.report.gui.ReportGui;
import fr.olympa.core.spigot.report.gui.ReportGuiChoose;
import fr.olympa.core.spigot.report.gui.ReportGuiConfirm;
import us.myles.viaversion.libs.gson.Gson;

public class ReportCommand extends ComplexCommand {

	public ReportCommand(Plugin plugin) {
		super(plugin, "report", "Signale un joueur", OlympaCorePermissions.REPORT_SEE_COMMAND, "reports", "signale", "signaler");
		addArgumentParser("REPORTREASON", sender -> Arrays.asList(ReportReason.values()).stream().map(ReportReason::getReasonClear).collect(Collectors.toList()), x -> {
			ReportReason reason = ReportReason.getByReason(x.replace("_", " "));
			if (reason != null)
				return reason;
			sendError(x + " doit être une raison tel que &4%s&c.", Arrays.asList(ReportReason.values()).stream().map(ReportReason::getReasonClear).collect(Collectors.joining(", ")));
			return null;
		});
		addArgumentParser("REPORTSTATUS", sender -> Arrays.asList(ReportStatus.values()).stream().map(ReportStatus::getName).collect(Collectors.toList()), x -> {
			ReportStatus status = ReportStatus.get(x);
			if (status != null)
				return status;
			sendError(x + " doit être un status tel que &4%s&c.", Arrays.asList(ReportStatus.values()).stream().map(ReportStatus::getName).collect(Collectors.joining(", ")));
			return null;
		});
	}

	@Override
	public boolean noArguments(CommandSender sender) {
		if (sender instanceof Player) {
			ReportGuiChoose.open(player);
			return true;
		}
		return false;
	}

	@Cmd(args = { "PLAYERS", "REPORTREASON", "note" }, min = 1, syntax = "<joueur> [status] [note]")
	public void player(CommandContext cmd) {
		Player player = this.player;

		OfflinePlayer target = cmd.getArgument(0);
		if (target.getUniqueId().equals(player.getUniqueId()))
			sendError("Tu ne peux pas te report toi même.");
		//			return;
		ReportReason reportReason = null;
		String note = null;
		if (cmd.getArgumentsLength() > 1)
			reportReason = cmd.getArgument(1);
		if (cmd.getArgumentsLength() > 2)
			note = cmd.getFrom(2);

		if (reportReason != null)
			ReportGuiConfirm.open(player, target, reportReason, note);
		else
			ReportGui.open(player, target, note);
	}

	@Cmd(args = { "INTEGER", "REPORTSTATUS", "note" }, min = 2, permissionName = "REPORT_CHANGE_COMMAND", syntax = "<idReport> <status> [note]")
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
			sender.sendMessage(Prefix.DEFAULT_GOOD.formatMessage("Le report &2n°%s&a envers &2%s&a est passé de %s&a à %s&a.", report.getId(), targetName, oldStatus.getNameColored(), status.getNameColored()));
		} catch (SQLException e) {
			e.printStackTrace();
			sendError("Une erreur est survenu avec la base de donnés.");
			return;
		}
	}

	@Cmd(args = "PLAYERS|UUID|INTEGER", aliases = { "seeauthor" }, permissionName = "REPORT_SEE_COMMAND", syntax = "<joueur|uuid|idReport>")
	public void see(CommandContext cmd) {
		List<OlympaReport> reports = new ArrayList<>();
		OlympaPlayer op = null;
		try {
			if (cmd.getArgument(0) instanceof Player)
				op = AccountProvider.get(cmd.<Player>getArgument(0).getUniqueId());
			else if (cmd.getArgument(0) instanceof UUID)
				op = new AccountProvider(cmd.<UUID>getArgument(0)).get();
			else if (cmd.getArgument(0) instanceof Integer)
				reports.add(ReportMySQL.getReport(cmd.<Integer>getArgument(0)));
			else {
				sendUsage(cmd.label);
				return;
			}

			if (reports.isEmpty())
				if (cmd.label.equals("seeauthor"))
					reports.addAll(ReportMySQL.getReportsByAuthor(op.getId()));
				else
					reports.addAll(ReportMySQL.getReportByTarget(op.getId()));

		} catch (SQLException e) {
			e.printStackTrace();
			sendError("Une erreur est survenu avec la base de donnés.");
			return;
		}
		String target = op != null ? op.getName() : cmd.getArgument(0) instanceof Integer ? String.valueOf(cmd.<Integer>getArgument(0)) : cmd.<String>getArgument(0);
		System.out.println("target " + target + " reports " + new Gson().toJson(reports));
		if (reports.isEmpty()) {
			player.sendMessage(Prefix.DEFAULT_BAD.formatMessage("Aucun report trouvé avec &4%s&c.", target));
			return;
		}
		ReportMsg.sendPanel(sender, target, reports);
	}
}
