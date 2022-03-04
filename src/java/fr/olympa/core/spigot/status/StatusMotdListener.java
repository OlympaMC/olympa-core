package fr.olympa.core.spigot.status;

import java.util.Map.Entry;
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
import fr.olympa.api.utils.TimeEvaluator;
import fr.olympa.core.spigot.OlympaCore;

public class StatusMotdListener implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPing(ServerListPingEvent event) {
		TimeEvaluator time = null;
		if (OlympaModule.DEBUG)
			time = new TimeEvaluator("Spigot Custom Ping");
		OlympaCore core = OlympaCore.getInstance();
		StringJoiner sj = new StringJoiner(" ");
		try {
			sj.add("V2");
			sj.add(new ServerInfoAdvancedSpigot(core).toString());
		} catch (Exception e) {
			ServerStatus status = OlympaCore.getInstance().getStatus();
			JavaInstanceInfo machineInfo = new JavaInstanceInfo();
			sj.add(status.getName());
			sj.add(String.valueOf(TPS.getTPS()));
			sj.add(String.valueOf(machineInfo.getRawMemUsage()));
			sj.add(String.valueOf(machineInfo.getThreads() + "/" + machineInfo.getAllThreadsCreated()));
			Entry<String, String> entryVersion = core.getVersionHandler().getRangeVersionArray();
			sj.add(entryVersion.getKey());
			sj.add(entryVersion.getValue());
			sj.add(String.valueOf(core.getLastModifiedLong()));
			sj.add(new ServerInfoAdvancedSpigot(core).toString());
			if (OlympaModule.DEBUG) {
				time.print();
				core.sendMessage("&rDEBUG Ping > %s ", LinkSpigotBungee.getInstance().getGson().toJson(sj.toString())); // XXX ???????
			}
			e.printStackTrace();
		}
		event.setMotd(sj.toString());
	}
}
