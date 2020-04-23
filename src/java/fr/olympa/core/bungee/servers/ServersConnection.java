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
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.ScheduledTask;

public class ServersConnection {

	static Map<ProxiedPlayer, ScheduledTask> connect = new HashMap<>();

	public static ServerInfo getAuth() {
		return getAuth(null);
	}

	public static ServerInfo getAuth(ServerInfo noThis) {
		Map<ServerInfo, Integer> auths = MonitorServers.getLastServerInfo().stream().filter(si -> {
			return si.getError() == null && (noThis == null || noThis.getName() != si.getName()) && si.getName().startsWith("auth") && si.getMaxPlayers() - si.getOnlinePlayer() > 0;
		}).collect(Collectors.toMap((si) -> si.getServerInfo(), (si) -> si.getMaxPlayers() - si.getOnlinePlayer()));
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
		Map<ServerInfo, Integer> lobbys = MonitorServers.getLastServerInfo().stream().filter(si -> {
			return si.getError() == null && (noThis == null || noThis.getName() != si.getName()) && si.getName().startsWith("lobby") && si.getMaxPlayers() / 2 - si.getOnlinePlayer() > 0;
		}).collect(Collectors.toMap((si) -> si.getServerInfo(), (si) -> si.getMaxPlayers() / 2 - si.getOnlinePlayer()));
		// TODO add sort by name1 name2 name3
		Entry<ServerInfo, Integer> lobby = lobbys.entrySet().stream().sorted(Map.Entry.comparingByValue()).findFirst().orElse(null);
		if (lobby != null) {
			return lobby.getKey();
		}
		// TODO create new server
		return null;
	}

	public static ServerInfo getServer(String name) {
		return MonitorServers.getLastServerInfo().stream().filter(si -> si.getError() != null && si.getName().startsWith(name.toLowerCase())).map(MonitorInfo::getServerInfo).findFirst().orElse(null);
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
			player.sendMessage(Prefix.DEFAULT_BAD + BungeeUtils.color("Aucun serveur " + olympaServer.getNameCaps() + " n'est actuellement disponible merci de patienter ..."));
		}
	}
}
