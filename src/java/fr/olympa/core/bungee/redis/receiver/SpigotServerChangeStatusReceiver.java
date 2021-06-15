package fr.olympa.core.bungee.redis.receiver;

import java.util.Map;

import fr.olympa.api.common.server.ServerStatus;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.servers.MonitorInfoBungee;
import fr.olympa.core.bungee.servers.MonitorServers;
import fr.olympa.core.bungee.servers.ServersConnection;
import net.md_5.bungee.api.config.ServerInfo;
import redis.clients.jedis.JedisPubSub;

public class SpigotServerChangeStatusReceiver extends JedisPubSub {

	@Override
	public void onMessage(String channel, String message) {
		String[] args = message.split(";");
		ServerInfo serverInfo = ServersConnection.getServerByNameOrIpPort(args[0]);
		if (serverInfo != null) {
			ServerStatus status = ServerStatus.get(Integer.parseInt(args[1]));
			ServerStatus previous = ServerStatus.UNKNOWN;
			MonitorInfoBungee info = MonitorServers.getMonitor(serverInfo);
			String serverName;
			if (info != null) {
				Map<Integer, MonitorInfoBungee> previousInfoServers = MonitorServers.getServers(info.getOlympaServer());
				if (previousInfoServers != null) {
					MonitorInfoBungee previousInfo = previousInfoServers.get(info.getServerID());
					if (previousInfo != null)
						previous = previousInfo.getStatus();
				}
				info.setStatus(status);
				serverName = info.getName();
				MonitorServers.updateServer(serverInfo, info.getOlympaServer(), info);
			} else
				serverName = serverInfo.getName();
			OlympaBungee.getInstance().sendMessage("§7Serveur §e" + serverName + "§7 : " + previous.getNameColored() + " §7-> " + status.getNameColored() + " (via redis)");
		}
	}
}
