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
import fr.olympa.core.spigot.report.connections.ReportMySQL;
import fr.olympa.core.spigot.scoreboards.ScoreboardListener;
import fr.olympa.core.spigot.security.AntiWD;
import fr.olympa.core.spigot.security.HelpCommand;
import fr.olympa.core.spigot.security.PluginCommand;
import fr.olympa.core.spigot.status.SetStatusCommand;
import fr.olympa.core.spigot.status.StatusMotdListener;
import fr.olympa.core.spigot.tps.TpsCommand;
import redis.clients.jedis.Jedis;

public class OlympaCore extends OlympaSpigot implements LinkSpigotBungee {

	private static OlympaCore instance = null;

	public static OlympaCore getInstance() {
		return instance;
	}

	@Override
	public void launchAsync(Runnable run) {
		getTask().runTaskAsynchronously(run);
	}

	@Override
	public void onDisable() {
		// ScoreboardPrefix.deleteTeams();
		RedisAccess.close();
		super.onDisable();
		sendMessage("§4" + getDescription().getName() + "§c (" + getDescription().getVersion() + ") est désactiver.");
	}

	@Override
	public void onEnable() {
		instance = this;
		LinkSpigotBungee.Provider.link = this;

		status = MaintenanceStatus.DEV;
		OlympaPermission.registerPermissions(OlympaCorePermissions.class);
		Utils.registerConfigurationSerializable();
		super.onEnable();

		new MySQL(database);
		new ReportMySQL(database);
		RedisAccess redis = RedisAccess.init(getServer().getName());
		Jedis jedis = redis.connect();
		redis.addListenerSpigot(this, redis.connect());

		if (jedis.isConnected()) {
			sendMessage("&aConnexion à &2Redis&a établie.");
		} else {
			sendMessage("&cConnexion à &4Redis&c impossible.");
		}
		// this.getTask().runTaskAsynchronously("redis1", () -> jedis.subscribe(new
		// OlympaPlayerSpigotListener(), "OlympaPlayer"));
		// this.getTask().runTaskAsynchronously("redis2", () -> jedis.subscribe(new
		// OlympaPlayerReceiveListener(), "OlympaPlayerReceive"));

		new GroupCommand(this).register();
		new ChatCommand(this).register();
		new ReportCommand(this).register();
		new SetStatusCommand(this).register();
		new PluginCommand(this).register();
		new HelpCommand(this).register();
		new TpsCommand(this).register();
		// new PasswdCommand(this).register();

		PluginManager pluginManager = getServer().getPluginManager();
		pluginManager.registerEvents(new DataManagmentListener(), this);
		pluginManager.registerEvents(new GroupListener(), this);
		pluginManager.registerEvents(new ChatListener(), this);
		pluginManager.registerEvents(new ScoreboardListener(), this);
		pluginManager.registerEvents(new TestListener(), this);
		pluginManager.registerEvents(new Inventories(), this);
		pluginManager.registerEvents(new StatusMotdListener(), this);

		new AntiWD(this);

		sendMessage("§2" + getDescription().getName() + "§a (" + getDescription().getVersion() + ") est activé.");
	}

}
