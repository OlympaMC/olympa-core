package fr.olympa.core.bungee.servers.connections;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import fr.olympa.api.chat.TxtComponentBuilder;
import fr.olympa.api.server.OlympaServer;
import fr.olympa.api.sort.Sorting;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.bungee.servers.MonitorInfoBungee;
import fr.olympa.core.bungee.servers.MonitorServers;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * Working progress to remplace ServersConnection
 * @author Tristiisch
 *
 */
public class ServerConnection {

	private static final int TIMEOUT = 3000;
	private static final int IMAX = 5;
	@Nonnull
	OlympaServer olympaServer;
	@Nonnull
	ProxiedPlayer player;
	@Nullable
	String niceServerName;
	@Nullable
	ServerInfo serverInfo;
	@Nullable
	MonitorInfoBungee monitorInfo;
	boolean w8ForConnect;

	int i = 0;
	int iDone = 0;

	public void killConnection() {
		i = IMAX;
	}

	public boolean isDeadConnection() {
		return i >= IMAX;
	}

	public boolean isPendengConnection() {
		return i != iDone;
	}

	public void connect() {
		if (++i >= IMAX)
			return;
		String serverName = getServerNameCaps();
		Callback<Boolean> callback = (result, error) -> {
			iDone++;
			w8ForConnect = false;
			if (result)
				player.sendMessage(TxtComponentBuilder.of(Prefix.DEFAULT_GOOD, "Connexion au serveur %s établie !", serverName));
			else {
				player.sendMessage(TxtComponentBuilder.of(Prefix.DEFAULT_BAD, "Echec de la connexion au serveur &4%s&c: &4%s&c. ", serverName, error.getMessage()));
				if (!isDeadConnection()) {
					ServerInfo newServerInfo = findOther(serverInfo);
					if (newServerInfo != null) {
						serverInfo = newServerInfo;
						connect();
					} else
						w8ForConnect = true;
				}
			}
		};
		player.connect(serverInfo, callback, false, TIMEOUT);
	}

	public static ServerInfo getBestServer(OlympaServer olympaServer, ServerInfo except, ProxiedPlayer w8forConnect) {
		if (!olympaServer.hasMultiServers())
			return MonitorServers.getServers(olympaServer).values().stream().findFirst().map(MonitorInfoBungee::getServerInfo).orElse(null);

		Map<ServerInfo, Integer> servers = MonitorServers.getServers(olympaServer).values().stream()
				.filter(x -> x.getStatus().canConnect() && (except == null || !except.getName().equals(x.getName())) && (!x.getOlympaServer().hasMultiServers() || x.getMaxPlayers() * 0.9 - x.getOnlinePlayers() > 0))
				.collect(Collectors.toMap((si) -> si.getServerInfo(), (si) -> si.getMaxPlayers() - si.getOnlinePlayers()));
		ServerInfo bestServer = servers.entrySet().stream().sorted(Map.Entry.comparingByValue()).map(Entry::getKey).findFirst().orElse(null);
		if (bestServer != null)
			return bestServer;
		return null;
		// TODO create new server
	}

	public ServerInfo findOther(ServerInfo other) {
		if (!olympaServer.hasMultiServers())
			return null;
		List<MonitorInfoBungee> monitorInfos = MonitorServers.getServers(olympaServer).entrySet().stream()
				.filter(e -> (other == null || !e.getValue().getServerInfo().equals(other)) && e.getValue().getStatus().canConnect()).map(e -> e.getValue())
				.collect(Collectors.toList());
		if (monitorInfos.isEmpty())
			return null;
		LinkedHashMap<ToLongFunction<MonitorInfoBungee>, Boolean> sortArgs = new LinkedHashMap<>();

		sortArgs.put(mi -> mi.getStatus().ordinal(), false);
		sortArgs.put(mi -> mi.getMaxPlayers() - mi.getOnlinePlayers(), true);
		monitorInfos.sort(new Sorting<>(sortArgs));
		return monitorInfos.get(0).getServerInfo();
		//		monitorInfos.stream().filter(mi -> {
		//			ServerInfo serv = mi.getServerInfo();
		//			ServerStatus status = mi.getStatus();
		//			status.ordinal();
		//			if (status.getPermission().hasPermission(player.getUniqueId())) {
		//
		//			}
		//		});
	}

	public String getServerNameCaps() {
		if (niceServerName != null)
			return olympaServer.getNameCaps() + " n°" + niceServerName.replaceFirst("^[A-Za-z]+", "");
		return olympaServer.getNameCaps();
	}
}
