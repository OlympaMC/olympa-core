package fr.olympa.core.bungee.maintenance;

import fr.olympa.api.server.ServerStatus;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.utils.BungeeUtils;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class MaintenanceListener implements Listener {

	@EventHandler(priority = EventPriority.LOW)
	public void onLoginEvent(PreLoginEvent event) {
		if (event.isCancelled())
			return;
		String playername = event.getConnection().getName();
		OlympaBungee bungee = OlympaBungee.getInstance();
		ServerStatus status = bungee.getStatus();
		if (status == ServerStatus.OPEN)
			return;
		String playerName = playername;
		Configuration config = bungee.getMaintConfig();
		String message = config.getString("settings.message");
		if (config.getStringList("whitelist").contains(playerName))
			return;
		if (status == ServerStatus.DEV) {
			event.setCancelReason(BungeeUtils.connectScreen("&6Le serveur est actuellement en développement.\n\n&3Plus d'infos sur le &bTwitter &n@Olympa_fr\n&3Ou &bDiscord &nwww.discord.olympa.fr"));
			event.setCancelled(true);
			bungee.sendMessage("&d" + playername + " ne peux pas se connecter (serveur en dev)");
		} else if (status == ServerStatus.SOON) {
			event.setCancelReason(BungeeUtils.connectScreen("&cNous ouvrons bientôt !.\n\n&3Plus d'infos sur le &bTwitter &n@Olympa_fr\\n&3Ou &bDiscord &nwww.discord.olympa.fr"));
			event.setCancelled(true);
			bungee.sendMessage("&d" + playername + " ne peux pas se connecter (serveur en maintenance : open soon)");
		} else if (status == ServerStatus.BETA) {
			event.setCancelReason(BungeeUtils.connectScreen("&cNous sommes ouvert en bêta, tu dois t'inscrire sur le site :\n&4&nwww.olympa.fr&c."));
			event.setCancelled(true);
			bungee.sendMessage("&d" + playername + " ne peux pas se connecter (serveur en maintenance : beta)");
		} else if (status == ServerStatus.CLOSE_BETA) {
			event.setCancelReason(BungeeUtils.connectScreen("&cLe serveur est en bêta fermée, désolé."));
			event.setCancelled(true);
			bungee.sendMessage("&d" + playername + " ne peux pas se connecter (serveur en bêta fermée)");
		} else if (status == ServerStatus.MAINTENANCE || status == ServerStatus.CLOSE) {
			if (message != null && !message.isEmpty())
				message = "\n\n&c&nRaison:&c " + message;
			event.setCancelReason(BungeeUtils.connectScreen("&cLe serveur est actuellement en maintenance." + message));
			event.setCancelled(true);
			bungee.sendMessage("&d" + playername + " ne peux pas se connecter (serveur en maintenance: " + message + ")");
		}
	}

}
