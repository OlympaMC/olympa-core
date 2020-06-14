package fr.olympa.core.spigot.status;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.server.ServerListPingEvent;

import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.server.ServerStatus;
import fr.olympa.api.utils.ColorUtils;
import fr.olympa.api.utils.spigot.TPS;
import fr.olympa.core.spigot.OlympaCore;

public class StatusMotdListener implements Listener {

	@EventHandler
	public void onPing(ServerListPingEvent event) {
		ServerStatus status = OlympaCore.getInstance().getStatus();
		event.setMotd(status.getName() + " " + TPS.getTPS());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
		OlympaCore core = OlympaCore.getInstance();
		ServerStatus status = core.getStatus();
<<<<<<< src/java/fr/olympa/core/spigot/status/StatusMotdListener.java
		if (status == ServerStatus.OPEN)
			return;
=======
		if (status == ServerStatus.OPEN) {
			return;
		}
>>>>>>> src/java/fr/olympa/core/spigot/status/StatusMotdListener.java
		if (status == ServerStatus.CLOSE) {
			event.setKickMessage(ColorUtils.color("&cLe serveur &4" + core.getName() + "&c est fermé, réessaye dans quelques instants..."));
			return;
		}
		if (OlympaCorePermissions.SERVER_BYPASS_MAITENANCE_SPIGOT.hasPermission(event.getUniqueId()))
			return;
<<<<<<< src/java/fr/olympa/core/spigot/status/StatusMotdListener.java
=======
		}
>>>>>>> src/java/fr/olympa/core/spigot/status/StatusMotdListener.java
		if (status == ServerStatus.UNKNOWN) {
			event.setKickMessage(ColorUtils.color("&cImpossible de se connecter au serveur &4" + core.getName() + "&c, réessaye dans quelques instants..."));
			return;
		}
		event.setKickMessage(ColorUtils.color("&cLe serveur &4" + core.getName() + "&c est actuellement en " + status.getNameColored() + "&c."));
	}
}
