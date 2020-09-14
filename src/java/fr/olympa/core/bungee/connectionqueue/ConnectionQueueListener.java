package fr.olympa.core.bungee.connectionqueue;

import fr.olympa.core.bungee.utils.BungeeUtils;
import net.md_5.bungee.api.chat.TextComponent;
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

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPreLogin(PreLoginEvent event) {
		PendingConnection connection = event.getConnection();
		String name = connection.getName();
		int timeToW8 = QueueHandler.add(name);
		if (timeToW8 == -1) {
			event.setCancelReason(BungeeUtils.connectScreen(String.join("&cIl y a déjà une connexion en attente avec le pseudo %s, réésaye dans %s secondes.", name, String.valueOf(QueueHandler.getTimeToW8(name) / 1000))));
			event.setCancelled(true);
		}
		while (QueueHandler.isInQueue(name) && connection.isConnected()) {
			System.out.println("WAIT " + timeToW8);
			try {
				Thread.sleep(timeToW8);
			} catch (Exception e) {
				e.printStackTrace();
				event.setCancelReason(TextComponent.fromLegacyText(BungeeUtils.connectScreen("§cUne erreur est survenue.")));
				event.setCancelled(true);
				return;
			}
			System.out.println("ENDWAIT " + timeToW8);
			timeToW8 = QueueHandler.getTimeToW8(name);
		}
	}
}
