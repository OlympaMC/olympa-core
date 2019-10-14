package fr.tristiisch.olympa;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

import fr.tristiisch.olympa.api.plugin.OlympaPlugin;
import fr.tristiisch.olympa.core.ban.commands.BanCommand;
import fr.tristiisch.olympa.core.ban.listeners.SanctionListener;
import fr.tristiisch.olympa.core.chat.ChatCommand;
import fr.tristiisch.olympa.core.chat.ChatListener;
import fr.tristiisch.olympa.core.datamanagment.listeners.DataManagmentListener;
import fr.tristiisch.olympa.core.datamanagment.redis.RedisAccess;
import fr.tristiisch.olympa.core.datamanagment.sql.DatabaseManager;
import fr.tristiisch.olympa.core.groups.GroupCommand;
import fr.tristiisch.olympa.core.groups.GroupListener;
import fr.tristiisch.olympa.core.gui.GuiListener;
import fr.tristiisch.olympa.core.report.ReportCommand;
import fr.tristiisch.olympa.core.report.ReportListener;
import fr.tristiisch.olympa.core.scoreboards.ScoreboardListener;

public class OlympaCore extends OlympaPlugin {

	public static OlympaCore getInstance() {
		return (OlympaCore) instance;
	}

	@Override
	public void onDisable() {
		Bukkit.getServer().getScheduler().cancelTasks(this);
		// ScoreboardPrefix.deleteTeams();
		RedisAccess.close();
		DatabaseManager.close();
		this.sendMessage("§4" + this.getDescription().getName() + "§c by Tristiisch (" + this.getDescription().getVersion() + ") is disabled.");
	}

	@Override
	public void onEnable() {
		Set<Thread> threads = Thread.getAllStackTraces().keySet();
		threads.stream().filter(thread2 -> thread2.getName().startsWith("Craft Scheduler Thread") && thread2.isAlive()).forEach(thread2 -> thread2.interrupt());

		RedisAccess.init(this.getServer().getName());
		DatabaseManager.connect();
		this.saveDefaultConfig();

		new GroupCommand(this).register();
		new ChatCommand(this).register();
		new ReportCommand(this).register();

		new BanCommand(this).register();

		final PluginManager pluginManager = this.getServer().getPluginManager();
		pluginManager.registerEvents(new DataManagmentListener(), this);
		pluginManager.registerEvents(new GroupListener(), this);
		pluginManager.registerEvents(new ChatListener(), this);
		pluginManager.registerEvents(new ScoreboardListener(), this);
		pluginManager.registerEvents(new GuiListener(), this);
		pluginManager.registerEvents(new TestListener(), this);
		pluginManager.registerEvents(new ReportListener(), this);
		pluginManager.registerEvents(new SanctionListener(), this);

		this.sendMessage("§2" + this.getDescription().getName() + "§a by Tristiisch (" + this.getDescription().getVersion() + ") is activated.");
		// Thread.getAllStackTraces().keySet().forEach((t) ->
		// System.out.println(t.getName() + "\nIs Daemon " + t.isDaemon() + "\nIs Alive
		// " + t.isAlive()));
	}
}
