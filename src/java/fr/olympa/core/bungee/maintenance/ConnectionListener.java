package fr.olympa.core.bungee.maintenance;

import java.util.logging.Level;

import fr.olympa.api.maintenance.MaintenanceStatus;
import fr.olympa.api.utils.ColorUtils;
import fr.olympa.core.bungee.utils.BungeeConfigUtils;
import fr.olympa.core.bungee.utils.BungeeUtils;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

@SuppressWarnings("deprecation")
public class ConnectionListener implements Listener {

	@EventHandler(priority = EventPriority.LOWEST)
	public void PreLoginEvent(PreLoginEvent event) {
		if (event.isCancelled()) {
			return;
		}
		String playername = event.getConnection().getName();
		Configuration config = BungeeConfigUtils.getConfig("maintenance");
		String message = config.getString("settings.message");
		String statusString = config.getString("settings.status");
		MaintenanceStatus maintenanceStatus = MaintenanceStatus.get(statusString);
		// Vérifie si le serveur n'est pas en maintenance

		String playerName = playername;
		if (!config.getStringList("whitelist").contains(playerName)) {
			if (maintenanceStatus == MaintenanceStatus.DEV) {
				event.setCancelReason(BungeeUtils.connectScreen("&6Le serveur est actuellement en développement.\n\n&3Plus d'infos sur le &btwitter &n@Olympa_fr"));
				event.setCancelled(true);
				ProxyServer.getInstance().getLogger().log(Level.INFO, ColorUtils.color("&d" + playername + " ne peux pas se connecter (serveur en dev)"));
			} else if (maintenanceStatus == MaintenanceStatus.SOON) {
				event.setCancelReason(BungeeUtils.connectScreen("&cNous ouvrons bientôt !.\n\n&3Plus d'infos sur le &btwitter &n@Olympa_fr"));
				event.setCancelled(true);
				ProxyServer.getInstance().getLogger().log(Level.INFO, ColorUtils.color("&d" + playername + " ne peux pas se connecter (serveur en dev, open soon)"));
			} else if (maintenanceStatus == null || maintenanceStatus != MaintenanceStatus.OPEN || maintenanceStatus == MaintenanceStatus.DEV) {
				if (message != null && !message.isEmpty()) {
					message = "\n\n&c&nRaison:&c " + message;
				}
				event.setCancelReason(BungeeUtils.connectScreen("&cLe serveur est actuellement en maintenance." + message));
				event.setCancelled(true);
				ProxyServer.getInstance().getLogger().log(Level.INFO, ColorUtils.color("&d" + playername + " ne peux pas se connecter (serveur en maintenance)"));
			}
		}
	}

}
