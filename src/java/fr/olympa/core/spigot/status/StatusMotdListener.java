package fr.olympa.core.spigot.status;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.server.ServerListPingEvent;

import fr.olympa.api.maintenance.MaintenanceStatus;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.utils.ColorUtils;
import fr.olympa.api.utils.TPS;
import fr.olympa.core.spigot.OlympaCore;

public class StatusMotdListener implements Listener {

	@EventHandler
	public void onPing(ServerListPingEvent event) {
		MaintenanceStatus status = OlympaCore.getInstance().getStatus();
		event.setMotd(status.getName() + " " + TPS.getTPS());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
		OlympaCore core = OlympaCore.getInstance();
		MaintenanceStatus status = core.getStatus();
		if (status == MaintenanceStatus.OPEN) {
			return;
		}
		if (status == MaintenanceStatus.CLOSE) {
			event.setKickMessage(ColorUtils.color("&cLe serveur &4" + core.getName() + "&c est fermé, réessaye dans quelques instants..."));
			return;
		}
		if (OlympaCorePermissions.SERVER_BYPASS_MAITENANCE_SPIGOT.hasPermission(event.getUniqueId())) {
			return;
		}
		if (status == MaintenanceStatus.UNKNOWN) {
			event.setKickMessage(ColorUtils.color("&cImpossible de se connecter au serveur &4" + core.getName() + "&c, réessaye dans quelques instants..."));
			return;
		}
		event.setKickMessage(ColorUtils.color("&cLe serveur &4" + core.getName() + "&c est actuellement en " + status.getNameColored() + "&c."));
	}
}
