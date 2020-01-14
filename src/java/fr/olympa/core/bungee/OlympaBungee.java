package fr.olympa.core.bungee;

import java.sql.Connection;
import java.sql.SQLException;

import fr.olympa.api.provider.RedisAccess;
import fr.olympa.api.sql.DbConnection;
import fr.olympa.api.sql.DbCredentials;
import fr.olympa.api.sql.MySQL;
import fr.olympa.core.bungee.auth.AuthListener;
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
import fr.olympa.core.bungee.maintenance.ConnectionListener;
import fr.olympa.core.bungee.maintenance.MaintenanceCommand;
import fr.olympa.core.bungee.maintenance.MaintenanceListener;
import fr.olympa.core.bungee.motd.MotdListener;
import fr.olympa.core.bungee.utils.BungeeConfigUtils;
import fr.olympa.core.bungee.utils.BungeeUtils;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.api.scheduler.TaskScheduler;
import net.md_5.bungee.config.Configuration;

public class OlympaBungee extends Plugin {

	private static OlympaBungee INSTANCE;

	public static OlympaBungee getInstance() {
		return INSTANCE;
	}

	protected DbConnection database = null;

	public Connection getDatabase() throws SQLException {
		return this.database.getConnection();
	}

	private String getPrefixConsole() {
		return "&f[&6" + this.getDescription().getName() + "&f] &e";
	}

	public TaskScheduler getTask() {
		// TODO Auto-generated method stub
		return ProxyServer.getInstance().getScheduler();
	}

	@Override
	public void onDisable() {
		this.sendMessage("§4" + this.getDescription().getName() + "§c (" + this.getDescription().getVersion() + ") is disabled.");
	}

	@Override
	public void onEnable() {
		INSTANCE = this;
		BungeeConfigUtils.loadConfigs();
		this.setupDatabase();
		new MySQL(this.database);
		RedisAccess.init("bungee");

		PluginManager pluginManager = this.getProxy().getPluginManager();
		pluginManager.registerListener(this, new MotdListener());
		pluginManager.registerListener(this, new MaintenanceListener());
		pluginManager.registerListener(this, new ConnectionListener());
		pluginManager.registerListener(this, new AuthListener());

		pluginManager.registerListener(this, new SanctionListener());
		pluginManager.registerListener(this, new TestListener());

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

		new MaintenanceCommand(this).register();

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
