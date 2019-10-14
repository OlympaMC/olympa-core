package fr.tristiisch.olympa.api.plugin;

import org.bukkit.plugin.java.JavaPlugin;

import fr.tristiisch.olympa.api.config.CustomConfig;
import fr.tristiisch.olympa.api.task.TaskManager;
import fr.tristiisch.olympa.api.utils.SpigotUtils;

public abstract class OlympaPlugin extends JavaPlugin {

	protected static OlympaPlugin instance;
	private TaskManager task;
	private CustomConfig config;

	public OlympaPlugin() {
		instance = this;
		this.task = new TaskManager(this);
		this.config = new CustomConfig(this, "config");
	}

	@Override
	public CustomConfig getConfig() {
		return this.config;
	}

	private String getPrefixConsole() {
		return "&f[&6" + this.getDescription().getName() + "&f] &e";
	}

	public TaskManager getTask() {
		return this.task;
	}

	public void sendMessage(final String message) {
		this.getServer().getConsoleSender().sendMessage(SpigotUtils.color(this.getPrefixConsole() + message));
	}
}
