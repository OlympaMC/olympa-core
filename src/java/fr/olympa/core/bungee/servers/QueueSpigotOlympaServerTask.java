package fr.olympa.core.bungee.servers;

import fr.olympa.api.common.chat.ColorUtils;
import fr.olympa.api.common.chat.TxtComponentBuilder;
import fr.olympa.api.common.server.OlympaServer;
import fr.olympa.api.common.server.ServerInfoAdvanced;
import fr.olympa.api.common.server.ServerInfoAdvancedBungee;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent.Reason;

public class QueueSpigotOlympaServerTask implements Runnable {
	ProxiedPlayer proxiedPlayer;
	ServerInfoAdvanced serverInfo;

	public QueueSpigotOlympaServerTask(ProxiedPlayer proxiedPlayer, ServerInfoAdvanced serverInfo) {
		this.proxiedPlayer = proxiedPlayer;
		this.serverInfo = serverInfo;
	}

	public ProxiedPlayer getPlayer() {
		return proxiedPlayer;
	}

	public ServerInfoAdvanced getServerInfo() {
		return serverInfo;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void run() {
		if (!proxiedPlayer.isConnected() || proxiedPlayer.getServer() == null) {
			ServersConnection.removeTryToConnect(proxiedPlayer);
			return;
		}
		OlympaServer olympaServer = serverInfo.getOlympaServer();
		//		String name = olympaServer;
		//		ServerInfo server = ServersConnection.getBestServer(olympaServer, null);
		ServerInfo server = ProxyServer.getInstance().getServerInfo(serverInfo.getName());
		if (server == null) {
			TextComponent text = new TextComponent(TextComponent.fromLegacyText(Prefix.DEFAULT_BAD + ColorUtils.color("Le serveur " + olympaServer.getNameCaps() + " n'est actuellement disponible, merci de patienter...")));
			text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ColorUtils.color("&cClique ici pour sortir de la file d'attente"))));
			text.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/leavequeue"));
			proxiedPlayer.sendMessage(text);
			return;
		}
		String serverName;
		ServerInfoAdvancedBungee monitorInfo = MonitorServers.getMonitor(server);
		if (monitorInfo == null)
			serverName = Utils.capitalize(server.getName());
		else
			serverName = monitorInfo.getHumanName();
		if (!ServersConnection.canPlayerConnect(server)) {
			TextComponent text = new TextComponent(TextComponent.fromLegacyText(Prefix.DEFAULT_BAD + ColorUtils.color("Tu es dans la file d'attente du &4" + olympaServer.getNameCaps() + "&c...")));
			text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ColorUtils.color("&cClique ici pour sortir de la file d'attente"))));
			text.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/leavequeue"));
			proxiedPlayer.sendMessage(text);
			return;
		}
		if (proxiedPlayer.getServer().getInfo().getName().equals(server.getName())) {
			ServersConnection.removeTryToConnect(proxiedPlayer);
			return;
		}
		//		proxiedPlayer.sendMessage(Prefix.DEFAULT_GOOD + ColorUtils.color("Tentative de connexion au serveur &2" + serverName + "&a..."));
		//		proxiedPlayer.sendMessage(Prefix.DEFAULT_GOOD.formatMessageB("Tentative de connexion au serveur §2%s§a...", serverName));
		proxiedPlayer.connect(server, (succes, error) -> {
			if (succes)
				proxiedPlayer.sendMessage(TxtComponentBuilder.of(Prefix.DEFAULT_GOOD, "Connexion au serveur %s établie !", serverName));
			else {
				proxiedPlayer.sendMessage(TxtComponentBuilder.of(Prefix.DEFAULT_BAD, "Echec de la connexion au serveur &4%s&c: &4%s&c. ", serverName, error.getMessage()));
				ServersConnection.removeTryToConnect(proxiedPlayer);
			}
		}, false, Reason.PLUGIN, 10000);
	}

}
