package fr.olympa.core.bungee.servers;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import fr.olympa.api.utils.Prefix;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.utils.BungeeUtils;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class ServersConnection {

	public static ServerInfo getAuth() {
		return getAuth(null);
	}

	public static ServerInfo getAuth(ServerInfo noThis) {
		Map<ServerInfo, Integer> auths = MonitorServers.getServers().entrySet().stream().filter(entry -> {
			ServerInfo si = entry.getKey();
			ServerPing sp = entry.getValue().getServerPing();
			return noThis != si && sp != null && si.getName().startsWith("auth") && sp.getPlayers().getMax() - sp.getPlayers().getOnline() > 0;
		}).collect(Collectors.toMap((entry) -> entry.getKey(), (entry) -> entry.getValue().getServerPing().getPlayers().getMax() / 2 - entry.getValue().getServerPing().getPlayers().getOnline()));
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
		Map<ServerInfo, Integer> lobbys = MonitorServers.getServers().entrySet().stream().filter(entry -> {
			ServerInfo si = entry.getKey();
			ServerPing sp = entry.getValue().getServerPing();
			return noThis != si && sp != null && si.getName().startsWith("lobby") && sp.getPlayers().getMax() / 2 - sp.getPlayers().getOnline() > 0;
		}).collect(Collectors.toMap((entry) -> entry.getKey(), (entry) -> entry.getValue().getServerPing().getPlayers().getMax() / 2 - entry.getValue().getServerPing().getPlayers().getOnline()));
		// TODO add sort by name1 name2 name3
		Entry<ServerInfo, Integer> lobby = lobbys.entrySet().stream().sorted(Map.Entry.comparingByValue()).findFirst().orElse(null);
		if (lobby != null) {
			return lobby.getKey();
		}
		// TODO create new server
		return null;
	}

	public static boolean isAuth(ProxiedPlayer player) {
		return player.getServer() != null && player.getServer().getInfo().getName().startsWith("auth");
	}

	@SuppressWarnings("deprecation")
	public static void tryConnectToLobby(ProxiedPlayer player) {
		ServerInfo lobby = ServersConnection.getLobby();
		if (lobby != null) {
			//player.setReconnectServer(lobby);
			player.connect(lobby);
			return;
		}
		player.sendMessage(Prefix.DEFAULT_BAD + BungeeUtils.color("&cAucun lobby n'est actuellement disponible merci de patienter ..."));
		ProxyServer.getInstance().getScheduler().schedule(OlympaBungee.getInstance(), () -> tryConnectToLobby(player), 10, TimeUnit.SECONDS);
	}
}
