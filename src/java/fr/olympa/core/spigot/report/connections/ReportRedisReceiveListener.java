package fr.olympa.core.spigot.report.connections;

import fr.olympa.api.report.OlympaReport;
import fr.olympa.api.utils.GsonCustomizedObjectTypeAdapter;
import fr.olympa.core.spigot.report.ReportAlerts;
import redis.clients.jedis.JedisPubSub;

public class ReportRedisReceiveListener extends JedisPubSub {
	@Override
	public void onMessage(String channel, String message) {
		OlympaReport report = GsonCustomizedObjectTypeAdapter.GSON.fromJson(message, OlympaReport.class);
		ReportAlerts.sendAlert(report);
	}
}
