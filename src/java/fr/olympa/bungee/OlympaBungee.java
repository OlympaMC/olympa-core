package fr.olympa.bungee;

import fr.olympa.api.provider.RedisAccess;
import fr.olympa.api.utils.SpigotUtils;
import fr.olympa.bungee.maintenance.ConnectionListener;
import fr.olympa.bungee.maintenance.MaintenanceCommand;
import fr.olympa.bungee.maintenance.MaintenanceListener;
import fr.olympa.bungee.motd.MotdListener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;

public class OlympaBungee extends Plugin {

	private static OlympaBungee INSTANCE;

	public static OlympaBungee getInstance() {
		return INSTANCE;
	}

	private String getPrefixConsole() {
		return "&f[&6" + this.getDescription().getName() + "&f] &e";
	}

	@Override
	public void onDisable() {
		this.sendMessage("§4" + this.getDescription().getName() + "§c (" + this.getDescription().getVersion() + ") is disabled.");
	}

	@Override
	public void onEnable() {
		INSTANCE = this;

		RedisAccess.init("bungee");

		PluginManager pluginManager = this.getProxy().getPluginManager();
		pluginManager.registerListener(this, new MotdListener());
		pluginManager.registerListener(this, new MaintenanceListener());
		pluginManager.registerListener(this, new ConnectionListener());

		new MaintenanceCommand(this).register();

		this.sendMessage("§2" + this.getDescription().getName() + "§a (" + this.getDescription().getVersion() + ") is activated.");
	}

	@SuppressWarnings("deprecation")
	public void sendMessage(final String message) {
		this.getProxy().getConsole().sendMessage(SpigotUtils.color(this.getPrefixConsole() + message));
	}
}
