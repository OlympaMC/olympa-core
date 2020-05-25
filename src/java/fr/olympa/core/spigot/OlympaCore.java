package fr.olympa.core.spigot;

import org.bukkit.plugin.PluginManager;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.command.CommandListener;
import fr.olympa.api.gui.Inventories;
import fr.olympa.api.hook.ProtocolAction;
import fr.olympa.api.maintenance.MaintenanceStatus;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.plugin.OlympaSpigot;
import fr.olympa.api.provider.RedisAccess;
import fr.olympa.api.region.tracking.RegionManager;
import fr.olympa.api.scoreboard.tab.INametagApi;
import fr.olympa.api.sql.MySQL;
import fr.olympa.core.spigot.chat.CancerListener;
import fr.olympa.core.spigot.chat.ChatCommand;
import fr.olympa.core.spigot.chat.ChatListener;
import fr.olympa.core.spigot.chat.SwearHandler;
import fr.olympa.core.spigot.commands.GenderCommand;
import fr.olympa.core.spigot.datamanagment.listeners.DataManagmentListener;
import fr.olympa.core.spigot.groups.GroupCommand;
import fr.olympa.core.spigot.groups.GroupListener;
import fr.olympa.core.spigot.protocolsupport.ProtocolSupportHook;
import fr.olympa.core.spigot.redis.RedisSpigotSend;
import fr.olympa.core.spigot.report.commands.ReportCommand;
import fr.olympa.core.spigot.report.connections.ReportMySQL;
import fr.olympa.core.spigot.scoreboards.NameTagListener;
import fr.olympa.core.spigot.scoreboards.NametagManager;
import fr.olympa.core.spigot.scoreboards.ScoreboardTeamListener;
import fr.olympa.core.spigot.scoreboards.api.NametagAPI;
import fr.olympa.core.spigot.security.AntiWD;
import fr.olympa.core.spigot.security.HelpCommand;
import fr.olympa.core.spigot.security.PluginCommand;
import fr.olympa.core.spigot.status.SetStatusCommand;
import fr.olympa.core.spigot.status.StatusMotdListener;
import fr.olympa.core.spigot.tps.TpsCommand;

public class OlympaCore extends OlympaSpigot implements LinkSpigotBungee {

	private static OlympaCore instance = null;

	public static OlympaCore getInstance() {
		return instance;
	}

	private SwearHandler swearHandler;
	private RegionManager regionManager = new RegionManager();
	private ProtocolAction protocolSupportHook;
	private INametagApi nameTagApi;

	@Override
	public INametagApi getNameTagApi() {
		return nameTagApi;
	}

	@Override
	public ProtocolAction getProtocolSupport() {
		return protocolSupportHook;
	}

	@Override
	public RegionManager getRegionManager() {
		return regionManager;
	}

	public SwearHandler getSwearHandler() {
		return swearHandler;
	}

	@Override
	public void launchAsync(Runnable run) {
		getTask().runTaskAsynchronously(run);
	}

	@Override
	public void onDisable() {
		RedisSpigotSend.sendShutdown();
		RedisAccess.close();
		status = MaintenanceStatus.CLOSE;
		nameTagApi.reset();
		super.onDisable();
		sendMessage("§4" + getDescription().getName() + "§c (" + getDescription().getVersion() + ") est désactivé.");
	}

	@Override
	public void onEnable() {
		instance = this;
		LinkSpigotBungee.Provider.link = this;

		status = MaintenanceStatus.DEV;
		OlympaPermission.registerPermissions(OlympaCorePermissions.class);
		super.onEnable();

		swearHandler = new SwearHandler(getConfig().getStringList("chat.insult"));
		new MySQL(database);
		new ReportMySQL(database);

		new GroupCommand(this).register();
		new ChatCommand(this).register();
		new ReportCommand(this).register();
		new SetStatusCommand(this).register();
		new PluginCommand(this).register();
		new HelpCommand(this).register();
		new TpsCommand(this).registerPreProcess();
		new UtilsCommand(this).register();
		new GenderCommand(this).register();
		// new PasswdCommand(this).register();

		PluginManager pluginManager = getServer().getPluginManager();
		pluginManager.registerEvents(new DataManagmentListener(), this);
		pluginManager.registerEvents(new GroupListener(), this);
		pluginManager.registerEvents(new CancerListener(), this);
		pluginManager.registerEvents(new NameTagListener(), this);
		pluginManager.registerEvents(new TestListener(), this);
		pluginManager.registerEvents(new Inventories(), this);
		pluginManager.registerEvents(new StatusMotdListener(), this);
		pluginManager.registerEvents(new ChatListener(), this);
		pluginManager.registerEvents(regionManager, this);
		pluginManager.registerEvents(new CommandListener(), this);
		pluginManager.registerEvents(new StatusMotdListener(), this);
		pluginManager.registerEvents(new NameTagListener(), this);
		pluginManager.registerEvents(new ScoreboardTeamListener(), this);
		nameTagApi = new NametagAPI(new NametagManager());
		((NametagAPI) nameTagApi).testCompat();

		new AntiWD(this);
		if (getServer().getPluginManager().isPluginEnabled("ProtocolSupport")) {
			protocolSupportHook = new ProtocolSupportHook(this);
		}

		sendMessage("§2" + getDescription().getName() + "§a (" + getDescription().getVersion() + ") est activé.");
	}

}
