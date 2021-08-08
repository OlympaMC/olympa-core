package fr.olympa.core.bungee.connectionqueue;

import fr.olympa.api.bungee.utils.BungeeUtils;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ConnectionQueueListener implements Listener {

	@EventHandler
	public void onPlayerDisconnect(PlayerDisconnectEvent event) {
		QueueHandler.remove(event.getPlayer().getName());
	}

	@EventHandler(priority = -128)
	public void onPreLogin(PreLoginEvent event) {
		if (!QueueHandler.ENABLED)
			return;
		PendingConnection connection = event.getConnection();
		String name = connection.getName();
		int timeToW8 = QueueHandler.add(name);
		if (timeToW8 < 0) {
			event.setCancelled(true);
			if (timeToW8 == -1)
				event.setCancelReason(BungeeUtils.connectScreen("&cIl y a déjà une connexion en attente avec le pseudo %s, réessaye dans %s.", name, QueueHandler.getTimeToW8String(name)));
			else if (timeToW8 == -2)
				event.setCancelReason(BungeeUtils.connectScreen("&cL'attente pour te connecter est de &4%s&c\n&4Réessaye plus tard.", QueueHandler.getQueueTimeString()));
		}

		while (QueueHandler.isInQueue(name) && connection.isConnected())
			try {
				Thread.sleep(timeToW8);
			} catch (InterruptedException e) {
				e.printStackTrace();
				event.setCancelReason(BungeeUtils.connectScreen("§cUne erreur est survenue."));
				event.setCancelled(true);
				return;
			}
		//			System.out.println("ENDWAIT " + timeToW8);
	}
}
