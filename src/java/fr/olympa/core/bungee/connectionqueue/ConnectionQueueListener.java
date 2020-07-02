package fr.olympa.core.bungee.connectionqueue;

import fr.olympa.core.bungee.utils.BungeeUtils;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

@SuppressWarnings("deprecation")
public class ConnectionQueueListener implements Listener {

	@EventHandler
	public void onPlayerDisconnect(PlayerDisconnectEvent event) {
		QueueHandler.remove(event.getPlayer().getName());
	}

	//	@EventHandler
	//	public void onServerConnect(ServerConnectEvent event) {
	//		if (event.getReason() == Reason.JOIN_PROXY)
	//			return;
	//		ProxiedPlayer player = event.getPlayer();
	//		ServerInfo target = event.getTarget();
	//		ServerConnectRequest request = event.getRequest();
	//		//		request.setConnectTimeout(connectTimeout);
	//	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPreLogin(PreLoginEvent event) {
		PendingConnection connection = event.getConnection();
		String name = connection.getName();
		int timeToW8 = 1;
		if ((timeToW8 = QueueHandler.add(name)) == -1) {
			event.setCancelReason(BungeeUtils.connectScreen("&cMerci de ne pas spammer le déco/reco."));
			event.setCancelled(true);
			return;
		}
		while (QueueHandler.isInQueue(name)) {
			blockThread(timeToW8);
			if (timeToW8 > 7)
				timeToW8--;
		}
	}

	public void blockThread(int i) {
		try {
			Thread.sleep(i * QueueHandler.TIME_BETWEEN_2);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
