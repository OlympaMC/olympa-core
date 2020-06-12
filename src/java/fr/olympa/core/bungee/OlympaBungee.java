package fr.olympa.core.bungee;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.provider.RedisAccess;
import fr.olympa.api.redis.RedisTestListener;
import fr.olympa.api.sql.DbConnection;
import fr.olympa.api.sql.DbCredentials;
import fr.olympa.api.sql.MySQL;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.api.config.CustomBungeeConfig;
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
import fr.olympa.core.bungee.commands.InfoCommand;
import fr.olympa.core.bungee.commands.RestartBungeeCommand;
import fr.olympa.core.bungee.datamanagment.AuthListener;
import fr.olympa.core.bungee.datamanagment.GetUUIDCommand;
import fr.olympa.core.bungee.footer.FooterListener;
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
import fr.olympa.core.bungee.redis.ShutdownListener;
import fr.olympa.core.bungee.security.BasicSecurityListener;
import fr.olympa.core.bungee.servers.MonitorServers;
import fr.olympa.core.bungee.servers.ServersListener;
import fr.olympa.core.bungee.servers.commands.ListServerCommand;
import fr.olympa.core.bungee.servers.commands.RestartServerCommand;
import fr.olympa.core.bungee.servers.commands.ServerSwitchCommand;
import fr.olympa.core.bungee.servers.commands.StartServerCommand;
import fr.olympa.core.bungee.servers.commands.StopServerCommand;
import fr.olympa.core.bungee.staffchat.StaffChatCommand;
import fr.olympa.core.bungee.staffchat.StaffChatListener;
import fr.olympa.core.bungee.utils.BungeeUtils;
import fr.olympa.core.bungee.vpn.VpnListener;
import fr.olympa.core.bungee.vpn.VpnSql;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.api.scheduler.TaskScheduler;
import net.md_5.bungee.config.Configuration;

public class OlympaBungee extends Plugin implements LinkSpigotBungee {
	
	private static OlympaBungee instance;
	
	public static OlympaBungee getInstance() {
		return instance;
	}
	
	protected DbConnection database = null;
	protected long uptime = Utils.getCurrentTimeInSeconds();
	protected CustomBungeeConfig defaultConfig;
	protected CustomBungeeConfig maintConfig;
	
	public Configuration getConfig() {
		return defaultConfig.getConfig();
	}
	
	@Override
	public Connection getDatabase() throws SQLException {
		return database.getConnection();
	}
	
	public CustomBungeeConfig getDefaultConfig() {
		return defaultConfig;
	}
	
	public Configuration getMaintConfig() {
		return maintConfig != null ? maintConfig.getConfig() : null;
	}
	
	public CustomBungeeConfig getMaintCustomConfig() {
		return maintConfig;
	}
	
	private String getPrefixConsole() {
		return "&f[&6" + getDescription().getName() + "&f] &e";
	}
	
	public String getServerName() {
		return "bungee";
	}
	
	public TaskScheduler getTask() {
		return ProxyServer.getInstance().getScheduler();
	}
	
	public String getUptime() {
		return Utils.timestampToDuration(uptime);
	}
	
	public long getUptimeLong() {
		return uptime;
	}
	
	@Override
	public void launchAsync(Runnable run) {
		getTask().runAsync(this, run);
	}
	
	@Override
	public void onDisable() {
		sendMessage("&4" + getDescription().getName() + "&c (" + getDescription().getVersion() + ") est désactivé.");
	}
	
	@Override
	public void onEnable() {
		instance = this;
		LinkSpigotBungee.Provider.link = this;
		
		defaultConfig = new CustomBungeeConfig(this, "config");
		defaultConfig.load();
		maintConfig = new CustomBungeeConfig(this, "maintenance");
		maintConfig.load();
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
		pluginManager.registerListener(this, new FooterListener());
		
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
		new LoginCommand(this).register();
		new RegisterCommand(this).register();
		new EmailCommand(this).register();
		new ServerSwitchCommand(this).register();
		new InfoCommand(this).register();
		new StaffChatCommand(this).register();
		new StartServerCommand(this).register();
		new StopServerCommand(this).register();
		new RestartServerCommand(this).register();
		new RestartBungeeCommand(this).register();
		
		new MonitorServers(this);
		sendMessage("&2" + getDescription().getName() + "&a (" + getDescription().getVersion() + ") est activé.");
	}
	
	@SuppressWarnings("deprecation")
	public void sendMessage(String message) {
		getProxy().getConsole().sendMessage(BungeeUtils.color(getPrefixConsole() + message));
	}
	
	public void setDefaultConfig(CustomBungeeConfig defaultConfig) {
		this.defaultConfig = defaultConfig;
	}
	
	public void setMaintConfig(CustomBungeeConfig maintConfig) {
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
			getTask().schedule(this, () -> setupDatabase(i), 10, TimeUnit.SECONDS);
		}
	}
	
	private void setupRedis(int... is) {
		int i1 = 0;
		if (is != null && is.length != 0)
			i1 = is[0] + 1;
		int i = i1;
		RedisAccess redisAcces = RedisAccess.init("bungee");
		redisAcces.connect();
		if (redisAcces.isConnected()) {
			new Thread(() -> redisAcces.newConnection().subscribe(new AskServerNameListener(), "askServerName"), "subscriberThread").start();
			new Thread(() -> redisAcces.newConnection().subscribe(new PlayerGroupChangeListener(), "playerGroupChange"), "subscriberThread").start();
			new Thread(() -> redisAcces.newConnection().subscribe(new ShutdownListener(), "shutdown"), "subscriberThread").start();
			// Test
			new Thread(() -> redisAcces.newConnection().subscribe(new RedisTestListener(), "test"), "subscriberThread").start();
			sendMessage("&aConnexion à &2Redis&a établie.");
		} else {
			if (i % 100 == 0)
				sendMessage("&cConnexion à &4Redis&c impossible.");
			getTask().schedule(this, () -> setupRedis(i), 10, TimeUnit.SECONDS);
		}
	}
}
