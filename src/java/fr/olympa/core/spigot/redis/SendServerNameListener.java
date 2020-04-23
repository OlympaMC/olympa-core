package fr.olympa.core.spigot.redis;

import fr.olympa.core.spigot.OlympaCore;
import redis.clients.jedis.JedisPubSub;

public class SendServerNameListener extends JedisPubSub {

	@Override
	public void onMessage(String channel, String message) {
		String[] info = message.split(":");
		String ip = info[0];
		int port = Integer.valueOf(info[1]);
		String serverName = info[2];
		OlympaCore core = OlympaCore.getInstance();
		String serverIp = core.getServer().getIp();
		int serverPort = core.getServer().getPort();
		if (ip.equals(serverIp) && port == serverPort) {
			core.setServerName(serverName);
			this.unsubscribe();
		}
	}
}
