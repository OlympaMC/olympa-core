package fr.olympa.core.bungee.redis.receiver;

import fr.olympa.core.bungee.redis.RedisBungeeSend;
import fr.olympa.core.bungee.servers.MonitorServers;
import fr.olympa.core.bungee.servers.ServersConnection;
import net.md_5.bungee.api.config.ServerInfo;
import redis.clients.jedis.JedisPubSub;

public class SpigotServerNameReceiver extends JedisPubSub {

	@Override
	public void onMessage(String channel, String message) {
		super.onMessage(channel, message);
		ServerInfo serverInfo = ServersConnection.getServerByNameOrIpPort(message);
		if (serverInfo != null) {
			RedisBungeeSend.sendServerName(serverInfo);
			MonitorServers.updateServer(serverInfo, true/*, t -> {
														
														J'ai essayé de connecter directement les joueurs qui sont en file d'attente
														ServersConnection.connect.entrySet().stream().filter(r -> MonitorServers.getServers(((QueueSpigotTask) r.getValue()).getOlympaServer())
														.values().stream().anyMatch(o -> o.getServerInfo().getName().equals(serverInfo.getName()))).forEach(entry -> {
														ProxiedPlayer player = ProxyServer.getInstance().getPlayer(entry.getKey());
														player.connect(serverInfo);
														ServersConnection.removeTryToConnect(player);
														});
														
														}*/);
		}
	}
}
