package fr.olympa.core.spigot;

import java.io.File;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.spigotmc.SpigotConfig;

import com.google.gson.Gson;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.common.groups.OlympaGroup;
import fr.olympa.api.common.logger.LoggerUtils;
import fr.olympa.api.common.module.OlympaModule;
import fr.olympa.api.common.permission.OlympaPermission;
import fr.olympa.api.common.permission.list.OlympaAPIPermissionsSpigot;
import fr.olympa.api.common.player.OlympaAccount;
import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.common.plugin.OlympaSpigot;
import fr.olympa.api.common.provider.AccountProviderAPI;
import fr.olympa.api.common.redis.RedisChannel;
import fr.olympa.api.common.redis.RedisClass;
import fr.olympa.api.common.redis.ResourcePackHandler;
import fr.olympa.api.common.report.ReportReason;
import fr.olympa.api.common.server.ServerInfoAdvanced;
import fr.olympa.api.common.server.ServerStatus;
import fr.olympa.api.spigot.command.CommandListener;
import fr.olympa.api.spigot.command.essentials.ColorCommand;
import fr.olympa.api.spigot.command.essentials.EcseeCommand;
import fr.olympa.api.spigot.command.essentials.FlyCommand;
import fr.olympa.api.spigot.command.essentials.GamemodeCommand;
import fr.olympa.api.spigot.command.essentials.InvseeCommand;
import fr.olympa.api.spigot.command.essentials.ItemCommand;
import fr.olympa.api.spigot.command.essentials.ListCommand;
import fr.olympa.api.spigot.command.essentials.PingCommand;
import fr.olympa.api.spigot.command.essentials.SayCommand;
import fr.olympa.api.spigot.frame.ImageFrameManager;
import fr.olympa.api.spigot.gui.Inventories;
import fr.olympa.api.spigot.holograms.HologramsManager;
import fr.olympa.api.spigot.region.tracking.RegionManager;
import fr.olympa.api.utils.CacheStats;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.common.SwearHandler;
import fr.olympa.core.common.permission.list.OlympaCorePermissionsSpigot;
import fr.olympa.core.common.provider.AccountProvider;
import fr.olympa.core.common.provider.AccountProviderGetter;
import fr.olympa.core.common.redis.RedisAccess;
import fr.olympa.core.common.sql.DbConnection;
import fr.olympa.core.common.sql.DbCredentials;
import fr.olympa.core.common.sql.MySQL;
import fr.olympa.core.common.utils.ErrorLoggerHandler;
import fr.olympa.core.common.utils.ErrorOutputStream;
import fr.olympa.core.common.utils.GsonCustomizedObjectTypeAdapter;
import fr.olympa.core.spigot.chat.CancerListener;
import fr.olympa.core.spigot.chat.ChatCommand;
import fr.olympa.core.spigot.chat.ChatListener;
import fr.olympa.core.spigot.commands.ConfigCommand;
import fr.olympa.core.spigot.commands.GenderCommand;
import fr.olympa.core.spigot.commands.NewSpigotCommand;
import fr.olympa.core.spigot.commands.PermissionCommand;
import fr.olympa.core.spigot.commands.RestartCommand;
import fr.olympa.core.spigot.commands.ToggleErrors;
import fr.olympa.core.spigot.commands.UtilsCommand;
import fr.olympa.core.spigot.datamanagment.DataManagmentListener;
import fr.olympa.core.spigot.datamanagment.OnLoadListener;
import fr.olympa.core.spigot.groups.GroupCommand;
import fr.olympa.core.spigot.groups.GroupListener;
import fr.olympa.core.spigot.groups.StaffCommand;
import fr.olympa.core.spigot.module.CoreModules;
import fr.olympa.core.spigot.redis.RedisSpigotSend;
import fr.olympa.core.spigot.redis.receiver.BungeeAskPlayerServerReceiver;
import fr.olympa.core.spigot.redis.receiver.BungeeSendOlympaPlayerReceiver;
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
import fr.olympa.core.spigot.versionhook.VersionHook;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

public class OlympaCore extends OlympaSpigot implements Listener {

	private static OlympaCore instance = null;

	public static OlympaCore getInstance() {
		return instance;
	}

	protected DbConnection database = null;
	private SwearHandler swearHandler;
	private ErrorOutputStream errorOutputStream;
	public GamemodeCommand gamemodeCommand = null;
	private RedisAccess redisAccess;
	private boolean redisConnected = false;
	private boolean dbConnected = false;

	@Override
	public VersionHook getVersionHandler() {
		return (VersionHook) versionHandler;
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
		LoggerUtils.unHookAll();
		sendMessage("&4%s&c (%s) est désactivé.", getDescription().getName(), getDescription().getVersion());
	}

	@Override
	public void onLoad() {
		instance = this;
		LinkSpigotBungee.Provider.link = this;
		SpigotConfig.sendNamespaced = false;
		errorOutputStream = new ErrorOutputStream(System.err, RedisSpigotSend::sendError, run -> getServer().getScheduler().runTaskLater(this, run, 20));
		System.setErr(new PrintStream(errorOutputStream));
		LoggerUtils.hook(new ErrorLoggerHandler(RedisSpigotSend::sendError));
		sendMessage("&6%s&e (%s) est chargé.", getDescription().getName(), getDescription().getVersion());
	}

	@Override
	public void onEnable() {
		try {
			OlympaPermission.registerPermissions(OlympaAPIPermissionsSpigot.class);
			OlympaPermission.registerPermissions(OlympaCorePermissionsSpigot.class);
			new RestartCommand(this).registerPreProcess().register();
			PluginManager pluginManager = getServer().getPluginManager();
			pluginManager.registerEvents(new OnLoadListener(), this);
			CacheStats.addDebugMap("PERMISSION", OlympaPermission.permissions);
			ReportReason.registerReason(ReportReason.class);
			super.onEnable();
			if (config != null) {
				config.addTask("redis_config", config -> {
					redisAccess = RedisAccess.init(config);
					AccountProviderAPI.setRedisConnection(redisAccess);
				});
				setupRedis();
				setupDatabase();
			}
			RedisClass.SERVER_INFO_ADVANCED.registerCallback(mi -> {
				lastInfo = Utils.getCurrentTimeInSeconds();
				monitorInfos.clear();
				monitorInfos.addAll(mi);
			});
			RedisSpigotSend.errorsEnabled = true;
			swearHandler = new SwearHandler(getConfig().getStringList("chat.insult"));
			imageFrameManager = new ImageFrameManager(this, "maps.yml", "images");
			try {
				MySQL sql = new MySQL(database);
				AccountProviderAPI.init(sql, new AccountProviderGetter(sql));
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
			new PluginCommand(this).registerPreProcess().register();
			new HelpCommand(this).register().registerPreProcess();
			new SayCommand(this).registerPreProcess();
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
			new ColorCommand(this).register();
			new ToggleErrors(this).register();
			new StaffCommand(this).register();
			new ItemCommand(this).register();
			new NewSpigotCommand(this).register().registerPreProcess();
			new ListCommand(this).register().registerPreProcess();

			new AntiWD(this);
			try {
				getTask().runTask(() -> versionHandler = new VersionHook(this));
			} catch (Exception e) {
				getLogger().severe("Une erreur est survenue lors de la detection des versions avec ViaVersion ou ProtocolSupport.");
				e.printStackTrace();
			}
			OlympaGroup defaultGroup = OlympaGroup.PLAYER;
			defaultGroup.setRuntimePermission("minecraft.command.help", false);
			defaultGroup.setRuntimePermission("minecraft.command.me", false);
			defaultGroup.setRuntimePermission("minecraft.command.msg", false);
			defaultGroup.setRuntimePermission("bukkit.command.version", false);
			defaultGroup.setRuntimePermission("bukkit.command.plugins", false);
			defaultGroup.setRuntimePermission("bukkit.command.help", false);
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				Collection<? extends Player> players = Bukkit.getOnlinePlayers();
				players.forEach(p -> {
					OlympaAccount account = new AccountProvider(p.getUniqueId());
					OlympaPlayer olympaPlayer = account.getFromCache();
					if (olympaPlayer != null) {
						//MySQL.savePlayerPluginDatas(olympaPlayer);
						account.saveToRedis(olympaPlayer);
						account.removeFromCache();
					}
					System.out.println("Succes save " + p.getName());
				});
			}));
			sendMessage("&2%s&a (%s) est activé.", getDescription().getName(), getDescription().getVersion());
		} catch (Error | Exception e) {
			setStatus(ServerStatus.MAINTENANCE);
			getLogger().severe(String.format("Une erreur est survenu lors de l'activation de %s. Le serveur est désormais en maintenance.", this.getClass().getSimpleName()));
			e.printStackTrace();
		}
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
		redisAccess.connect();
		if (redisAccess.isConnected()) {
			redisConnected = true;
			registerRedisSub(redisAccess.getConnection(), new BungeeServerNameReceiver(), RedisChannel.BUNGEE_ASK_SEND_SERVERNAME.name());
			registerRedisSub(new SpigotOlympaPlayerReceiver(), RedisChannel.BUNGEE_ASK_SEND_OLYMPAPLAYER.name());
			registerRedisSub(new SpigotReceiveOlympaPlayerReceiver(), RedisChannel.SPIGOT_SEND_OLYMPAPLAYER.name());
			registerRedisSub(new BungeeSendOlympaPlayerReceiver(), RedisChannel.BUNGEE_SEND_OLYMPAPLAYER.name());
			registerRedisSub(new SpigotGroupChangedReceiver(), RedisChannel.SPIGOT_CHANGE_GROUP.name());
			registerRedisSub(new SpigotGroupChangedReceiveReceiver(), RedisChannel.SPIGOT_CHANGE_GROUP_RECEIVE.name());
			registerRedisSub(new BungeeAskPlayerServerReceiver(), RedisChannel.BUNGEE_SEND_PLAYERSERVER.name());
			registerRedisSub(new SpigotCommandReceiver(), RedisChannel.SPIGOT_COMMAND.name());
			registerRedisSub(new BungeeTeamspeakIdReceiver(), RedisChannel.BUNGEE_SEND_TEAMSPEAKID.name());
			//			registerRedisSub(new BungeeServerInfoReceiver(this), RedisChannel.BUNGEE_SEND_SERVERSINFOS2.name());
			//			registerRedisSub(redisAccess.connect(), new BungeeAskSomething(), RedisChannel.BUNGEE_ASK_SOMETHING.name());
			RedisClass.registerSpigotSubChannels(this);
			RedisSpigotSend.askServerName();
			sendMessage("&aConnexion à &2Redis&a établie.");
		} else {
			if (i % 100 == 0)
				sendMessage("&cConnexion à &4Redis&c impossible.");
			getTask().runTaskLater(() -> setupRedis(i), 10, TimeUnit.SECONDS);
			redisConnected = false;
		}
	}

	private void setupDatabase(int... is) {
		int i1 = 0;
		if (is != null && is.length != 0)
			i1 = is[0] + 1;
		int i = i1;
		DbCredentials dbcredentials = new DbCredentials(getConfig());
		database = new DbConnection(dbcredentials);
		if (database.connect()) {
			dbConnected = true;
			sendMessage("&aConnexion à la base de donnée &2" + dbcredentials.getDatabase() + "&a établie.");
		} else {
			if (i % 100 == 0)
				sendMessage("&cConnexion à la base de donnée &4" + dbcredentials.getDatabase() + "&c impossible.");
			getTask().runTaskLater(() -> setupDatabase(i), 10, TimeUnit.SECONDS);
			dbConnected = false;
		}
	}

	@Override
	public void registerRedisSub(JedisPubSub sub, String channel) {
		registerRedisSub(redisAccess.connect(), sub, channel);
	}

	@Override
	public void registerRedisSub(Jedis jedis, JedisPubSub sub, String channel) {
		Thread t = new Thread(() -> {
			jedis.subscribe(sub, channel);
			jedis.disconnect();
		}, "Redis sub " + channel);
		Thread.UncaughtExceptionHandler h = (th, ex) -> {
			ex.printStackTrace();
			if (redisAccess != null)
				registerRedisSub(redisAccess.connect(), sub, channel);
		};
		t.setUncaughtExceptionHandler(h);
		t.start();
	}

	@Override
	public void retreiveMonitorInfos(BiConsumer<List<ServerInfoAdvanced>, Boolean> callback, boolean freshDoubleCallBack) {
		if (monitorInfos.isEmpty() || Utils.getCurrentTimeInSeconds() - lastInfo > 10)
			if (freshDoubleCallBack)
				RedisClass.ASK_SERVER_INFO.askServerInfo(callback);
			else
				RedisClass.ASK_SERVER_INFO.askServerInfo(null);
		if (callback != null)
			callback.accept(monitorInfos, true);
	}

	@Override
	public void registerPackListener(ResourcePackHandler packHandler) {
		registerRedisSub(RedisAccess.INSTANCE.connect(), new JedisPubSub() {
			@Override
			public void onMessage(String channel, String message) {
				super.onMessage(channel, message);
				String[] args = message.split(";");
				packHandler.handle(args[0], args[1], Boolean.valueOf(args[2]));
			}
		}, RedisChannel.BUNGEE_PLAYER_RESOUREPACK.name());
	}

	@Override
	public void setStatus(ServerStatus status) {
		this.status = status;
		RedisSpigotSend.changeStatus(status);
	}

	@Override
	public Gson getGson() {
		return GsonCustomizedObjectTypeAdapter.GSON;
	}

	@Override
	public List<String> getPlayersNames() {
		return Bukkit.getServer().getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
	}

	@Override
	public Connection getDatabase() throws SQLException {
		Connection connection = database.getConnection();
		if (!database.isConnected())
			dbConnected = false;
		else if (!dbConnected)
			dbConnected = true;
		return connection;
	}

	@Override
	public RedisAccess getRedisAccess() {
		if (!redisAccess.isConnected())
			redisConnected = false;
		else if (!redisConnected)
			redisConnected = true;
		return redisAccess;
	}

	@Override
	public boolean isRedisConnected() {
		return redisConnected;
	}

	@Override
	public boolean isDatabaseConnected() {
		return dbConnected;
	}
}
