package fr.olympa.core.bungee.servers;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import fr.olympa.api.objects.OlympaServer;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.utils.BungeeUtils;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.ScheduledTask;

public class ServersConnection {

	static Map<ProxiedPlayer, ScheduledTask> connect = new HashMap<>();

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

	public static ServerInfo getServer(String name) {
		return MonitorServers.getServers().entrySet().stream().filter(entry -> entry.getValue().getServerPing() != null && entry.getKey().getName().startsWith(name.toLowerCase())).map(entry -> entry.getKey())
				.findFirst().orElse(null);
	}

	public static void removeTryToConnect(ProxiedPlayer player) {
		ScheduledTask task = connect.get(player);
		if (task != null) {
			task.cancel();
			connect.remove(player);
		}
	}

	public static void tryConnect(ProxiedPlayer player, OlympaServer olympaServer) {
		ScheduledTask task = ProxyServer.getInstance().getScheduler().schedule(OlympaBungee.getInstance(), () -> tryConnectTo(player, olympaServer), 0, 10, TimeUnit.SECONDS);
		connect.put(player, task);
	}

	@SuppressWarnings("deprecation")
	private static void tryConnectTo(ProxiedPlayer player, OlympaServer olympaServer) {
		ServerInfo server;
		switch (olympaServer) {
		case LOBBY:
			server = ServersConnection.getLobby();
			break;
		case AUTH:
			server = ServersConnection.getAuth();
			break;
		default:
			server = getServer(olympaServer.getName());
			break;
		}
		if (server != null) {
			player.connect(server);
			player.sendMessage(Prefix.DEFAULT_GOOD + BungeeUtils.color("Connexion au serveur " + server.getName() + "..."));
			removeTryToConnect(player);
			return;
		} else {
			player.sendMessage(Prefix.DEFAULT_BAD + BungeeUtils.color("Aucun " + olympaServer.getNameCaps() + " n'est actuellement disponible merci de patienter ..."));
		}
	}
}
