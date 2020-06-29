package fr.olympa.core.spigot;

import java.io.File;
import java.io.IOException;

import org.bukkit.plugin.PluginManager;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.SwearHandler;
import fr.olympa.api.command.CommandListener;
import fr.olympa.api.frame.ImageFrameManager;
import fr.olympa.api.gui.Inventories;
import fr.olympa.api.holograms.HologramsManager;
import fr.olympa.api.hook.ProtocolAction;
import fr.olympa.api.permission.OlympaAPIPermissions;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.plugin.OlympaSpigot;
import fr.olympa.api.provider.RedisAccess;
import fr.olympa.api.region.tracking.RegionManager;
import fr.olympa.api.scoreboard.tab.INametagApi;
import fr.olympa.api.server.ServerStatus;
import fr.olympa.api.sql.MySQL;
import fr.olympa.core.spigot.chat.CancerListener;
import fr.olympa.core.spigot.chat.ChatCommand;
import fr.olympa.core.spigot.chat.ChatListener;
import fr.olympa.core.spigot.commands.GenderCommand;
import fr.olympa.core.spigot.commands.RestartCommand;
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
	private RegionManager regionManager;
	private HologramsManager hologramsManager;
	private ImageFrameManager imageFrameManager;
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

	@Override
	public HologramsManager getHologramsManager() {
		return hologramsManager;
	}

	@Override
	public ImageFrameManager getImageFrameManager() {
		return imageFrameManager;
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
		status = ServerStatus.CLOSE;
		nameTagApi.reset();
		hologramsManager.unload();
		super.onDisable();
		sendMessage("§4" + getDescription().getName() + "§c (" + getDescription().getVersion() + ") est désactivé.");
	}

	@Override
	public void onEnable() {
		instance = this;
		LinkSpigotBungee.Provider.link = this;
		
		OlympaPermission.registerPermissions(OlympaAPIPermissions.class);
		OlympaPermission.registerPermissions(OlympaCorePermissions.class);
		super.onEnable();
		// Force Dev Status (le status dans la config est ignoré)
		status = ServerStatus.DEV;
		
		try {
			hologramsManager = new HologramsManager(new File(getDataFolder(), "holograms.yml"));
		} catch (IOException e) {
			getLogger().severe("Une erreur est survenue lors du chargement des hologrammes.");
			e.printStackTrace();
		}
		
		swearHandler = new SwearHandler(getConfig().getStringList("chat.insult"));
		imageFrameManager = new ImageFrameManager(this, "maps.yml", "images");
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
		new RestartCommand(this).registerPreProcess();
		// new PasswdCommand(this).register();
		
		PluginManager pluginManager = getServer().getPluginManager();
		pluginManager.registerEvents(new DataManagmentListener(), this);
		pluginManager.registerEvents(new GroupListener(), this);
		pluginManager.registerEvents(new CancerListener(), this);
		pluginManager.registerEvents(new TestListener(), this);
		pluginManager.registerEvents(new Inventories(), this);
		pluginManager.registerEvents(new StatusMotdListener(), this);
		pluginManager.registerEvents(new ChatListener(), this);
		pluginManager.registerEvents(regionManager = new RegionManager(), this);
		pluginManager.registerEvents(new CommandListener(), this);
		pluginManager.registerEvents(new StatusMotdListener(), this);
		pluginManager.registerEvents(new NameTagListener(), this);
		pluginManager.registerEvents(new ScoreboardTeamListener(), this);
		nameTagApi = new NametagAPI(new NametagManager());
		((NametagAPI) nameTagApi).testCompat();
		
		new AntiWD(this);
		if (getServer().getPluginManager().isPluginEnabled("ProtocolSupport"))
			protocolSupportHook = new ProtocolSupportHook(this);
		
		sendMessage("§2" + getDescription().getName() + "§a (" + getDescription().getVersion() + ") est activé.");
	}
}
