package fr.olympa.core.spigot;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.SQLException;
import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.spigotmc.SpigotConfig;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.SwearHandler;
import fr.olympa.api.afk.AfkHandler;
import fr.olympa.api.command.CommandListener;
import fr.olympa.api.command.essentials.AfkCommand;
import fr.olympa.api.command.essentials.ColorCommand;
import fr.olympa.api.command.essentials.EcseeCommand;
import fr.olympa.api.command.essentials.FlyCommand;
import fr.olympa.api.command.essentials.GamemodeCommand;
import fr.olympa.api.command.essentials.InvseeCommand;
import fr.olympa.api.command.essentials.PingCommand;
import fr.olympa.api.command.essentials.tp.TpCommand;
import fr.olympa.api.config.CustomConfig;
import fr.olympa.api.customevents.SpigotConfigReloadEvent;
import fr.olympa.api.frame.ImageFrameManager;
import fr.olympa.api.gui.Inventories;
import fr.olympa.api.holograms.HologramsCommand;
import fr.olympa.api.holograms.HologramsManager;
import fr.olympa.api.hook.IProtocolSupport;
import fr.olympa.api.permission.OlympaAPIPermissions;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.plugin.OlympaSpigot;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.region.tracking.RegionManager;
import fr.olympa.api.report.ReportReason;
import fr.olympa.api.scoreboard.tab.INametagApi;
import fr.olympa.api.server.ServerStatus;
import fr.olympa.api.sql.MySQL;
import fr.olympa.api.utils.CacheStats;
import fr.olympa.api.utils.ErrorLoggerHandler;
import fr.olympa.api.utils.ErrorOutputStream;
import fr.olympa.core.spigot.chat.CancerListener;
import fr.olympa.core.spigot.chat.ChatCommand;
import fr.olympa.core.spigot.chat.ChatListener;
import fr.olympa.core.spigot.commands.ConfigCommand;
import fr.olympa.core.spigot.commands.GenderCommand;
import fr.olympa.core.spigot.commands.NewSpigotCommand;
import fr.olympa.core.spigot.commands.PermissionCommand;
import fr.olympa.core.spigot.commands.RestartCommand;
import fr.olympa.core.spigot.commands.ToggleErrors;
import fr.olympa.core.spigot.commands.TpsCommand;
import fr.olympa.core.spigot.commands.UtilsCommand;
import fr.olympa.core.spigot.datamanagment.DataManagmentListener;
import fr.olympa.core.spigot.datamanagment.OnLoadListener;
import fr.olympa.core.spigot.groups.GroupCommand;
import fr.olympa.core.spigot.groups.GroupListener;
import fr.olympa.core.spigot.groups.StaffCommand;
import fr.olympa.core.spigot.protocolsupport.VersionHandler;
import fr.olympa.core.spigot.protocolsupport.ViaVersionHook;
import fr.olympa.core.spigot.redis.RedisSpigotSend;
import fr.olympa.core.spigot.report.commands.ReportCommand;
import fr.olympa.core.spigot.report.connections.ReportMySQL;
import fr.olympa.core.spigot.scoreboards.NametagManager;
import fr.olympa.core.spigot.scoreboards.ScoreboardTeamListener;
import fr.olympa.core.spigot.scoreboards.api.NametagAPI;
import fr.olympa.core.spigot.security.AntiWD;
import fr.olympa.core.spigot.security.HelpCommand;
import fr.olympa.core.spigot.security.PluginCommand;
import fr.olympa.core.spigot.status.SetStatusCommand;
import fr.olympa.core.spigot.status.StatusMotdListener;

public class OlympaCore extends OlympaSpigot implements LinkSpigotBungee, Listener {

	private static OlympaCore instance = null;

	public static OlympaCore getInstance() {
		return instance;
	}

	private SwearHandler swearHandler;
	private RegionManager regionManager;
	private HologramsManager hologramsManager;
	private ImageFrameManager imageFrameManager;
	private VersionHandler versionHandler;
	private AfkHandler afkHandler;
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
		return versionHandler.getViaVersion();
	}

	private INametagApi nameTagApi;
	private long lastModifiedTime;

	public long getLastModifiedTime() {
		return lastModifiedTime;
	}

	@Override
	public INametagApi getNameTagApi() {
		return nameTagApi;
	}

	@Override
	public IProtocolSupport getProtocolSupport() {
		return versionHandler.getProtocolSupport();
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
	public AfkHandler getAfkHandler() {
		return afkHandler;
	}

	@Override
	public void launchAsync(Runnable run) {
		getTask().runTaskAsynchronously(run);
	}

	@Override
	public void onDisable() {
		setStatus(ServerStatus.CLOSE);
		hologramsManager.unload();
		super.onDisable();
		sendMessage("§4" + getDescription().getName() + "§c (" + getDescription().getVersion() + ") est désactivé.");
		RedisSpigotSend.errorsEnabled = false;
	}

	@Override
	public void onLoad() {
		super.onLoad();
		instance = this;
		SpigotConfig.sendNamespaced = false;
	}

	@Override
	public void onEnable() {
		LinkSpigotBungee.Provider.link = this;
		PluginManager pluginManager = getServer().getPluginManager();
		pluginManager.registerEvents(new OnLoadListener(), this);

		OlympaPermission.registerPermissions(OlympaAPIPermissions.class);
		OlympaPermission.registerPermissions(OlympaCorePermissions.class);
		CacheStats.addDebugMap("PERMISSION", OlympaPermission.permissions);
		ReportReason.registerReason(ReportReason.class);
		super.onEnable();

		RedisSpigotSend.errorsEnabled = true;
		System.setErr(new PrintStream(new ErrorOutputStream(System.err, RedisSpigotSend::sendError, run -> getServer().getScheduler().runTaskLater(this, run, 20))));
		ErrorLoggerHandler errorHandler = new ErrorLoggerHandler(RedisSpigotSend::sendError);
		for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
			plugin.getLogger().addHandler(errorHandler);
			sendMessage("Hooked error stream handler into §6%s§e's logger!", plugin.getName());
		}
		Bukkit.getLogger().addHandler(errorHandler);
		sendMessage("Hooked error stream handler into §6server§e logger!");
		sendMessage("§6" + getDescription().getName() + "§e (" + getDescription().getVersion() + ") est chargé.");

		swearHandler = new SwearHandler(getConfig().getStringList("chat.insult"));
		imageFrameManager = new ImageFrameManager(this, "maps.yml", "images");
		try {
			AccountProvider.init(new MySQL(database));
		} catch (SQLException ex) {
			sendMessage("§cUne erreur est survenue lors du chargement du MySQL. Arrêt du plugin.");
			ex.printStackTrace();
			setEnabled(false);
			return;
		}
		new ReportMySQL(database);

		/*TestCommand test = new TestCommand(this);
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
		}*/

		//		pluginManager.registerEvents(new TestListener(), this);

		nameTagApi = new NametagAPI(new NametagManager());
		nameTagApi.addNametagHandler(EventPriority.LOW, (nametag, player, to) -> nametag.appendPrefix(player.getGroupPrefix()));
		((NametagAPI) nameTagApi).testCompat();

		pluginManager.registerEvents(this, this);
		pluginManager.registerEvents(new DataManagmentListener(), this);
		pluginManager.registerEvents(new GroupListener(), this);
		pluginManager.registerEvents(new CancerListener(), this);
		pluginManager.registerEvents(new Inventories(), this);
		pluginManager.registerEvents(new StatusMotdListener(), this);
		pluginManager.registerEvents(new ChatListener(), this);
		pluginManager.registerEvents(new CommandListener(), this);
		pluginManager.registerEvents(new StatusMotdListener(), this);
		pluginManager.registerEvents(new ScoreboardTeamListener(), this);
		pluginManager.registerEvents(regionManager = new RegionManager(), this);
		pluginManager.registerEvents(afkHandler = new AfkHandler(), this);

		try {
			pluginManager.registerEvents(hologramsManager = new HologramsManager(new File(getDataFolder(), "holograms.yml")), this);
		} catch (IOException | ReflectiveOperationException e) {
			getLogger().severe("Une erreur est survenue lors du chargement des hologrammes.");
			e.printStackTrace();
		}

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
		new GamemodeCommand(this).register().registerPreProcess();
		new InvseeCommand(this).register();
		new EcseeCommand(this).register();
		new FlyCommand(this).register();
		new AfkCommand(this).register();
		new ConfigCommand(this).register();
		new PermissionCommand(this).register();
		new PingCommand(this).register();
		new HologramsCommand(hologramsManager).register();
		new TpCommand(this).register();
		new ColorCommand(this).register();
		new ToggleErrors(this).register();
		new StaffCommand(this).register();
		new NewSpigotCommand(this).register().registerPreProcess();

		new AntiWD(this);
		getTask().runTask(() -> versionHandler = new VersionHandler(this));
		sendMessage("§2" + getDescription().getName() + "§a (" + getDescription().getVersion() + ") est activé.");

		try {
			File file = new File(OlympaCore.class.getProtectionDomain().getCodeSource().getLocation().getPath());
			BasicFileAttributes attr;
			attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
			lastModifiedTime = attr.lastModifiedTime().toMillis() / 1000L;
		} catch (Exception e) {
			e.printStackTrace();
		}
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			Collection<? extends Player> players = Bukkit.getOnlinePlayers();
			players.forEach(p -> {
				AccountProvider account = new AccountProvider(p.getUniqueId());
				OlympaPlayer olympaPlayer = account.getFromCache();
				if (olympaPlayer != null) {
					//MySQL.savePlayerPluginDatas(olympaPlayer);
					account.saveToRedis(olympaPlayer);
					account.removeFromCache();
				}
				System.out.println("Succes save " + p.getName());
			});
		}));
	}

	@EventHandler
	public void onSpigotConfigReload(SpigotConfigReloadEvent event) {
		CustomConfig newConfig = event.getConfig();
		String newFileName = newConfig.getFileName();
		if (newFileName.equals(getConfig().getFileName()))
			swearHandler = new SwearHandler(getConfig().getStringList("chat.insult"));
		else if (newFileName.equals(imageFrameManager.getFileName()))
			imageFrameManager.loadMaps(newConfig);
		/*else if (newFileName.equals(hologramsManager.getFile().getName()))
			try {
				hologramsManager = new HologramsManager(new File(getDataFolder(), "holograms.yml"));
			}catch (IOException | ReflectiveOperationException e) {
				e.printStackTrace();
			}*/
	}

	@Override
	public boolean isSpigot() {
		return true;
	}
}
