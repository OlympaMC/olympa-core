package fr.olympa.core.spigot;

import org.bukkit.plugin.PluginManager;

import fr.olympa.api.gui.Inventories;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.plugin.OlympaPlugin;
import fr.olympa.api.provider.RedisAccess;
import fr.olympa.core.spigot.chat.ChatCommand;
import fr.olympa.core.spigot.chat.ChatListener;
import fr.olympa.core.spigot.datamanagment.listeners.DataManagmentListener;
import fr.olympa.core.spigot.groups.GroupCommand;
import fr.olympa.core.spigot.groups.GroupListener;
import fr.olympa.core.spigot.report.commands.ReportCommand;
import fr.olympa.core.spigot.scoreboards.ScoreboardListener;

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
		this.sendMessage("§4" + this.getDescription().getName() + "§c (" + this.getDescription().getVersion() + ") est désativer.");
	}

	@Override
	public void onEnable() {
		instance = this;

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
		this.sendMessage("§2" + this.getDescription().getName() + "§a (" + this.getDescription().getVersion() + ") est activé.");
	}
}
