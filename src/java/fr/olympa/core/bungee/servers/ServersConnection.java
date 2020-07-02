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
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
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
		if (!olympaServer.hasMultiServers())
			return MonitorServers.getServers(olympaServer).values().stream().findFirst().map(MonitorInfo::getServerInfo).orElse(null);

		Map<ServerInfo, Integer> servers = MonitorServers.getServers(olympaServer).values().stream()
				.filter(x -> x.isOpen() && (except == null || except.getName() != x.getName()) && (!olympaServer.hasMultiServers() || x.getMaxPlayers() * 0.9 - x.getOnlinePlayers() > 0))
				.collect(Collectors.toMap((si) -> si.getServerInfo(), (si) -> si.getMaxPlayers() - si.getOnlinePlayers()));
		Entry<ServerInfo, Integer> bestServer = servers.entrySet().stream().sorted(Map.Entry.comparingByValue()).findFirst().orElse(null);
		if (bestServer != null)
			return bestServer.getKey();
		// TODO create new server
		return null;
	}

	public static ServerInfo getServerByNameOrIpPort(String nameOrIpPort) {
		Map<String, ServerInfo> servers = ProxyServer.getInstance().getServers();
		ServerInfo server = servers.get(nameOrIpPort);
		if (server == null) {
			String[] ipPort = nameOrIpPort.split(":");
			if (ipPort.length >= 2) {
				String ip = ipPort[0].replace("localhost", "172.0.0.1");
				server = servers.values().stream().filter(sr -> {
					return sr.getAddress().getAddress().getHostAddress().equals(ip) && sr.getAddress().getPort() == Integer.parseInt(ipPort[1]);
				}).findFirst().orElse(null);
			}
		}
		return server;
	}

	public static boolean removeTryToConnect(ProxiedPlayer player) {
		ScheduledTask task = connect.remove(player);
		if (task != null) {
			task.cancel();
			return true;
		}
		return false;
	}

	public static void tryConnect(ProxiedPlayer player, OlympaServer olympaServer) {
		ScheduledTask task = ProxyServer.getInstance().getScheduler().schedule(OlympaBungee.getInstance(), () -> tryConnectTo(player, olympaServer), 0, 30, TimeUnit.SECONDS);
		connect.put(player, task);
	}

	@SuppressWarnings("deprecation")
	private static void tryConnectTo(ProxiedPlayer player, OlympaServer olympaServer) {
		ServerInfo server = getBestServer(olympaServer, null);
		if (server == null && olympaServer.hasMultiServers()) {
			TextComponent text = new TextComponent(TextComponent.fromLegacyText(Prefix.DEFAULT_BAD + BungeeUtils.color("Aucun serveur " + olympaServer.getNameCaps() + " n'est actuellement disponible, merci de patienter...")));
			text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(BungeeUtils.color("&cClique ici pour sortir de la file d'attente"))));
			text.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/leavefile"));
			player.sendMessage(text);
			return;
		}
		String serverName = Utils.capitalize(server.getName());
		if (!canPlayerConnect(server)) {
			TextComponent text = new TextComponent(TextComponent.fromLegacyText(Prefix.DEFAULT_BAD + BungeeUtils.color("Tu es en file d'attente pour rejoindre le serveur &4" + serverName + "&c...")));
			text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(BungeeUtils.color("&cClique ici pour sortir de la file d'attente"))));
			text.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/leavefile"));
			player.sendMessage(text);
			return;
		}
		player.sendMessage(Prefix.DEFAULT_GOOD + BungeeUtils.color("Tentative de connexion au serveur &2" + serverName + "&a..."));
		player.connect(server);
		return;
	}
}
