package fr.olympa.core.spigot;

import org.bukkit.plugin.PluginManager;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.gui.Inventories;
import fr.olympa.api.maintenance.MaintenanceStatus;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.plugin.OlympaSpigot;
import fr.olympa.api.provider.RedisAccess;
import fr.olympa.api.sql.MySQL;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.spigot.chat.ChatCommand;
import fr.olympa.core.spigot.chat.ChatListener;
import fr.olympa.core.spigot.datamanagment.listeners.DataManagmentListener;
import fr.olympa.core.spigot.groups.GroupCommand;
import fr.olympa.core.spigot.groups.GroupListener;
import fr.olympa.core.spigot.report.commands.ReportCommand;
import fr.olympa.core.spigot.scoreboards.ScoreboardListener;
import fr.olympa.core.spigot.security.AntiWD;
import fr.olympa.core.spigot.status.SetStatusCommand;
import fr.olympa.core.spigot.status.StatusMotdListener;
import redis.clients.jedis.Jedis;

public class OlympaCore extends OlympaSpigot implements LinkSpigotBungee {

	private static OlympaCore instance = null;

	public static OlympaCore getInstance() {
		return instance;
	}

	@Override
	public void launchAsync(Runnable run) {
		this.getTask().runTaskAsynchronously(run);
	}

	@Override
	public void onDisable() {
		// ScoreboardPrefix.deleteTeams();
		RedisAccess.close();
		super.onDisable();
		this.sendMessage("§4" + this.getDescription().getName() + "§c (" + this.getDescription().getVersion() + ") est désativer.");
	}

	@Override
	public void onEnable() {
		instance = this;
		LinkSpigotBungee.Provider.link = this;

		this.status = MaintenanceStatus.DEV;
		OlympaPermission.registerPermissions(OlympaCorePermissions.class);
		Utils.registerConfigurationSerializable();
		super.onEnable();

		new MySQL(this.database);
		Jedis jedis = RedisAccess.init(this.getServer().getName()).connect();
		if (jedis.isConnected()) {
			this.sendMessage("&aConnexion à &2Redis&a établie.");
		} else {
			this.sendMessage("&cConnexion à &4Redis&c impossible.");
		}
		//this.getTask().runTaskAsynchronously("redis1", () -> jedis.subscribe(new OlympaPlayerSpigotListener(), "OlympaPlayer"));
		//this.getTask().runTaskAsynchronously("redis2", () -> jedis.subscribe(new OlympaPlayerReceiveListener(), "OlympaPlayerReceive"));

		new GroupCommand(this).register();
		new ChatCommand(this).register();
		new ReportCommand(this).register();
		new SetStatusCommand(this).register();

		PluginManager pluginManager = this.getServer().getPluginManager();
		pluginManager.registerEvents(new DataManagmentListener(), this);
		pluginManager.registerEvents(new GroupListener(), this);
		pluginManager.registerEvents(new ChatListener(), this);
		pluginManager.registerEvents(new ScoreboardListener(), this);
		pluginManager.registerEvents(new TestListener(), this);
		pluginManager.registerEvents(new Inventories(), this);
		pluginManager.registerEvents(new StatusMotdListener(), this);
		
		new AntiWD(this);

		this.sendMessage("§2" + this.getDescription().getName() + "§a (" + this.getDescription().getVersion() + ") est activé.");
	}

}
