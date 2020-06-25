package fr.olympa.core.bungee.servers;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import fr.olympa.api.server.OlympaServer;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.utils.BungeeUtils;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.ScheduledTask;

public class ServersConnection {
	
	private static Map<ProxiedPlayer, ScheduledTask> connect = new HashMap<>();
	
	public static boolean canPlayerConnect(ServerInfo server) {
		MonitorInfo monitor = MonitorServers.getMonitor(server);
		return monitor != null && monitor.getStatus().canConnect();
	}
	
	public static ServerInfo getBestServer(OlympaServer olympaServer, ServerInfo except) {
		if (!olympaServer.hasMultiServers()) return MonitorServers.getServers(olympaServer).values().stream().findFirst().map(MonitorInfo::getServerInfo).orElse(null);

		Map<ServerInfo, Integer> servers = MonitorServers.getServers(olympaServer).values().stream().filter(x -> x.isOpen() && (except == null || except.getName() != x.getName()) && (!olympaServer.hasMultiServers() || x.getMaxPlayers() * 0.9 - x.getOnlinePlayers() > 0)).collect(Collectors.toMap((si) -> si.getServerInfo(), (si) -> si.getMaxPlayers() - si.getOnlinePlayers()));
		Entry<ServerInfo, Integer> bestServer = servers.entrySet().stream().sorted(Map.Entry.comparingByValue()).findFirst().orElse(null);
		if (bestServer != null) return bestServer.getKey();
		// TODO create new server
		return null;
	}
	
	@SuppressWarnings("deprecation")
	public static ServerInfo getServerByNameOrIpPort(String nameOrIpPort) {
		Map<String, ServerInfo> servers = ProxyServer.getInstance().getServers();
		ServerInfo server = servers.get(nameOrIpPort);
		if (server == null) {
			String[] ipPort = nameOrIpPort.split(":");
			if (ipPort.length >= 2)
				server = servers.values().stream().filter(sr -> sr.getAddress().getAddress().getHostAddress().equals(ipPort[0]) && sr.getAddress().getPort() == Integer.parseInt(ipPort[1])).findFirst().orElse(null);
		}
		return server;
	}
	
	public static void removeTryToConnect(ProxiedPlayer player) {
		ScheduledTask task = connect.remove(player);
		if (task != null) task.cancel();
	}
	
	public static void tryConnect(ProxiedPlayer player, OlympaServer olympaServer) {
		ScheduledTask task = ProxyServer.getInstance().getScheduler().schedule(OlympaBungee.getInstance(), () -> tryConnectTo(player, olympaServer), 0, 30, TimeUnit.SECONDS);
		connect.put(player, task);
	}
	
	@SuppressWarnings("deprecation")
	private static void tryConnectTo(ProxiedPlayer player, OlympaServer olympaServer) {
		ServerInfo server = getBestServer(olympaServer, null);
		if (server == null && olympaServer.hasMultiServers()) {
			player.sendMessage(Prefix.DEFAULT_BAD + BungeeUtils.color("Aucun serveur " + olympaServer.getNameCaps() + " n'est actuellement disponible, merci de patienter..."));
			return;
		}
		String serverName = Utils.capitalize(server.getName());
		if (!canPlayerConnect(server)) {
			player.sendMessage(Prefix.DEFAULT_BAD + BungeeUtils.color("Tu es en file d'attente pour rejoindre le serveur &4" + serverName + "&c..."));
			return;
		}
		player.sendMessage(Prefix.DEFAULT_GOOD + BungeeUtils.color("Tentative de connexion au serveur &2" + serverName + "&a..."));
		player.connect(server);
		return;
		
	}
}
