package fr.olympa.core.bungee.servers;

import fr.olympa.api.server.OlympaServer;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.utils.BungeeUtils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class QueueSpigotTask implements Runnable {

	ProxiedPlayer proxiedPlayer;
	OlympaServer olympaServer;

	public QueueSpigotTask(ProxiedPlayer proxiedPlayer, OlympaServer olympaServer) {
		this.proxiedPlayer = proxiedPlayer;
		this.olympaServer = olympaServer;
	}

	public ProxiedPlayer getPlayer() {
		return proxiedPlayer;
	}

	public OlympaServer getOlympaServer() {
		return olympaServer;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void run() {
		if (!proxiedPlayer.isConnected()) {
			ServersConnection.removeTryToConnect(proxiedPlayer);
			return;
		}
		ServerInfo server = ServersConnection.getBestServer(olympaServer, null);
		if (server == null && olympaServer.hasMultiServers()) {
			TextComponent text = new TextComponent(TextComponent.fromLegacyText(Prefix.DEFAULT_BAD + BungeeUtils.color("Aucun serveur " + olympaServer.getNameCaps() + " n'est actuellement disponible, merci de patienter...")));
			text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(BungeeUtils.color("&cClique ici pour sortir de la file d'attente"))));
			text.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/leavequeue"));
			proxiedPlayer.sendMessage(text);
			return;
		}
		String serverName = Utils.capitalize(server.getName());
		if (!ServersConnection.canPlayerConnect(server)) {
			TextComponent text = new TextComponent(TextComponent.fromLegacyText(Prefix.DEFAULT_BAD + BungeeUtils.color("Tu es dans la file d'attente du &4" + serverName + "&c...")));
			text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(BungeeUtils.color("&cClique ici pour sortir de la file d'attente"))));
			text.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/leavequeue"));
			proxiedPlayer.sendMessage(text);
			return;
		}
		if (proxiedPlayer.getServer().getInfo().getName().equals(server.getName())) {
			ServersConnection.removeTryToConnect(proxiedPlayer);
			return;
		}
		proxiedPlayer.sendMessage(Prefix.DEFAULT_GOOD + BungeeUtils.color("Tentative de connexion au serveur &2" + serverName + "&a..."));
		proxiedPlayer.connect(server);
	}

}
