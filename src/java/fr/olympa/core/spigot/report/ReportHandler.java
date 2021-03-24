package fr.olympa.core.spigot.report;

import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import fr.olympa.api.chat.ColorUtils;
import fr.olympa.api.player.OlympaConsole;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.player.OlympaPlayerInformations;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.report.OlympaReport;
import fr.olympa.api.report.OlympaReportAddEvent;
import fr.olympa.api.report.ReportReason;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.core.spigot.redis.RedisSpigotSend;
import fr.olympa.core.spigot.report.connections.ReportMySQL;

public class ReportHandler {

	public static void report(Player author, OfflinePlayer target, ReportReason reason, String note) throws SQLException {
		OlympaPlayerInformations authorOlympaPlayer;
		ConsoleCommandSender consoleCommand = null;
		if (author != null)
			authorOlympaPlayer = AccountProvider.getPlayerInformations(author.getUniqueId());
		else {
			authorOlympaPlayer = AccountProvider.getPlayerInformations(OlympaConsole.getId());
			consoleCommand = Bukkit.getConsoleSender();
		}
		OlympaPlayer targetOlympaPlayer;
		String serverName = OlympaCore.getInstance().getServerName();
		String targetServer;
		if (target.isOnline())
			targetServer = serverName;
		else
			targetServer = "";
		try {
			targetOlympaPlayer = new AccountProvider(target.getUniqueId()).get();
		} catch (SQLException e) {
			throw new SQLException("&4REPORT &cImpossible de récupérer l'id olympaPlayer de " + target.getName());
		}
		OlympaReport report = new OlympaReport(targetOlympaPlayer.getId(), authorOlympaPlayer.getId(), reason, OlympaCore.getInstance().getServerName(), note);
		report.setAuthorName(authorOlympaPlayer.getName());
		report.setTargetName(target.getName());
		try {
			ReportMySQL.createReport(report);
			String msg = ColorUtils.color(Prefix.DEFAULT_GOOD + "Tu as signaler &2" + target.getName() + "&a pour &2" + reason.getReason() + "&a.");
			if (author != null)
				author.sendMessage(msg);
			else
				consoleCommand.sendMessage(msg);

		} catch (SQLException e) {
			e.printStackTrace();
			String msg = ColorUtils.color(Prefix.DEFAULT_BAD + "Une erreur est survenu, ton report n'a pas été sauvegardé mais le staff connecté est au courant.");
			if (author != null) {

				author.closeInventory();
				author.sendMessage(msg);
			} else
				consoleCommand.sendMessage(msg);
		}
		Bukkit.getPluginManager().callEvent(new OlympaReportAddEvent(author, target, report));
		//		if (!RedisSpigotSend.sendReport(report))
		RedisSpigotSend.sendReport(report);
		ReportMsg.sendAlert(report, authorOlympaPlayer.getName(), targetOlympaPlayer.getName(), targetServer);
	}
}
