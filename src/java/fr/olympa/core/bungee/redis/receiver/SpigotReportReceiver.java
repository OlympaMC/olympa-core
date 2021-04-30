package fr.olympa.core.bungee.redis.receiver;

import com.google.gson.Gson;

import fr.olympa.api.report.OlympaReport;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.spigot.report.ReportMsg;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import redis.clients.jedis.JedisPubSub;

public class SpigotReportReceiver extends JedisPubSub {

	@Override
	public void onMessage(String channel, String message) {
		OlympaBungee.getInstance().sendMessage("&a[Redis] Report receive " + message);
		OlympaReport report = new Gson().fromJson(message, OlympaReport.class);
		ProxiedPlayer targetPlayer = ProxyServer.getInstance().getPlayer(report.getTargetName());
		String targetServer = null;
		if (targetPlayer != null)
			targetServer = targetPlayer.getServer().getInfo().getName();
		ReportMsg.sendAlert(report, report.getAuthorName(), report.getTargetName(), targetServer);
	}
}
