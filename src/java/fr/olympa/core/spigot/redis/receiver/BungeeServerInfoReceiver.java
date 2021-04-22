package fr.olympa.core.spigot.redis.receiver;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import fr.olympa.api.server.MonitorInfo;
import fr.olympa.core.spigot.redis.RedisSpigotSend;
import protocolsupport.libs.com.google.gson.Gson;
import redis.clients.jedis.JedisPubSub;

public class BungeeServerInfoReceiver extends JedisPubSub {

	private static List<Consumer<List<MonitorInfo>>> callbacksRegister = new ArrayList<>();

	public static List<Consumer<List<MonitorInfo>>> getCallbacksRegister() {
		return callbacksRegister;
	}

	public static boolean registerCallback(Consumer<List<MonitorInfo>> callback) {
		return callbacksRegister.add(callback);
	}

	@Override
	public void onMessage(String channel, String message) {
		super.onMessage(channel, message);
		String[] serversInfoJson = message.split("\\n");
		List<MonitorInfo> newMonitorInfos = new ArrayList<>();
		for (String s : serversInfoJson) {
			MonitorInfo monitorInfo = new Gson().fromJson(s, MonitorInfo.class);
			newMonitorInfos.add(monitorInfo);
		}
		callbacksRegister.forEach(c -> c.accept(newMonitorInfos));
		RedisSpigotSend.askServerInfo.forEach(c -> c.accept(newMonitorInfos));
		RedisSpigotSend.askServerInfo.clear();
	}
}
