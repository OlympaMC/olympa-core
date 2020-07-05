package fr.olympa.core.bungee;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.provider.RedisAccess;
import fr.olympa.api.redis.RedisChannel;
import fr.olympa.api.server.ServerStatus;
import fr.olympa.api.sql.DbConnection;
import fr.olympa.api.sql.DbCredentials;
import fr.olympa.api.sql.MySQL;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.api.command.BungeeCommandListener;
import fr.olympa.core.bungee.api.config.BungeeCustomConfig;
import fr.olympa.core.bungee.api.task.BungeeTask;
import fr.olympa.core.bungee.ban.commands.BanCommand;
import fr.olympa.core.bungee.ban.commands.BanHistoryCommand;
import fr.olympa.core.bungee.ban.commands.BanIpCommand;
import fr.olympa.core.bungee.ban.commands.DelbanCommand;
import fr.olympa.core.bungee.ban.commands.ForceKickCommand;
import fr.olympa.core.bungee.ban.commands.KickCommand;
import fr.olympa.core.bungee.ban.commands.MuteCommand;
import fr.olympa.core.bungee.ban.commands.UnbanCommand;
import fr.olympa.core.bungee.ban.commands.UnmuteCommand;
import fr.olympa.core.bungee.ban.listeners.SanctionListener;
import fr.olympa.core.bungee.commands.BungeelagCommand;
import fr.olympa.core.bungee.commands.InfoCommand;
import fr.olympa.core.bungee.connectionqueue.ConnectionQueueListener;
import fr.olympa.core.bungee.connectionqueue.LeaveQueue;
import fr.olympa.core.bungee.datamanagment.AuthListener;
import fr.olympa.core.bungee.datamanagment.GetUUIDCommand;
import fr.olympa.core.bungee.login.commands.EmailCommand;
import fr.olympa.core.bungee.login.commands.LoginCommand;
import fr.olympa.core.bungee.login.commands.RegisterCommand;
import fr.olympa.core.bungee.login.listener.FailsPasswordEvent;
import fr.olympa.core.bungee.login.listener.LoginChatListener;
import fr.olympa.core.bungee.login.listener.OlympaLoginListener;
import fr.olympa.core.bungee.maintenance.MaintenanceCommand;
import fr.olympa.core.bungee.maintenance.MaintenanceListener;
import fr.olympa.core.bungee.motd.MotdListener;
import fr.olympa.core.bungee.privatemessage.PrivateMessageCommand;
import fr.olympa.core.bungee.privatemessage.PrivateMessageListener;
import fr.olympa.core.bungee.privatemessage.PrivateMessageToggleCommand;
import fr.olympa.core.bungee.privatemessage.ReplyCommand;
import fr.olympa.core.bungee.protocol.ProtocolListener;
import fr.olympa.core.bungee.redis.AskServerNameListener;
import fr.olympa.core.bungee.redis.PlayerGroupChangeListener;
import fr.olympa.core.bungee.redis.ServerSwitchListener;
import fr.olympa.core.bungee.redis.ShutdownListener;
import fr.olympa.core.bungee.security.BasicSecurityListener;
import fr.olympa.core.bungee.servers.MonitorServers;
import fr.olympa.core.bungee.servers.ServersListener;
import fr.olympa.core.bungee.servers.commands.ListServerCommand;
import fr.olympa.core.bungee.servers.commands.LobbyCommand;
import fr.olympa.core.bungee.servers.commands.RestartBungeeCommand;
import fr.olympa.core.bungee.servers.commands.RestartServerCommand;
import fr.olympa.core.bungee.servers.commands.ServerSwitchCommand;
import fr.olympa.core.bungee.servers.commands.StartServerCommand;
import fr.olympa.core.bungee.servers.commands.StopServerCommand;
import fr.olympa.core.bungee.staffchat.StaffChatCommand;
import fr.olympa.core.bungee.staffchat.StaffChatListener;
import fr.olympa.core.bungee.tabtext.TabTextListener;
import fr.olympa.core.bungee.utils.BungeeUtils;
import fr.olympa.core.bungee.vpn.VpnListener;
import fr.olympa.core.bungee.vpn.VpnSql;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.config.Configuration;
import redis.clients.jedis.JedisPubSub;

public class OlympaBungee extends Plugin implements LinkSpigotBungee {

	private static OlympaBungee instance;

	public static OlympaBungee getInstance() {
		return instance;
	}

	protected DbConnection database = null;
	protected long uptime = Utils.getCurrentTimeInSeconds();
	protected BungeeCustomConfig defaultConfig;
	protected BungeeCustomConfig maintConfig;
	private BungeeTask bungeeTask;
	private ServerStatus status;

	public Configuration getConfig() {
		return defaultConfig.getConfig();
	}

	@Override
	public Connection getDatabase() throws SQLException {
		return database.getConnection();
	}

	public BungeeCustomConfig getDefaultConfig() {
		return defaultConfig;
	}

	public Configuration getMaintConfig() {
		return maintConfig != null ? maintConfig.getConfig() : null;
	}

	public BungeeCustomConfig getMaintCustomConfig() {
		return maintConfig;
	}

	private String getPrefixConsole() {
		return "&f[&6" + getDescription().getName() + "&f] &e";
	}

	@Override
	public String getServerName() {
		return "bungee";
	}

	public BungeeTask getTask() {
		return bungeeTask;
	}

	@Override
	public String getUptime() {
		return Utils.timestampToDuration(uptime);
	}

	@Override
	public long getUptimeLong() {
		return uptime;
	}

	@Override
	public void launchAsync(Runnable run) {
		getTask().runTaskAsynchronously(run);
	}

	@Override
	public void onDisable() {
		sendMessage("&4" + getDescription().getName() + "&c (" + getDescription().getVersion() + ") est désactivé.");
	}

	@Override
	public void onEnable() {
		instance = this;
		LinkSpigotBungee.Provider.link = this;

		bungeeTask = new BungeeTask(this);
		defaultConfig = new BungeeCustomConfig(this, "config");
		defaultConfig.load();

		maintConfig = new BungeeCustomConfig(this, "maintenance");
		maintConfig.load();
		status = ServerStatus.get(maintConfig.getConfig().getString("settings.status"));
		setupDatabase();
		new MySQL(database);
		new VpnSql(database);
		setupRedis();

		// BungeeTaskManager tasks = new BungeeTaskManager(this);
		// tasks.runTaskAsynchronously(() -> this.jedis.subscribe(new
		// RedisTestListener(), "test"));
		// tasks.runTaskAsynchronously(() -> this.jedis.subscribe(new
		// OlympaPlayerBungeeReceiveListener(), "OlympaPlayerReceive"));

		PluginManager pluginManager = getProxy().getPluginManager();
		pluginManager.registerListener(this, new MotdListener());
		pluginManager.registerListener(this, new MaintenanceListener());
		pluginManager.registerListener(this, new AuthListener());
		pluginManager.registerListener(this, new BasicSecurityListener());
		pluginManager.registerListener(this, new SanctionListener());
		pluginManager.registerListener(this, new ServersListener());
		// pluginManager.registerListener(this, new TestListener());
		pluginManager.registerListener(this, new PrivateMessageListener());
		pluginManager.registerListener(this, new LoginChatListener());
		pluginManager.registerListener(this, new FailsPasswordEvent());
		pluginManager.registerListener(this, new VpnListener());
		pluginManager.registerListener(this, new OlympaLoginListener());
		pluginManager.registerListener(this, new StaffChatListener());
		pluginManager.registerListener(this, new ProtocolListener());
		pluginManager.registerListener(this, new TabTextListener());
		pluginManager.registerListener(this, new BungeeCommandListener());
		pluginManager.registerListener(this, new ConnectionQueueListener());

		new BanCommand(this).register();
		new BanHistoryCommand(this).register();
		new BanIpCommand(this).register();
		new DelbanCommand(this).register();
		new ForceKickCommand(this).register();
		new KickCommand(this).register();
		new MuteCommand(this).register();
		new MuteCommand(this).register();
		new UnbanCommand(this).register();
		new UnmuteCommand(this).register();
		new GetUUIDCommand(this).register();
		new ReplyCommand(this).register();
		new PrivateMessageCommand(this).register();
		new PrivateMessageToggleCommand(this).register();
		new ListServerCommand(this).register();
		new MaintenanceCommand(this).register();
		LoginCommand loginCommand = new LoginCommand(this);
		loginCommand.register();
		loginCommand.registerPreProcess();
		RegisterCommand registerCommand = new RegisterCommand(this);
		registerCommand.register();
		registerCommand.registerPreProcess();
		new EmailCommand(this).register();
		new ServerSwitchCommand(this).register();
		new InfoCommand(this).register();
		new StaffChatCommand(this).register();
		new StartServerCommand(this).register();
		new StopServerCommand(this).register();
		new RestartServerCommand(this).register();
		new RestartBungeeCommand(this).register();
		new LobbyCommand(this).register();
		new LeaveQueue(this).register();
		new BungeelagCommand(this).register();

		new MonitorServers(this);
		sendMessage("&2" + getDescription().getName() + "&a (" + getDescription().getVersion() + ") est activé.");
	}

	@Override
	@SuppressWarnings("deprecation")
	public void sendMessage(String message) {
		getProxy().getConsole().sendMessage(BungeeUtils.color(getPrefixConsole() + message));
	}

	public void setDefaultConfig(BungeeCustomConfig defaultConfig) {
		this.defaultConfig = defaultConfig;
	}

	public void setMaintConfig(BungeeCustomConfig maintConfig) {
		this.maintConfig = maintConfig;
	}

	private void setupDatabase(int... is) {
		int i1 = 0;
		if (is != null && is.length != 0)
			i1 = is[0] + 1;
		int i = i1;
		Configuration path = defaultConfig.getConfig().getSection("database.default");
		String host = path.getString("host");
		String user = path.getString("user");
		String password = path.getString("password");
		String databaseName = path.getString("database");
		int port = path.getInt("port");
		if (port == 0)
			port = 3306;
		DbCredentials dbcredentials = new DbCredentials(host, user, password, databaseName, port);
		database = new DbConnection(dbcredentials);
		if (database.connect())
			sendMessage("&aConnexion à la base de donnée &2" + dbcredentials.getDatabase() + "&a établie.");
		else {
			if (i % 100 == 0)
				sendMessage("&cConnexion à la base de donnée &4" + dbcredentials.getDatabase() + "&c impossible.");
			getTask().runTaskLater("db_setup", () -> setupDatabase(i), 10, TimeUnit.SECONDS);
		}
	}

	public void registerRedisSub(RedisAccess redisAccess, JedisPubSub sub, String channel) {
		new Thread(() -> redisAccess.newConnection().subscribe(sub, channel), "Redis sub " + channel).start();
	}

	private void setupRedis(int... is) {
		int i1 = 0;
		if (is != null && is.length != 0)
			i1 = is[0] + 1;
		int i = i1;
		RedisAccess redisAccess = RedisAccess.init("bungee");
		redisAccess.connect();
		if (redisAccess.isConnected()) {
			registerRedisSub(redisAccess, new AskServerNameListener(), RedisChannel.SPIGOT_ASK_SERVERNAME.name());
			registerRedisSub(redisAccess, new PlayerGroupChangeListener(), RedisChannel.SPIGOT_PLAYER_HAS_GROUP_CHANGED.name());
			registerRedisSub(redisAccess, new ShutdownListener(), RedisChannel.SPIGOT_SERVER_SHUTDOWN.name());
			registerRedisSub(redisAccess, new ServerSwitchListener(), RedisChannel.SPIGOT_PLAYER_SWITCH_SERVER.name());
			sendMessage("&aConnexion à &2Redis&a établie.");
		} else {
			if (i % 100 == 0)
				sendMessage("&cConnexion à &4Redis&c impossible.");
			getTask().runTaskLater("redis_setup", () -> setupRedis(i), 10, TimeUnit.SECONDS);
		}
	}

	@Override
	public ServerStatus getStatus() {
		return status;
	}

	public ServerStatus setStatus(ServerStatus status) {
		return this.status = status;
	}

	@Override
	public boolean isSpigot() {
		return false;
	}
}
