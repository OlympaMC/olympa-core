package fr.olympa.core.spigot.redis.receiver;

import fr.olympa.api.redis.RedisAccess;
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
			if (core.getServerName().equals(serverName)) {
				OlympaCore.getInstance().sendMessage("&4Nom du serveur déjà reçu : &c" + serverName + "&4.");
				return;
			}
			core.setServerName(serverName);
			RedisAccess.INSTANCE.updateClientName(serverName);
			OlympaCore.getInstance().sendMessage("&2Nom du serveur reçu : &a" + serverName + "&2.");
			this.unsubscribe();
		} else
			OlympaCore.getInstance().sendMessage("&4Mauvais nom du serveur reçu : &c" + serverName + "&4.");
	}
}
