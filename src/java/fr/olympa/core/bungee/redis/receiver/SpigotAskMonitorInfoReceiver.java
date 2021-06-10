package fr.olympa.core.bungee.redis.receiver;

import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.redis.RedisBungeeSend;
import fr.olympa.core.bungee.servers.ServersConnection;
import net.md_5.bungee.api.config.ServerInfo;
import redis.clients.jedis.JedisPubSub;

public class SpigotAskMonitorInfoReceiver extends JedisPubSub {

	public static long lastTimeAsk;

	@Override
	public void onMessage(String channel, String message) {
		lastTimeAsk = Utils.getCurrentTimeInSeconds();
		ServerInfo serverInfo = ServersConnection.getServerByNameOrIpPort(message);
		OlympaBungee.getInstance().sendMessage("&2%s&a demande les informations des autres serveurs.", serverInfo.getName());
		RedisBungeeSend.sendServerInfos();
	}
}
