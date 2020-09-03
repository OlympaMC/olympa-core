package fr.olympa.core.bungee.redis.receiver;

import fr.olympa.api.utils.Prefix;
import fr.olympa.core.bungee.redis.RedisBungeeSend;
import fr.olympa.core.bungee.servers.MonitorServers;
import fr.olympa.core.bungee.servers.QueueSpigotTask;
import fr.olympa.core.bungee.servers.ServersConnection;
import fr.olympa.core.bungee.utils.BungeeUtils;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import redis.clients.jedis.JedisPubSub;

public class SpigotAskServerNameReceiver extends JedisPubSub {

	@SuppressWarnings("deprecation")
	@Override
	public void onMessage(String channel, String message) {
		String[] args = message.split(";");
		ServerInfo serverInfo = ServersConnection.getServerByNameOrIpPort(args[0]);
		if (serverInfo != null) {
			RedisBungeeSend.sendServerName(serverInfo);
			// J'ai essayé de connecter directement les joueurs qui sont en file d'attente
			MonitorServers.updateServer(serverInfo, true, t -> {
				ServersConnection.connect.entrySet().stream().filter(r -> MonitorServers.getServers(((QueueSpigotTask) r.getValue()).getOlympaServer())
						.values().stream().anyMatch(o -> o.getServerInfo().getName().equals(serverInfo.getName()))).forEach(entry -> {
							ProxiedPlayer proxiedPlayer = ProxyServer.getInstance().getPlayer(entry.getKey());
							ServersConnection.removeTryToConnect(proxiedPlayer);
							proxiedPlayer.sendMessage(Prefix.DEFAULT_GOOD + BungeeUtils.color("Tentative de connexion au serveur &2" + serverInfo.getName() + "&a..."));
							proxiedPlayer.connect(serverInfo);
						});

			});
		}
	}
}
