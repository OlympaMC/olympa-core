package fr.olympa.core.bungee.connectionqueue;

import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class ConnectionQueueListener implements Listener {

	@EventHandler
	public void onPlayerDisconnect(PlayerDisconnectEvent event) {
		QueueHandler.remove(event.getPlayer().getName());
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPreLogin(PreLoginEvent event) {
		PendingConnection connection = event.getConnection();
		String name = connection.getName();
		int timeToW8 = QueueHandler.add(name);
		//		if ((timeToW8 = QueueHandler.add(name)) == -1) {
		//			event.setCancelReason(BungeeUtils.connectScreen("&cMerci de ne pas spammer le déco/reco."));
		//			event.setCancelled(true);
		//			return;
		//		}
		while (QueueHandler.isInQueue(name)) {
			blockThread(timeToW8);
			if (timeToW8 > 7)
				timeToW8--;
		}
	}

	public void blockThread(int i) {
		try {
			Thread.sleep(i * QueueHandler.TIME_BETWEEN_2 + 1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
