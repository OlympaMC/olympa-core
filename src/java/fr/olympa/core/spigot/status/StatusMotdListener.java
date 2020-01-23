package fr.olympa.core.spigot.status;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

import fr.olympa.api.maintenance.MaintenanceStatus;
import fr.olympa.api.utils.SpigotUtils;
import fr.olympa.core.spigot.OlympaCore;

public class StatusMotdListener implements Listener {

	@EventHandler
	public void onPing(ServerListPingEvent event) {
		MaintenanceStatus status = OlympaCore.getInstance().getStatus();
		event.setMotd(SpigotUtils.color(status.getNameColored()));
	}

}
