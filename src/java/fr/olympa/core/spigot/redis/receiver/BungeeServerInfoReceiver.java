package fr.olympa.core.spigot.redis.receiver;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.google.gson.Gson;

import fr.olympa.api.common.server.ServerInfoBasic;
import fr.olympa.core.spigot.redis.RedisSpigotSend;
import redis.clients.jedis.JedisPubSub;

public class BungeeServerInfoReceiver extends JedisPubSub {

	private static List<Consumer<List<ServerInfoBasic>>> callbacksRegister = new ArrayList<>();

	public static List<Consumer<List<ServerInfoBasic>>> getCallbacksRegister() {
		return callbacksRegister;
	}

	public static boolean registerCallback(Consumer<List<ServerInfoBasic>> callback) {
		return callbacksRegister.add(callback);
	}

	@Override
	public void onMessage(String channel, String message) {
		super.onMessage(channel, message);
		String[] serversInfoJson = message.split("\\n");
		List<ServerInfoBasic> newMonitorInfos = new ArrayList<>();
		for (String s : serversInfoJson) {
			ServerInfoBasic monitorInfo = new Gson().fromJson(s, ServerInfoBasic.class);
			newMonitorInfos.add(monitorInfo);
		}
		callbacksRegister.forEach(c -> c.accept(newMonitorInfos));
		RedisSpigotSend.askServerInfo.forEach(c -> c.accept(newMonitorInfos, false));
		RedisSpigotSend.askServerInfo.clear();
	}
}
