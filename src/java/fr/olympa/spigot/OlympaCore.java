package fr.olympa.spigot;

import org.bukkit.plugin.PluginManager;

import fr.olympa.api.gui.Inventories;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.plugin.OlympaPlugin;
import fr.olympa.api.provider.RedisAccess;
import fr.olympa.spigot.core.chat.ChatCommand;
import fr.olympa.spigot.core.chat.ChatListener;
import fr.olympa.spigot.core.datamanagment.listeners.DataManagmentListener;
import fr.olympa.spigot.core.groups.GroupCommand;
import fr.olympa.spigot.core.groups.GroupListener;
import fr.olympa.spigot.core.report.commands.ReportCommand;
import fr.olympa.spigot.core.scoreboards.ScoreboardListener;

public class OlympaCore extends OlympaPlugin {

	private static OlympaCore instance;

	public static OlympaCore getInstance() {
		return instance;
	}

	@Override
	public void onDisable() {
		// ScoreboardPrefix.deleteTeams();
		RedisAccess.close();
		this.disable();
	}

	@Override
	public void onEnable() {
		OlympaPermission.registerPermissions(OlympaCorePermissions.class);
		this.enable();

		RedisAccess.init(this.getServer().getName());

		new GroupCommand(this).register();
		new ChatCommand(this).register();
		new ReportCommand(this).register();

		PluginManager pluginManager = this.getServer().getPluginManager();
		pluginManager.registerEvents(new DataManagmentListener(), this);
		pluginManager.registerEvents(new GroupListener(), this);
		pluginManager.registerEvents(new ChatListener(), this);
		pluginManager.registerEvents(new ScoreboardListener(), this);
		pluginManager.registerEvents(new TestListener(), this);
		pluginManager.registerEvents(new Inventories(), this);

		// Thread.getAllStackTraces().keySet().forEach((t) ->
		// System.out.println(t.getName() + "\nIs Daemon " + t.isDaemon() + "\nIs Alive
		// " + t.isAlive()));
	}
}
