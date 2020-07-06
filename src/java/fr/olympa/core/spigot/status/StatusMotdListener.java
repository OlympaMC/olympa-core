package fr.olympa.core.spigot.status;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

import fr.olympa.api.server.ServerStatus;
import fr.olympa.api.utils.machine.MachineInfo;
import fr.olympa.api.utils.spigot.TPS;
import fr.olympa.core.spigot.OlympaCore;

public class StatusMotdListener implements Listener {

	@EventHandler
	public void onPing(ServerListPingEvent event) {
		ServerStatus status = OlympaCore.getInstance().getStatus();
		MachineInfo machineInfo = new MachineInfo();
		event.setMotd(status.getName() + " " + TPS.getTPS() + " " + machineInfo.getMemUsage() + " " + machineInfo.getThreads());
	}
}
