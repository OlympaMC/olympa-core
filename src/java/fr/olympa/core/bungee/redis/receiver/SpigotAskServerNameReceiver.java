package fr.olympa.core.bungee.redis.receiver;

import fr.olympa.api.common.chat.ColorUtils;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.bungee.redis.RedisBungeeSend;
import fr.olympa.core.bungee.servers.MonitorInfoBungee;
import fr.olympa.core.bungee.servers.MonitorServers;
import fr.olympa.core.bungee.servers.ServersConnection;
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
			MonitorServers.updateServer(serverInfo, true, t -> {
				MonitorInfoBungee info = MonitorServers.getMonitor(serverInfo);
				ServersConnection.getConnections(info.getOlympaServer()).forEach(wc -> {
					ProxiedPlayer proxiedPlayer = ProxyServer.getInstance().getPlayer(wc.uuid);
					proxiedPlayer.sendMessage(Prefix.DEFAULT_GOOD + ColorUtils.color("Tentative de connexion au serveur &2" + serverInfo.getName() + "&a..."));
					proxiedPlayer.connect(serverInfo);
				});

			});
		}
	}
}
