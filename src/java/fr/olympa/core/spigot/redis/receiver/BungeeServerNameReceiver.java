package fr.olympa.core.spigot.redis.receiver;

import fr.olympa.api.provider.RedisAccess;
import fr.olympa.core.spigot.OlympaCore;
import redis.clients.jedis.JedisPubSub;

public class BungeeServerNameReceiver extends JedisPubSub {

	@Override
	public void onMessage(String channel, String message) {
		super.onMessage(channel, message);
		String[] info = message.split(";");
		String ip = info[0];
		int port = Integer.valueOf(info[1]);
		String serverName = info[2];
		OlympaCore core = OlympaCore.getInstance();
		String serverIp = core.getServer().getIp();
		int serverPort = core.getServer().getPort();
		if (ip.equals(serverIp) && port == serverPort) {
			core.setServerName(serverName);
			this.unsubscribe();
			RedisAccess.INSTANCE.getConnection().clientSetname("Olympa_" + serverName);
			//			RedisAccess.INSTANCE.disconnect();
		}
	}
}
