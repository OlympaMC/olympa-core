package fr.olympa.core.bungee.redis.receiver;

import com.google.gson.Gson;

import fr.olympa.api.report.OlympaReport;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.spigot.report.ReportAlerts;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import redis.clients.jedis.JedisPubSub;

public class SpigotReportReceiver extends JedisPubSub {

	@Override
	public void onMessage(String channel, String message) {
		OlympaReport report = new Gson().fromJson(message, OlympaReport.class);
		ProxiedPlayer targetPlayer = ProxyServer.getInstance().getPlayer(report.targetName);
		String targetServer = null;
		if (targetPlayer != null)
			targetServer = targetPlayer.getServer().getInfo().getName();
		ReportAlerts.sendAlert(report, report.authorName, report.targetName, targetServer);
		OlympaBungee.getInstance().sendMessage("&a[Redis] Report receive " + message);
	}
}
