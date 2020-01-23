package fr.olympa.core.bungee.servers;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import net.md_5.bungee.api.config.ServerInfo;

public class ServersConnection {

	public static ServerInfo getAuth() {
		return getAuth(null);
	}

	public static ServerInfo getAuth(ServerInfo noThis) {
		Map<ServerInfo, Integer> auths = MonitorServers.getServers().entrySet().stream().filter(entry -> noThis != entry.getKey() && entry.getValue() != null && entry.getKey().getName().startsWith("auth")
				&& entry.getValue().getPlayers().getMax() - entry.getValue().getPlayers().getOnline() > 0)
				.collect(Collectors.toMap((entry) -> entry.getKey(), (entry) -> entry.getValue().getPlayers().getMax() - entry.getValue().getPlayers().getOnline()));
		// TODO add sort by name1 name2 name3
		Entry<ServerInfo, Integer> auth = auths.entrySet().stream().sorted(Map.Entry.comparingByValue()).findFirst().orElse(null);
		if (auth != null) {
			return auth.getKey();
		}
		// TODO create new server
		return null;
	}

	public static ServerInfo getLobby() {
		return getLobby(null);
	}

	public static ServerInfo getLobby(ServerInfo noThis) {
		Map<ServerInfo, Integer> lobbys = MonitorServers.getServers().entrySet().stream().filter(entry -> noThis != entry.getKey() && entry.getValue() != null && entry.getKey().getName().startsWith("auth")
				&& entry.getValue().getPlayers().getMax() / 2 - entry.getValue().getPlayers().getOnline() > 0)
				.collect(Collectors.toMap((entry) -> entry.getKey(), (entry) -> entry.getValue().getPlayers().getMax() / 2 - entry.getValue().getPlayers().getOnline()));
		// TODO add sort by name1 name2 name3
		Entry<ServerInfo, Integer> lobby = lobbys.entrySet().stream().sorted(Map.Entry.comparingByValue()).findFirst().orElse(null);
		if (lobby != null) {
			return lobby.getKey();
		}
		// TODO create new server
		return null;
	}
}
