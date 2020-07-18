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

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPreLogin(PreLoginEvent event) {
		PendingConnection connection = event.getConnection();
		String name = connection.getName();
		int timeToW8 = QueueHandler.add(name);
		while (QueueHandler.isInQueue(name)) {
			try {
				Thread.sleep(timeToW8 * QueueHandler.TIME_BETWEEN_2);
			} catch (Exception e) {
				e.printStackTrace();
				event.setCancelReason(TextComponent.fromLegacyText(BungeeUtils.connectScreen("§cUne erreur est survenue.")));
				event.setCancelled(true);
				return;
			}
			if (timeToW8 > 1)
				timeToW8--;
		}
	}
}
