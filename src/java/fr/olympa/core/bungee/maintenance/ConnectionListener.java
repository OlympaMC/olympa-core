package fr.olympa.core.bungee.maintenance;

import java.util.logging.Level;

import fr.olympa.api.utils.SpigotUtils;
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
		int status = config.getInt("settings.status");

		// Vérifie si le serveur n'est pas en maintenance
		if (status == 1) {
			if (!config.getStringList("whitelist").contains(playername)) {
				if (config.getString("settings.message").isEmpty()) {
					event.setCancelReason(BungeeUtils.connectScreen("&cLe serveur est actuellement en maintenance."));
				} else {
					String reason = config.getString("settings.message");
					event.setCancelReason(BungeeUtils.connectScreen("&cLe serveur est actuellement en maintenance.\n\n&c&nRaison:&c " + reason));
				}
				event.setCancelled(true);
				ProxyServer.getInstance().getLogger().log(Level.INFO, SpigotUtils.color("&d" + playername + " ne peux pas se connecter (serveur en maintenance)"));
				return;
			}

		} else if (status == 2) {
			String playerName = playername;
			if (!config.getStringList("whitelist").contains(playerName)) {
				event.setCancelReason(BungeeUtils.connectScreen("&cLe serveur est actuellement en développement.\n\n&aPlus d'infos sur le twitter &n@Olympa_fr"));
				event.setCancelled(true);
				ProxyServer.getInstance().getLogger().log(Level.INFO, SpigotUtils.color("&d" + playername + " ne peux pas se connecter (serveur en dev)"));
				return;
			}
		} else if (status == 3) {
			String playerName = playername;
			if (!config.getStringList("whitelist").contains(playerName)) {
				event.setCancelReason(BungeeUtils.connectScreen("&cNous ouvrons bientôt !.\n\n&aPlus d'infos sur le twitter &n@Olympa_fr"));
				event.setCancelled(true);
				ProxyServer.getInstance().getLogger().log(Level.INFO, SpigotUtils.color("&d" + playername + " ne peux pas se connecter (serveur en dev, open soon)"));
				return;
			}
		}

	}

}
