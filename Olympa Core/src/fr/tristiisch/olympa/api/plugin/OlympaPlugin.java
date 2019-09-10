package fr.tristiisch.olympa.api.plugin;

import org.bukkit.plugin.java.JavaPlugin;

import fr.tristiisch.olympa.api.utils.SpigotUtils;

public abstract class OlympaPlugin extends JavaPlugin {

	protected static OlympaPlugin instance;

	public static OlympaPlugin getInstance() {
		return instance;
	}

	public OlympaPlugin() {
		instance = this;
	}

	private String getPrefixConsole() {
		return "&f[&6" + this.getDescription().getName() + "&f] &e";
	}

	public void sendMessage(final String message) {
		this.getServer().getConsoleSender().sendMessage(SpigotUtils.color(this.getPrefixConsole() + message));
	}
}
