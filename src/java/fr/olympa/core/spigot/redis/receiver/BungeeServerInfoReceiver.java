package fr.olympa.core.spigot.redis.receiver;

import java.util.ArrayList;
import java.util.List;

import fr.olympa.api.customevents.MonitorServerInfoReceiveEvent;
import fr.olympa.api.server.MonitorInfo;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.core.spigot.redis.RedisSpigotSend;
import protocolsupport.libs.com.google.gson.Gson;
import redis.clients.jedis.JedisPubSub;

public class BungeeServerInfoReceiver extends JedisPubSub {

	private static long LAST_EVENT_TIME;

	@Override
	public void onMessage(String channel, String message) {
		super.onMessage(channel, message);
		List<MonitorInfo> monitorInfos = OlympaCore.getInstance().getMonitorInfos();
		String[] serversInfoJson = message.split("\\n");
		List<MonitorInfo> newMonitorInfos = new ArrayList<>();
		for (String s : serversInfoJson) {
			MonitorInfo monitorInfo = new Gson().fromJson(s, MonitorInfo.class);
			newMonitorInfos.add(monitorInfo);
		}
		monitorInfos.clear();
		monitorInfos.addAll(newMonitorInfos);
		RedisSpigotSend.askServerInfo.forEach(c -> c.accept(monitorInfos));
		RedisSpigotSend.askServerInfo.clear();
		long time = Utils.getCurrentTimeInSeconds();
		if (time - LAST_EVENT_TIME > 1) {
			OlympaCore.getInstance().getServer().getPluginManager().callEvent(new MonitorServerInfoReceiveEvent(newMonitorInfos));
			LAST_EVENT_TIME = time;
		}
	}
}
