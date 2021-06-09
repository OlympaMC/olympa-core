package fr.olympa.core.spigot.status;

import java.util.StringJoiner;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.common.machine.JavaInstanceInfo;
import fr.olympa.api.common.module.OlympaModule;
import fr.olympa.api.common.server.ServerInfoAdvancedSpigot;
import fr.olympa.api.common.server.ServerStatus;
import fr.olympa.api.spigot.utils.TPS;
import fr.olympa.core.spigot.OlympaCore;

public class StatusMotdListener implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPing(ServerListPingEvent event) {
		ServerStatus status = OlympaCore.getInstance().getStatus();
		JavaInstanceInfo machineInfo = new JavaInstanceInfo();
		OlympaCore core = OlympaCore.getInstance();
		StringJoiner sj = new StringJoiner(" ");
		sj.add(status.getName());
		sj.add(String.valueOf(TPS.getTPS()));
		sj.add(String.valueOf(machineInfo.getRawMemUsage()));
		sj.add(String.valueOf(machineInfo.getThreads() + "/" + machineInfo.getAllThreadsCreated()));
		sj.add(core.getFirstVersion());
		sj.add(core.getLastVersion());
		sj.add(String.valueOf(core.getLastModifiedLong()));
		sj.add(new ServerInfoAdvancedSpigot(core).toString());
		if (OlympaModule.DEBUG)
			core.sendMessage("&rDEBUG Ping > %s ", LinkSpigotBungee.getInstance().getGson().toJson(sj.toString())); // XXX ???????
		event.setMotd(sj.toString());
	}
}
