package fr.olympa.core.spigot;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitWorker;

import com.sun.management.ThreadMXBean;

import fr.olympa.api.chat.TxtComponentBuilder;

public class UsagePerPlugin {

	TxtComponentBuilder msg;

	public UsagePerPlugin() {
		Server server = Bukkit.getServer();
		//		TxtComponentBuilder builder = new TxtComponentBuilder(Prefix.DEFAULT, "Ram par plugins").extraSpliterBN();
		List<BukkitWorker> workers = server.getScheduler().getActiveWorkers();
		Map<Plugin, List<Long>> threadByPlugins = new HashMap<>();
		workers.forEach(bw -> {
			Plugin plugin = bw.getOwner();
			List<Long> threads = threadByPlugins.get(plugin);
			if (threads == null)
				threads = new ArrayList<>();
			Thread thread = bw.getThread();
			threads.add(thread.getId());

			threadByPlugins.put(bw.getOwner(), threads);
		});
		ThreadMXBean threadMXBean = (ThreadMXBean) ManagementFactory.getThreadMXBean();
		threadByPlugins.forEach((plugin, threads) -> {

			threads.forEach(l -> {
				threadMXBean.getThreadCpuTime(l); // utilsation en sec du proc par le thread
			});

		});
	}
}
