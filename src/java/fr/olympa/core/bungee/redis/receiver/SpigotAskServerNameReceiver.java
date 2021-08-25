package fr.olympa.core.bungee.redis.receiver;

import fr.olympa.api.common.chat.ColorUtils;
import fr.olympa.api.common.server.ServerInfoAdvancedBungee;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.redis.RedisBungeeSend;
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
			OlympaBungee.getInstance().getMonitoring().updateServer(serverInfo, true, t -> {
				ServerInfoAdvancedBungee info = OlympaBungee.getInstance().getMonitoring().getMonitor(serverInfo);
				ServersConnection.getConnections(info.getOlympaServer()).forEach(wc -> {
					ProxiedPlayer proxiedPlayer = ProxyServer.getInstance().getPlayer(wc.uuid);
					proxiedPlayer.sendMessage(Prefix.DEFAULT_GOOD + ColorUtils.color("Tentative de connexion au serveur &2" + serverInfo.getName() + "&a..."));
					proxiedPlayer.connect(serverInfo);
				});

			});
		}
	}
}
