package fr.olympa.core.bungee.servers;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import net.md_5.bungee.api.config.ServerInfo;

public class ServersConnection {

	public static ServerInfo getLobby() {
		Map<ServerInfo, Integer> lobbys = MonitorServers.getServers().entrySet().stream().filter(entry -> !entry.getKey().getName().startsWith("lobby") || entry.getValue() == null)
				.collect(Collectors.toMap((entry) -> entry.getKey(), (entry) -> entry.getValue().getPlayers().getMax() / 2 - entry.getValue().getPlayers().getOnline()));
		Entry<ServerInfo, Integer> lobby = lobbys.entrySet().stream().sorted(Map.Entry.comparingByValue()).findFirst().orElse(null);
		if (lobby != null) {
			return lobby.getKey();
		}
		return null;
	}
}
