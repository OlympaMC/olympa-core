package fr.olympa.core.bungee.connectionqueue;

import net.md_5.bungee.api.ServerConnectRequest;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerConnectEvent.Reason;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ConnectionQueueListener implements Listener {

	@EventHandler
	public void onServerConnect(ServerConnectEvent event) {
		if (event.getReason() == Reason.JOIN_PROXY)
			return;
		ProxiedPlayer player = event.getPlayer();
		ServerInfo target = event.getTarget();
		ServerConnectRequest request = event.getRequest();
		//		request.setConnectTimeout(connectTimeout);
	}

	@EventHandler
	public void onServerConnected(ServerConnectedEvent event) {

	}

}
