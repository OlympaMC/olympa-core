package fr.olympa.core.bungee.protocol;

import fr.olympa.api.spigot.utils.ProtocolAPI;
import fr.olympa.core.bungee.utils.BungeeUtils;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class ProtocolListener implements Listener {

	@EventHandler(priority = EventPriority.LOW)
	public void onPreLogin(PreLoginEvent event) {
		if (event.isCancelled())
			return;
		PendingConnection connection = event.getConnection();
		ProtocolAPI protocolApi = ProtocolAPI.get(connection.getVersion());
		if (protocolApi == null || !protocolApi.isAllowed()) {
			ProtocolAPI lastVersion = ProtocolAPI.getLastVersion();
			event.setCancelReason(
					BungeeUtils.connectScreen("&cLa version que tu utilise n'est pas compatible avec le serveur.\n&4Utilise une version entre &c&l%s&4 et &c&l%s&4."
							+ "\n\n&4&nLa version recommandée est &c&n%s&4.", ProtocolAPI.getFirstVersion().getName(), lastVersion.getName(), lastVersion.getName()));
			event.setCancelled(true);
		}
	}
}
