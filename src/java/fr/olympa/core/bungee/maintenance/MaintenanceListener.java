package fr.olympa.core.bungee.maintenance;

import java.util.logging.Level;

import fr.olympa.api.server.ServerStatus;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.utils.BungeeUtils;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

@SuppressWarnings("deprecation")
public class MaintenanceListener implements Listener {

	@EventHandler(priority = EventPriority.LOWEST)
	public void PreLoginEvent(PreLoginEvent event) {
		if (event.isCancelled())
			return;
		String playername = event.getConnection().getName();
		Configuration config = OlympaBungee.getInstance().getMaintConfig();
		String message = config.getString("settings.message");
		String statusString = config.getString("settings.status");
		ServerStatus status = ServerStatus.get(statusString);
		// Vérifie si le serveur n'est pas en maintenance

		String playerName = playername;
		if (!config.getStringList("whitelist").contains(playerName))
			if (status == ServerStatus.DEV) {
				event.setCancelReason(BungeeUtils.connectScreen("&6Le serveur est actuellement en développement.\n\n&3Plus d'infos sur le &bTwitter &n@Olympa_fr\n&3Ou &bDiscord &nwww.discord.olympa.fr"));
				event.setCancelled(true);
				ProxyServer.getInstance().getLogger().log(Level.INFO, BungeeUtils.color("&d" + playername + " ne peux pas se connecter (serveur en dev)"));
			} else if (status == ServerStatus.SOON) {
				event.setCancelReason(BungeeUtils.connectScreen("&cNous ouvrons bientôt !.\n\n&3Plus d'infos sur le &bTwitter &n@Olympa_fr\\n&3Ou &bDiscord &nwww.discord.olympa.fr"));
				event.setCancelled(true);
				ProxyServer.getInstance().getLogger().log(Level.INFO, BungeeUtils.color("&d" + playername + " ne peux pas se connecter (serveur en dev, open soon)"));
			} else if (status == null || status != ServerStatus.OPEN || status == ServerStatus.DEV) {
				if (message != null && !message.isEmpty())
					message = "\n\n&c&nRaison:&c " + message;
				event.setCancelReason(BungeeUtils.connectScreen("&cLe serveur est actuellement en maintenance." + message));
				event.setCancelled(true);
				ProxyServer.getInstance().getLogger().log(Level.INFO, BungeeUtils.color("&d" + playername + " ne peux pas se connecter (serveur en maintenance)"));
			}
	}

}
