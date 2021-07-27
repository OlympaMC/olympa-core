package fr.olympa.core.bungee.servers.connections;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import fr.olympa.api.common.chat.TxtComponentBuilder;
import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.common.server.OlympaServer;
import fr.olympa.api.common.server.ServerInfoAdvancedBungee;
import fr.olympa.api.common.sort.Sorting;
import fr.olympa.api.spigot.utils.ProtocolAPI;
import fr.olympa.api.utils.Prefix;
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
	ServerInfo otherServer;
	boolean w8ForConnect;
	boolean isLoginScreenConnection = false;

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
			else if (error != null) {
				player.sendMessage(TxtComponentBuilder.of(Prefix.DEFAULT_BAD, "Échec de la connexion au serveur &4%s&c: &4%s&c. ", serverName, error.getMessage()));
				if (!isDeadConnection()) {
					ServerInfo newServerInfo = findOther();
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

	@Deprecated
	public ServerInfoAdvancedBungee getBestServer(OlympaPlayer olympaPlayer, ProtocolAPI protocol) {
		if (!olympaServer.hasMultiServers() && otherServer == null)
			return MonitorServers.getServers(olympaServer).values().stream().filter(serv -> serv.canConnect(olympaPlayer)).findFirst().orElse(null);
		List<ServerInfoAdvancedBungee> serversCanBeReady;
		serversCanBeReady = MonitorServers.getServers(olympaServer).values().stream().filter(serv -> serv.isOpen()).collect(Collectors.toList());
		if (serversCanBeReady.isEmpty()) {
			player.sendMessage(Prefix.QUEUE.formatMessageB("&cAucun serveur &4%s&c n'est disponible.", olympaServer.getNameCaps()));
			// TODO open server
			// TODO create new server
			return null;
		} else {
			if (otherServer != null) {
				serversCanBeReady.removeIf(x -> otherServer.equals(x.getServerInfo()));
				if (serversCanBeReady.isEmpty()) {
					player.sendMessage(Prefix.QUEUE.formatMessageB("&cAucun serveur &4%s&c n'a été trouvé.", olympaServer.getNameCaps()));
					return null;
				}
			}
			serversCanBeReady.removeIf(x -> !x.canConnect(olympaPlayer));
			if (serversCanBeReady.isEmpty()) {
				player.sendMessage(Prefix.QUEUE.formatMessageB("&cTu n'as pas la permission de rejoindre les serveurs &4%s&c qui sont actuellement disponible.", olympaServer.getNameCaps()));
				return null;
			}
			serversCanBeReady.removeIf(x -> !x.canConnect(protocol));
			if (serversCanBeReady.isEmpty()) {
				player.sendMessage(Prefix.QUEUE.formatMessageB("&cAucun serveur &4%s&c ne supporte actuellement la version &4%s&c que tu utilises.", olympaServer.getNameCaps(), protocol.getCompleteName()));
				return null;
			}
			serversCanBeReady.removeIf(x -> !x.getOlympaServer().hasMultiServers() && x.getMaxPlayers() * 0.9 - x.getOnlinePlayers() <= 0);
			if (serversCanBeReady.isEmpty()) {
				player.sendMessage(Prefix.QUEUE.formatMessageB("&cLes serveurs %s sont actuellement pleins.", olympaServer.getNameCaps()));
				return null;
			}
			LinkedHashMap<ToLongFunction<ServerInfoAdvancedBungee>, Boolean> sortArgs = new LinkedHashMap<>();
			sortArgs.put(mi -> mi.getStatus().ordinal(), false);
			sortArgs.put(mi -> mi.getMaxPlayers(), false);
			sortArgs.put(mi -> mi.getMaxPlayers() - mi.getOnlinePlayers(), true);
			serversCanBeReady.sort(new Sorting<>(sortArgs));
			return serversCanBeReady.iterator().next();
		}
	}

	public ServerInfo findOther() {
		if (!olympaServer.hasMultiServers())
			return null;
		List<ServerInfoAdvancedBungee> monitorInfos = MonitorServers.getServers(olympaServer).entrySet().stream()
				.filter(e -> (otherServer == null || !e.getValue().getServerInfo().equals(otherServer)) && e.getValue().getStatus().canConnect()).map(e -> e.getValue())
				.collect(Collectors.toList());
		if (monitorInfos.isEmpty())
			return null;
		LinkedHashMap<ToLongFunction<ServerInfoAdvancedBungee>, Boolean> sortArgs = new LinkedHashMap<>();
		sortArgs.put(mi -> mi.getStatus().ordinal(), false);
		sortArgs.put(mi -> mi.getMaxPlayers(), false);
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
