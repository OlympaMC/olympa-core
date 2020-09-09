package fr.olympa.core.spigot.report;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.player.OlympaPlayerInformations;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.report.OlympaReport;
import fr.olympa.api.report.ReportUtils;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.core.spigot.redis.RedisSpigotSend;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;

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

	public static void sendPanel(CommandSender sender, String target, List<OlympaReport> reports) {
		sender.sendMessage(Prefix.DEFAULT_GOOD.formatMessage("Report %s :\n%s", target, reports.stream().map(r -> {
			OlympaPlayerInformations opTarget = AccountProvider.getPlayerInformations(r.getTargetId());
			OlympaPlayerInformations opAuthor = AccountProvider.getPlayerInformations(r.getAuthorId());
			return r.getStatus().getNameColored() + " &e" + Utils.timestampToDuration(r.getLastUpdate(), 1) + " " + opTarget.getName() + " &epar " + opAuthor.getName() + " pour " + r.getReason().getReason();
		}).collect(Collectors.joining("\n"))));
	}

}
