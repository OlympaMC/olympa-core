package fr.olympa.core.bungee.protocol;

import fr.olympa.core.bungee.api.ProtocolAPI;
import fr.olympa.core.bungee.utils.BungeeUtils;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class ProtocolListener implements Listener {

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPreLogin(PreLoginEvent event) {
		if (event.isCancelled()) {
			return;
		}
		PendingConnection connection = event.getConnection();
		ProtocolAPI protocolApi = ProtocolAPI.get(connection.getVersion());
		if (protocolApi == null || !protocolApi.isAllow()) {
			event.setCancelReason(BungeeUtils.connectScreen("&cLa version que tu utilise n'est pas compatible avec le serveur.\n&4Utilise la &lversion 1.9 à 1.15.2&4."));
			event.setCancelled(true);
		}
	}
}
