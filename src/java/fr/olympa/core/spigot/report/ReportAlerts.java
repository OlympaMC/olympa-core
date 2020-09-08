package fr.olympa.core.spigot.report;

import java.sql.SQLException;

import org.bukkit.Bukkit;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.report.OlympaReport;
import fr.olympa.api.report.ReportUtils;
import fr.olympa.api.sql.MySQL;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.core.spigot.redis.RedisSpigotSend;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;

public class ReportAlerts {

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
				players.forEach(p -> p.sendMessage(ReportUtils.getAlert(report, authorName, targetName, targetServer, p.getServer().getInfo().getName())));
			});
		}
	}

	public static void sendAlert(OlympaReport report) {
		OlympaPlayer targetOlympaPlayer;
		OlympaPlayer authorOlympaPlayer;
		try {
			targetOlympaPlayer = MySQL.getPlayer(report.getTargetId());
			authorOlympaPlayer = MySQL.getPlayer(report.getAuthorId());
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
		RedisSpigotSend.askPlayerServer(targetOlympaPlayer.getUniqueId(), t -> sendAlert(report, authorOlympaPlayer.getName(), targetOlympaPlayer.getName(), t));
	}

}
