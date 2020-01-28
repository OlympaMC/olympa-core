package fr.olympa.core.bungee;

import java.sql.Connection;
import java.sql.SQLException;

import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.provider.RedisAccess;
import fr.olympa.api.redis.RedisTestListener;
import fr.olympa.api.sql.DbConnection;
import fr.olympa.api.sql.DbCredentials;
import fr.olympa.api.sql.MySQL;
import fr.olympa.api.utils.Utils;
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
import fr.olympa.core.bungee.datamanagment.AuthListener;
import fr.olympa.core.bungee.datamanagment.BasicSecurityListener;
import fr.olympa.core.bungee.datamanagment.GetUUIDCommand;
import fr.olympa.core.bungee.datamanagment.redislisteners.OlympaPlayerBungeeReceiveListener;
import fr.olympa.core.bungee.login.HandlerHideLogin;
import fr.olympa.core.bungee.login.LoginCommand;
import fr.olympa.core.bungee.login.RegisterCommand;
import fr.olympa.core.bungee.maintenance.ConnectionListener;
import fr.olympa.core.bungee.maintenance.MaintenanceCommand;
import fr.olympa.core.bungee.maintenance.MaintenanceListener;
import fr.olympa.core.bungee.motd.MotdListener;
import fr.olympa.core.bungee.privatemessage.PrivateMessageCommand;
import fr.olympa.core.bungee.privatemessage.PrivateMessageListener;
import fr.olympa.core.bungee.privatemessage.PrivateMessageToggleCommand;
import fr.olympa.core.bungee.privatemessage.ReplyCommand;
import fr.olympa.core.bungee.servers.ListAllCommand;
import fr.olympa.core.bungee.servers.MonitorServers;
import fr.olympa.core.bungee.servers.ServersListener;
import fr.olympa.core.bungee.task.BungeeTaskManager;
import fr.olympa.core.bungee.utils.BungeeConfigUtils;
import fr.olympa.core.bungee.utils.BungeeUtils;
import fr.olympa.core.bungee.vpn.VpnSql;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.api.scheduler.TaskScheduler;
import net.md_5.bungee.config.Configuration;
import redis.clients.jedis.Jedis;

public class OlympaBungee extends Plugin {

	private static OlympaBungee instance;

	public static OlympaBungee getInstance() {
		return instance;
	}

	protected DbConnection database = null;
	protected long uptime = Utils.getCurrentTimeInSeconds();
	protected Jedis jedis;

	public Connection getDatabase() throws SQLException {
		return this.database.getConnection();
	}

	public Jedis getJedis() {
		return this.jedis;
	}

	private String getPrefixConsole() {
		return "&f[&6" + this.getDescription().getName() + "&f] &e";
	}

	public TaskScheduler getTask() {
		return ProxyServer.getInstance().getScheduler();
	}

	public String getUptime() {
		return Utils.timestampToDuration(this.uptime);
	}

	public long getUptimeLong() {
		return this.uptime;
	}

	@Override
	public void onDisable() {
		this.sendMessage("§4" + this.getDescription().getName() + "§c (" + this.getDescription().getVersion() + ") is disabled.");
	}

	@Override
	public void onEnable() {
		instance = this;
		BungeeConfigUtils.loadConfigs();
		this.setupDatabase();
		new MySQL(this.database);
		new VpnSql(this.database);
		RedisAccess redisAcces = RedisAccess.init("bungee");
		this.jedis = redisAcces.connect();
		if (this.jedis.isConnected()) {
			this.sendMessage("&aConnexion à &2Redis&a établie.");
		} else {
			this.sendMessage("&cConnexion à &4Redis&c impossible.");
		}

		BungeeTaskManager tasks = new BungeeTaskManager(this);
		tasks.runTaskAsynchronously(() -> this.jedis.subscribe(new RedisTestListener(), "test"));
		tasks.runTaskAsynchronously(() -> this.jedis.subscribe(new OlympaPlayerBungeeReceiveListener(), "OlympaPlayerReceive"));

		AccountProvider.asyncLaunch = (run) -> this.getTask().runAsync(this, run);

		PluginManager pluginManager = this.getProxy().getPluginManager();
		pluginManager.registerListener(this, new MotdListener());
		pluginManager.registerListener(this, new ConnectionListener());
		pluginManager.registerListener(this, new AuthListener());
		pluginManager.registerListener(this, new BasicSecurityListener());
		pluginManager.registerListener(this, new SanctionListener());
		pluginManager.registerListener(this, new ServersListener());
		pluginManager.registerListener(this, new TestListener());
		pluginManager.registerListener(this, new PrivateMessageListener());
		pluginManager.registerListener(this, new MaintenanceListener());

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
		new ListAllCommand(this).register();
		new MaintenanceCommand(this).register();
		new LoginCommand(this).register();
		new RegisterCommand(this).register();

		new MonitorServers(this);
		this.getLogger().setFilter(new HandlerHideLogin());
		ProxyServer.getInstance().getLogger().setFilter(new HandlerHideLogin());
		this.sendMessage("§2" + this.getDescription().getName() + "§a (" + this.getDescription().getVersion() + ") is activated.");
	}

	@SuppressWarnings("deprecation")
	public void sendMessage(final String message) {
		this.getProxy().getConsole().sendMessage(BungeeUtils.color(this.getPrefixConsole() + message));
	}

	private void setupDatabase() {
		Configuration config = BungeeConfigUtils.getDefaultConfig();
		Configuration path = config.getSection("database.default");
		String host = path.getString("host");
		String user = path.getString("user");
		String password = path.getString("password");
		String databaseName = path.getString("database");
		int port = path.getInt("port");
		if (port == 0) {
			port = 3306;
		}
		DbCredentials dbcredentials = new DbCredentials(host, user, password, databaseName, port);
		this.database = new DbConnection(dbcredentials);
		if (this.database.connect()) {
			this.sendMessage("&aConnexion à la base de donnée &2" + dbcredentials.getDatabase() + "&a établie.");
		} else {
			this.sendMessage("&cConnexion à la base de donnée &4" + dbcredentials.getDatabase() + "&c impossible.");
		}
	}
}
