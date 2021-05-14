package fr.olympa.core.spigot;

import java.io.File;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.spigotmc.SpigotConfig;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.SwearHandler;
import fr.olympa.api.command.CommandListener;
import fr.olympa.api.command.essentials.ColorCommand;
import fr.olympa.api.command.essentials.EcseeCommand;
import fr.olympa.api.command.essentials.FlyCommand;
import fr.olympa.api.command.essentials.GamemodeCommand;
import fr.olympa.api.command.essentials.InvseeCommand;
import fr.olympa.api.command.essentials.ItemCommand;
import fr.olympa.api.command.essentials.ListCommand;
import fr.olympa.api.command.essentials.PingCommand;
import fr.olympa.api.command.essentials.tp.TpCommand;
import fr.olympa.api.config.CustomConfig;
import fr.olympa.api.customevents.SpigotConfigReloadEvent;
import fr.olympa.api.frame.ImageFrameManager;
import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.gui.Inventories;
import fr.olympa.api.holograms.HologramsManager;
import fr.olympa.api.hook.IProtocolSupport;
import fr.olympa.api.module.OlympaModule;
import fr.olympa.api.permission.OlympaAPIPermissions;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.plugin.OlympaSpigot;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.redis.RedisAccess;
import fr.olympa.api.redis.RedisChannel;
import fr.olympa.api.region.tracking.RegionManager;
import fr.olympa.api.report.ReportReason;
import fr.olympa.api.server.MonitorInfo;
import fr.olympa.api.server.ServerStatus;
import fr.olympa.api.sql.DbConnection;
import fr.olympa.api.sql.DbCredentials;
import fr.olympa.api.sql.MySQL;
import fr.olympa.api.utils.CacheStats;
import fr.olympa.api.utils.ErrorLoggerHandler;
import fr.olympa.api.utils.ErrorOutputStream;
import fr.olympa.api.utils.Utils;
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
import fr.olympa.core.spigot.module.CoreModules;
import fr.olympa.core.spigot.protocolsupport.VersionHandler;
import fr.olympa.core.spigot.protocolsupport.ViaVersionHook;
import fr.olympa.core.spigot.redis.RedisSpigotSend;
import fr.olympa.core.spigot.redis.receiver.BungeeAskPlayerServerReceiver;
import fr.olympa.core.spigot.redis.receiver.BungeeAskSomething;
import fr.olympa.core.spigot.redis.receiver.BungeeSendOlympaPlayerReceiver;
import fr.olympa.core.spigot.redis.receiver.BungeeServerInfoReceiver;
import fr.olympa.core.spigot.redis.receiver.BungeeServerNameReceiver;
import fr.olympa.core.spigot.redis.receiver.BungeeTeamspeakIdReceiver;
import fr.olympa.core.spigot.redis.receiver.SpigotCommandReceiver;
import fr.olympa.core.spigot.redis.receiver.SpigotGroupChangedReceiveReceiver;
import fr.olympa.core.spigot.redis.receiver.SpigotGroupChangedReceiver;
import fr.olympa.core.spigot.redis.receiver.SpigotOlympaPlayerReceiver;
import fr.olympa.core.spigot.redis.receiver.SpigotReceiveOlympaPlayerReceiver;
import fr.olympa.core.spigot.report.commands.ReportCommand;
import fr.olympa.core.spigot.report.connections.ReportMySQL;
import fr.olympa.core.spigot.scoreboards.api.NametagAPI;
import fr.olympa.core.spigot.security.AntiWD;
import fr.olympa.core.spigot.security.HelpCommand;
import fr.olympa.core.spigot.security.PluginCommand;
import fr.olympa.core.spigot.status.SetStatusCommand;
import fr.olympa.core.spigot.status.StatusMotdListener;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

public class OlympaCore extends OlympaSpigot implements LinkSpigotBungee, Listener {

	private static OlympaCore instance = null;

	public static OlympaCore getInstance() {
		return instance;
	}

	protected DbConnection database = null;
	private SwearHandler swearHandler;
	private VersionHandler versionHandler;
	private String lastVersion = "unknown";
	private String firstVersion = "unknown";
	private ErrorOutputStream errorOutputStream;

	public GamemodeCommand gamemodeCommand = null;

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

	public VersionHandler getVersionHandler() {
		return versionHandler;
	}

	public ViaVersionHook getViaVersionHook() {
		return versionHandler.getViaVersion();
	}

	@Override
	public Connection getDatabase() throws SQLException {
		return database.getConnection();
	}

	@Override
	public IProtocolSupport getProtocolSupport() {
		return versionHandler.getProtocolSupport();
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
		RedisSpigotSend.errorsEnabled = false;
		setStatus(ServerStatus.CLOSE);
		super.onDisable();
		OlympaModule.disableAll();
		if (database != null)
			database.close();
		sendMessage("§4" + getDescription().getName() + "§c (" + getDescription().getVersion() + ") est désactivé.");
	}

	@Override
	public void onLoad() {
		super.onLoad();
		instance = this;
		SpigotConfig.sendNamespaced = false;
	}

	@Override
	public void onEnable() {

		//TODO supprimer cette classe quand le débug sera fini !
		//getServer().getPluginManager().registerEvents(new DebugTheTwo(), this);

		LinkSpigotBungee.Provider.link = this;
		PluginManager pluginManager = getServer().getPluginManager();
		pluginManager.registerEvents(new OnLoadListener(), this);

		OlympaPermission.registerPermissions(OlympaAPIPermissions.class);
		OlympaPermission.registerPermissions(OlympaCorePermissions.class);
		new RestartCommand(this).registerPreProcess().register();
		CacheStats.addDebugMap("PERMISSION", OlympaPermission.permissions);
		ReportReason.registerReason(ReportReason.class);
		BungeeServerInfoReceiver.registerCallback(mi -> {
			lastInfo = Utils.getCurrentTimeInSeconds();
			monitorInfos.clear();
			monitorInfos.addAll(mi);
		});
		super.onEnable();
		if (config != null) {
			setupRedis();
			setupDatabase();
		}

		RedisSpigotSend.errorsEnabled = true;
		errorOutputStream = new ErrorOutputStream(System.err, RedisSpigotSend::sendError, run -> getServer().getScheduler().runTaskLater(this, run, 20));
		System.setErr(new PrintStream(errorOutputStream));
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
			sendMessage("&cUne erreur est survenue lors du chargement du MySQL. Arrêt du plugin.");
			ex.printStackTrace();
			setEnabled(false);
			return;
		}
		new ReportMySQL(database);

		try {
			new CoreModules();
			OlympaModule.enableAll();
			((NametagAPI) nameTagApi).testCompat();
		} catch (Exception | NoSuchMethodError e) {
			sendMessage("&cUne erreur est survenue lors du chargement des modules.");
			e.printStackTrace();
		}
		try {
			new HologramsManager(this, new File(getDataFolder(), "holograms.yml"));
		} catch (NullPointerException | ReflectiveOperationException e) {
			getLogger().severe("Une erreur est survenue lors du chargement des hologrammes.");
			e.printStackTrace();
		}

		pluginManager.registerEvents(this, this);
		pluginManager.registerEvents(new DataManagmentListener(), this);
		pluginManager.registerEvents(new GroupListener(), this);
		pluginManager.registerEvents(new CancerListener(), this);
		pluginManager.registerEvents(new Inventories(), this);
		pluginManager.registerEvents(new StatusMotdListener(), this);
		pluginManager.registerEvents(new ChatListener(), this);
		pluginManager.registerEvents(new CommandListener(), this);
		pluginManager.registerEvents(regionManager = new RegionManager(), this);

		new GroupCommand(this).register();
		new ChatCommand(this).register();
		new ReportCommand(this).register();
		new SetStatusCommand(this).register();
		new PluginCommand(this).registerPreProcess();
		new HelpCommand(this).register().registerPreProcess();
		new TpsCommand(this).registerPreProcess();
		new UtilsCommand(this).register();
		new GenderCommand(this).register();
		gamemodeCommand = (GamemodeCommand) new GamemodeCommand(this).register().registerPreProcess();
		new InvseeCommand(this).register();
		new EcseeCommand(this).register();
		new FlyCommand(this).register();
		new ConfigCommand(this).register();
		new PermissionCommand(this).register();
		new PingCommand(this).register();
		//		new HologramsCommand(hologramsManager).register();
		new TpCommand(this).register();
		new ColorCommand(this).register();
		new ToggleErrors(this).register();
		new StaffCommand(this).register();
		new ItemCommand(this).register();
		new NewSpigotCommand(this).register().registerPreProcess();
		new ListCommand(this).register().registerPreProcess();

		new AntiWD(this);
		getTask().runTask(() -> versionHandler = new VersionHandler(this));
		OlympaGroup defaultGroup = OlympaGroup.PLAYER;
		defaultGroup.setRuntimePermission("minecraft.command.help", false);
		defaultGroup.setRuntimePermission("minecraft.command.me", false);
		defaultGroup.setRuntimePermission("minecraft.command.msg", false);
		defaultGroup.setRuntimePermission("bukkit.command.version", false);
		defaultGroup.setRuntimePermission("bukkit.command.plugins", false);
		defaultGroup.setRuntimePermission("bukkit.command.help", false);
		sendMessage("§2" + getDescription().getName() + "§a (" + getDescription().getVersion() + ") est activé.");
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

	private void setupRedis(int... is) {
		int i1 = 0;
		if (is != null && is.length != 0)
			i1 = is[0] + 1;
		int i = i1;
		RedisAccess redisAccess = RedisAccess.init(getConfig());
		redisAccess.connect();
		if (redisAccess.isConnected()) {
			registerRedisSub(redisAccess.getConnection(), new BungeeServerNameReceiver(), RedisChannel.BUNGEE_ASK_SEND_SERVERNAME.name());
			registerRedisSub(redisAccess.connect(), new SpigotOlympaPlayerReceiver(), RedisChannel.BUNGEE_ASK_SEND_OLYMPAPLAYER.name());
			registerRedisSub(redisAccess.connect(), new SpigotReceiveOlympaPlayerReceiver(), RedisChannel.SPIGOT_SEND_OLYMPAPLAYER.name());
			registerRedisSub(redisAccess.connect(), new BungeeSendOlympaPlayerReceiver(), RedisChannel.BUNGEE_SEND_OLYMPAPLAYER.name());
			registerRedisSub(redisAccess.connect(), new SpigotGroupChangedReceiver(), RedisChannel.SPIGOT_CHANGE_GROUP.name());
			registerRedisSub(redisAccess.connect(), new SpigotGroupChangedReceiveReceiver(), RedisChannel.SPIGOT_CHANGE_GROUP_RECEIVE.name());
			registerRedisSub(redisAccess.connect(), new BungeeAskPlayerServerReceiver(), RedisChannel.BUNGEE_SEND_PLAYERSERVER.name());
			registerRedisSub(redisAccess.connect(), new SpigotCommandReceiver(), RedisChannel.SPIGOT_COMMAND.name());
			registerRedisSub(redisAccess.connect(), new BungeeTeamspeakIdReceiver(), RedisChannel.BUNGEE_SEND_TEAMSPEAKID.name());
			registerRedisSub(redisAccess.connect(), new BungeeServerInfoReceiver(), RedisChannel.BUNGEE_SEND_SERVERSINFOS2.name());
			registerRedisSub(redisAccess.connect(), new BungeeAskSomething(), RedisChannel.BUNGEE_ASK_SOMETHING.name());
			RedisSpigotSend.askServerName();
			sendMessage("&aConnexion à &2Redis&a établie.");
		} else {
			if (i % 100 == 0)
				sendMessage("&cConnexion à &4Redis&c impossible.");
			getTask().runTaskLater(() -> setupRedis(i), 10, TimeUnit.SECONDS);
		}
	}

	private void setupDatabase(int... is) {
		int i1 = 0;
		if (is != null && is.length != 0)
			i1 = is[0] + 1;
		int i = i1;
		DbCredentials dbcredentials = new DbCredentials(getConfig());
		database = new DbConnection(dbcredentials);
		if (database.connect())
			sendMessage("&aConnexion à la base de donnée &2" + dbcredentials.getDatabase() + "&a établie.");
		else {
			if (i % 100 == 0)
				sendMessage("&cConnexion à la base de donnée &4" + dbcredentials.getDatabase() + "&c impossible.");
			getTask().runTaskLater(() -> setupDatabase(i), 10, TimeUnit.SECONDS);
		}
	}

	public void registerRedisSub(Jedis jedis, JedisPubSub sub, String channel) {
		Thread t = new Thread(() -> {
			jedis.subscribe(sub, channel);
			jedis.disconnect();
		}, "Redis sub " + channel);
		Thread.UncaughtExceptionHandler h = (th, ex) -> {
			ex.printStackTrace();
			if (RedisAccess.INSTANCE != null)
				registerRedisSub(RedisAccess.INSTANCE.connect(), sub, channel);
		};
		t.setUncaughtExceptionHandler(h);
		t.start();
	}

	@Override
	public void retreiveMonitorInfos(Consumer<List<MonitorInfo>> callback, boolean freshDoubleCallBack) {
		if (monitorInfos.isEmpty() || Utils.getCurrentTimeInSeconds() - lastInfo > 10)
			if (freshDoubleCallBack)
				RedisSpigotSend.askServerInfo(callback);
			else
				RedisSpigotSend.askServerInfo(null);
		if (callback != null)
			callback.accept(monitorInfos);
	}

	@Override
	public void setStatus(ServerStatus status) {
		this.status = status;
		RedisSpigotSend.changeStatus(status);
	}
}
