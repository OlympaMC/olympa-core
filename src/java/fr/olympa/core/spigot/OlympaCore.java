package fr.olympa.core.spigot;

import java.io.File;
import java.io.IOException;

import org.bukkit.plugin.PluginManager;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.SwearHandler;
import fr.olympa.api.command.CommandListener;
import fr.olympa.api.frame.ImageFrameManager;
import fr.olympa.api.gui.Inventories;
import fr.olympa.api.holograms.HologramsManager;
import fr.olympa.api.hook.IProtocolSupport;
import fr.olympa.api.permission.OlympaAPIPermissions;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.plugin.OlympaSpigot;
import fr.olympa.api.region.tracking.RegionManager;
import fr.olympa.api.scoreboard.tab.INametagApi;
import fr.olympa.api.server.ServerStatus;
import fr.olympa.api.sql.MySQL;
import fr.olympa.api.utils.ErrorOutputStream;
import fr.olympa.api.utils.spigot.ProtocolAPI;
import fr.olympa.core.spigot.chat.CancerListener;
import fr.olympa.core.spigot.chat.ChatCommand;
import fr.olympa.core.spigot.chat.ChatListener;
import fr.olympa.core.spigot.commands.AfkCommand;
import fr.olympa.core.spigot.commands.ConfigCommand;
import fr.olympa.core.spigot.commands.FlyCommand;
import fr.olympa.core.spigot.commands.GamemodeCommand;
import fr.olympa.core.spigot.commands.GenderCommand;
import fr.olympa.core.spigot.commands.PermissionCommand;
import fr.olympa.core.spigot.commands.PingCommand;
import fr.olympa.core.spigot.commands.RestartCommand;
import fr.olympa.core.spigot.commands.TpsCommand;
import fr.olympa.core.spigot.datamanagment.DataManagmentListener;
import fr.olympa.core.spigot.datamanagment.OnLoadListener;
import fr.olympa.core.spigot.groups.GroupCommand;
import fr.olympa.core.spigot.groups.GroupListener;
import fr.olympa.core.spigot.protocolsupport.ProtocolSupportHook;
import fr.olympa.core.spigot.protocolsupport.ViaVersionHook;
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
import me.lucko.commodore.Commodore;
import me.lucko.commodore.CommodoreProvider;

public class OlympaCore extends OlympaSpigot implements LinkSpigotBungee {

	private static OlympaCore instance = null;

	public static OlympaCore getInstance() {
		return instance;
	}

	private SwearHandler swearHandler;
	private RegionManager regionManager;
	private HologramsManager hologramsManager;
	private ImageFrameManager imageFrameManager;
	private IProtocolSupport protocolSupportHook;
	private ViaVersionHook viaVersionHook;
	private String lastVersion = "unknown";
	private String firstVersion = "unknown";

	public String getLastVersion() {
		return lastVersion;
	}

	public void setLastVersion(String lastVersion) {
		this.lastVersion = lastVersion;
	}

	public String getFirstVersion() {
		return firstVersion;
	}

	public String getRangeVersion() {
		if (firstVersion.equals(lastVersion))
			return firstVersion;
		return firstVersion + " à " + lastVersion;
	}

	public void setFirstVersion(String firstVersion) {
		this.firstVersion = firstVersion;
	}

	public ViaVersionHook getViaVersionHook() {
		return viaVersionHook;
	}

	private INametagApi nameTagApi;

	@Override
	public INametagApi getNameTagApi() {
		return nameTagApi;
	}

	@Override
	public IProtocolSupport getProtocolSupport() {
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
		setStatus(ServerStatus.CLOSE);
		nameTagApi.reset();
		hologramsManager.unload();
		super.onDisable();
		sendMessage("§4" + getDescription().getName() + "§c (" + getDescription().getVersion() + ") est désactivé.");
	}

	@Override
	public void onLoad() {
		super.onLoad();
		sendMessage("§6" + getDescription().getName() + "§e (" + getDescription().getVersion() + ") est chargé.");
		System.setErr(new ErrorOutputStream(System.err, RedisSpigotSend::sendError, run -> getServer().getScheduler().runTaskLater(instance, run, 20)));
	}

	@Override
	public void onEnable() {
		instance = this;
		LinkSpigotBungee.Provider.link = this;
		PluginManager pluginManager = getServer().getPluginManager();
		pluginManager.registerEvents(new OnLoadListener(), this);

		OlympaPermission.registerPermissions(OlympaAPIPermissions.class);
		OlympaPermission.registerPermissions(OlympaCorePermissions.class);
		super.onEnable();

		swearHandler = new SwearHandler(getConfig().getStringList("chat.insult"));
		imageFrameManager = new ImageFrameManager(this, "maps.yml", "images");
		new MySQL(database);
		new ReportMySQL(database);

		new GroupCommand(this).register();
		new ChatCommand(this).register();
		new ReportCommand(this).register();
		new SetStatusCommand(this).register();
		new PluginCommand(this).registerPreProcess();
		new HelpCommand(this).registerPreProcess();
		new TpsCommand(this).registerPreProcess();
		new UtilsCommand(this).register();
		new GenderCommand(this).register();
		new RestartCommand(this).registerPreProcess();
		GamemodeCommand gm = new GamemodeCommand(this);
		gm.register();
		gm.registerPreProcess();
		new FlyCommand(this).register();
		new AfkCommand(this).register();
		new ConfigCommand(this).register();
		new PermissionCommand(this).register();
		new PingCommand(this).register();

		TestCommand test = new TestCommand(this);
		test.register();

		if (CommodoreProvider.isSupported()) {
			Commodore commodore = CommodoreProvider.getCommodore(this);
			commodore.register(test.reflectCommand, LiteralArgumentBuilder.literal("test1")
					.then(LiteralArgumentBuilder.literal("set")
							.then(LiteralArgumentBuilder.literal("day"))
							.then(LiteralArgumentBuilder.literal("noon"))
							.then(LiteralArgumentBuilder.literal("night"))
							.then(LiteralArgumentBuilder.literal("midnight"))
							.then(RequiredArgumentBuilder.argument("time", IntegerArgumentType.integer())))
					.then(LiteralArgumentBuilder.literal("add")
							.then(RequiredArgumentBuilder.argument("time", DoubleArgumentType.doubleArg(-10, 10))))
					.then(LiteralArgumentBuilder.literal("query")
							.then(LiteralArgumentBuilder.literal("daytime"))
							.then(LiteralArgumentBuilder.literal("gametime"))
							.then(LiteralArgumentBuilder.literal("day"))
					//.then(RequiredArgumentBuilder.argument("uuid", UUIDArgumentType.uuid()))
					// Could not serialize fr.olympa.api.brigadier.UUIDArgumentType@28aefe5e (class fr.olympa.api.brigadier.UUIDArgumentType) - will not be sent to client!
					)
					.build());
		}

		pluginManager.registerEvents(new TestListener(), this);

		pluginManager.registerEvents(new DataManagmentListener(), this);
		pluginManager.registerEvents(new GroupListener(), this);
		pluginManager.registerEvents(new CancerListener(), this);
		pluginManager.registerEvents(new Inventories(), this);
		pluginManager.registerEvents(new StatusMotdListener(), this);
		pluginManager.registerEvents(new ChatListener(), this);
		pluginManager.registerEvents(new CommandListener(), this);
		pluginManager.registerEvents(new StatusMotdListener(), this);
		pluginManager.registerEvents(new NameTagListener(), this);
		pluginManager.registerEvents(new ScoreboardTeamListener(), this);
		pluginManager.registerEvents(regionManager = new RegionManager(), this);

		try {
			pluginManager.registerEvents(hologramsManager = new HologramsManager(new File(getDataFolder(), "holograms.yml")), this);
		} catch (IOException e) {
			getLogger().severe("Une erreur est survenue lors du chargement des hologrammes.");
			e.printStackTrace();
		}

		nameTagApi = new NametagAPI(new NametagManager());
		((NametagAPI) nameTagApi).testCompat();

		new AntiWD(this);
		getTask().runTaskLater(() -> {
			if (pluginManager.isPluginEnabled("ViaVersion"))
				viaVersionHook = new ViaVersionHook(this);
			if (pluginManager.isPluginEnabled("ProtocolSupport")) {
				protocolSupportHook = new ProtocolSupportHook(this);
				String[] versions = protocolSupportHook.getRangeVersionArray();
				setFirstVersion(versions[0]);
				setLastVersion(versions[1]);
			} else
				try {
					String[] versions = ProtocolAPI.getVersionSupportedArray();
					setFirstVersion(versions[0]);
					setLastVersion(versions[1]);
				} catch (Exception e) {
					e.printStackTrace();
				}
		}, 100);
		sendMessage("§2" + getDescription().getName() + "§a (" + getDescription().getVersion() + ") est activé.");
	}

	@Override
	public boolean isSpigot() {
		return true;
	}
}
