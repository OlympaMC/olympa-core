package fr.tristiisch.olympa;

import org.bukkit.plugin.PluginManager;

import fr.tristiisch.olympa.api.plugin.OlympaPlugin;
import fr.tristiisch.olympa.api.task.TaskManager;
import fr.tristiisch.olympa.core.chat.ChatCommand;
import fr.tristiisch.olympa.core.chat.ChatListener;
import fr.tristiisch.olympa.core.datamanagment.listeners.DataManagmentListener;
import fr.tristiisch.olympa.core.datamanagment.redis.RedisAccess;
import fr.tristiisch.olympa.core.datamanagment.sql.DatabaseManager;
import fr.tristiisch.olympa.core.permission.groups.GroupCommand;
import fr.tristiisch.olympa.core.permission.groups.listener.GroupListener;
import fr.tristiisch.olympa.core.permission.groups.listener.PlayerUpdateListener;

public class OlympaCore extends OlympaPlugin {

	@Override
	public void onDisable() {
		RedisAccess.close();
		DatabaseManager.close();
		this.sendMessage("§4" + this.getDescription().getName() + "§c by Tristiisch (" + this.getDescription().getVersion() + ") is disabled.");
	}

	@Override
	public void onEnable() {
		new TaskManager(this);

		RedisAccess.init(this.getServer().getName());
		DatabaseManager.connect();
		this.saveDefaultConfig();

		new GroupCommand(this).register();
		new ChatCommand(this).register();

		final PluginManager pluginManager = this.getServer().getPluginManager();
		pluginManager.registerEvents(new DataManagmentListener(), this);
		pluginManager.registerEvents(new GroupListener(), this);
		pluginManager.registerEvents(new ChatListener(), this);
		pluginManager.registerEvents(new PlayerUpdateListener(), this);
		pluginManager.registerEvents(new TestListener(), this);

		this.sendMessage("§2" + this.getDescription().getName() + "§a by Tristiisch (" + this.getDescription().getVersion() + ") is activated.");
	}
}
