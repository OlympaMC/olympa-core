package fr.olympa.core.spigot.status;

import java.util.StringJoiner;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

import fr.olympa.api.module.OlympaModule;
import fr.olympa.api.server.ServerDebugInit;
import fr.olympa.api.server.ServerStatus;
import fr.olympa.api.utils.machine.MachineInfo;
import fr.olympa.api.utils.spigot.TPS;
import fr.olympa.core.spigot.OlympaCore;

public class StatusMotdListener implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPing(ServerListPingEvent event) {
		ServerStatus status = OlympaCore.getInstance().getStatus();
		MachineInfo machineInfo = new MachineInfo();
		OlympaCore core = OlympaCore.getInstance();
		StringJoiner sj = new StringJoiner(" ");
		sj.add(status.getName());
		sj.add(String.valueOf(TPS.getTPS()));
		sj.add(String.valueOf(machineInfo.getRawMemUsage()));
		sj.add(String.valueOf(machineInfo.getThreads() + "/" + machineInfo.getAllThreadsCreated()));
		sj.add(core.getFirstVersion());
		sj.add(core.getLastVersion());
		sj.add(core.getLastModifiedTime());
		sj.add(new ServerDebugInit(core).toString());
		event.setMotd(sj.toString());
		if (OlympaModule.DEBUG)
			core.sendMessage("&rDEBUG ServerDebugInfo > %s ", new ServerDebugInit(core).toString());
	}
}
