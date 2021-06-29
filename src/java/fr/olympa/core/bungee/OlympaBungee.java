package fr.olympa.core.bungee;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.google.gson.Gson;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.bungee.command.BungeeCommandListener;
import fr.olympa.api.bungee.command.BungeeCommandListenerWaterFall;
import fr.olympa.api.bungee.plugin.OlympaBungeeCore;
import fr.olympa.api.bungee.utils.BungeeUtils;
import fr.olympa.api.common.groups.SQLGroup;
import fr.olympa.api.common.permission.OlympaPermission;
import fr.olympa.api.common.provider.AccountProviderAPI;
import fr.olympa.api.common.redis.RedisChannel;
import fr.olympa.api.common.redis.RedisClass;
import fr.olympa.api.common.server.ServerInfoAdvanced;
import fr.olympa.api.common.server.ServerStatus;
import fr.olympa.api.utils.CacheStats;
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
import fr.olympa.core.bungee.commands.AllPluginsCommand;
import fr.olympa.core.bungee.commands.BungeeBroadcastCommand;
import fr.olympa.core.bungee.commands.BungeeLagCommand;
import fr.olympa.core.bungee.commands.BungeePingCommand;
import fr.olympa.core.bungee.commands.CreditCommand;
import fr.olympa.core.bungee.commands.InfoCommand;
import fr.olympa.core.bungee.commands.IpCommand;
import fr.olympa.core.bungee.commands.NewBungeeCommand;
import fr.olympa.core.bungee.commands.RedisCommand;
import fr.olympa.core.bungee.connectionqueue.BungeeQueueCommand;
import fr.olympa.core.bungee.connectionqueue.ConnectionQueueListener;
import fr.olympa.core.bungee.connectionqueue.LeaveQueueCommand;
import fr.olympa.core.bungee.datamanagment.AuthListener;
import fr.olympa.core.bungee.login.HandlerLogin;
import fr.olympa.core.bungee.login.commands.EmailCommand;
import fr.olympa.core.bungee.login.commands.LoginCommand;
import fr.olympa.core.bungee.login.commands.PasswdCommand;
import fr.olympa.core.bungee.login.commands.RegisterCommand;
import fr.olympa.core.bungee.login.listener.FailsPasswordEvent;
import fr.olympa.core.bungee.login.listener.LoginChatListener;
import fr.olympa.core.bungee.login.listener.OlympaLoginListener;
import fr.olympa.core.bungee.login.listener.PlayerSwitchListener;
import fr.olympa.core.bungee.maintenance.MaintenanceCommand;
import fr.olympa.core.bungee.maintenance.MaintenanceListener;
import fr.olympa.core.bungee.motd.MotdListener;
import fr.olympa.core.bungee.motd.ShowPingMotdCommand;
import fr.olympa.core.bungee.motd.ShowPingMotdListener;
import fr.olympa.core.bungee.nick.NickCommand;
import fr.olympa.core.bungee.packets.BungeePackets;
import fr.olympa.core.bungee.permission.PermissionCheckListener;
import fr.olympa.core.bungee.privatemessage.PrivateMessageCommand;
import fr.olympa.core.bungee.privatemessage.PrivateMessageListener;
import fr.olympa.core.bungee.privatemessage.PrivateMessageToggleCommand;
import fr.olympa.core.bungee.privatemessage.ReplyCommand;
import fr.olympa.core.bungee.protocol.ProtocolListener;
import fr.olympa.core.bungee.redis.receiver.BungeeCommandReceiver;
import fr.olympa.core.bungee.redis.receiver.SpigotAskMonitorInfoReceiver;
import fr.olympa.core.bungee.redis.receiver.SpigotAskServerNameReceiver;
import fr.olympa.core.bungee.redis.receiver.SpigotGroupChangeReceiverOnBungee;
import fr.olympa.core.bungee.redis.receiver.SpigotOlympaPlayerReceiver;
import fr.olympa.core.bungee.redis.receiver.SpigotReportReceiver;
import fr.olympa.core.bungee.redis.receiver.SpigotServerChangeStatusReceiver;
import fr.olympa.core.bungee.redis.receiver.SpigotServerSwitchReceiver;
import fr.olympa.core.bungee.redis.receiver.SpigotServerSwitchReceiver2;
import fr.olympa.core.bungee.redis.receiver.site.SiteGroupChangeReceiver;
import fr.olympa.core.bungee.security.BasicSecurityListener;
import fr.olympa.core.bungee.servers.MonitorServers;
import fr.olympa.core.bungee.servers.ServersListener;
import fr.olympa.core.bungee.servers.commands.ListPlayerCommand;
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
import fr.olympa.core.bungee.vpn.VpnHandler;
import fr.olympa.core.bungee.vpn.VpnListener;
import fr.olympa.core.common.permission.list.OlympaCorePermissionsBungee;
import fr.olympa.core.common.provider.AccountProviderGetter;
import fr.olympa.core.common.redis.RedisAccess;
import fr.olympa.core.common.sql.DbConnection;
import fr.olympa.core.common.sql.DbCredentials;
import fr.olympa.core.common.sql.MySQL;
import fr.olympa.core.common.utils.GsonCustomizedObjectTypeAdapter;
import fr.olympa.core.spigot.redis.RedisSpigotSend;
import net.md_5.bungee.api.plugin.PluginManager;

public class OlympaBungee extends OlympaBungeeCore {

	private static OlympaBungee instance;

	public static OlympaBungee getInstance() {
		return instance;
	}

	@Override
	public void onDisable() {
		super.onDisable();
		//		RedisAccess.close();
		if (database != null)
			database.close();
		sendMessage("&4" + getDescription().getName() + "&c (" + getDescription().getVersion() + ") est désactivé.");
	}

	@Override
	public void onLoad() {
		super.onLoad();
		instance = this;
		LinkSpigotBungee.Provider.link = this;
	}

	@Override
	public void onEnable() {
		try {
			super.onEnable();
			OlympaPermission.registerPermissions(OlympaCorePermissionsBungee.class);

			new RestartBungeeCommand(this).register();
			setupDatabase();
			defaultConfig.addTask("redis_config", config -> {
				redisAccess = RedisAccess.init(config.getConfig());
				AccountProviderAPI.setRedisConnection(redisAccess);
			});
			setupRedis();
			defaultConfig.addTask("db_config", config -> {
				getDatabaseHandler().updateCredentials(new DbCredentials(config.getConfig()));
			});
			try {
				MySQL sql = new MySQL(getDatabaseHandler());
				AccountProviderAPI.init(sql, new AccountProviderGetter(sql));
			} catch (SQLException ex) {
				sendMessage("§cUne erreur est survenue lors du chargement du MySQL.");
				ex.printStackTrace();
			}

			PluginManager pluginManager = getProxy().getPluginManager();
			pluginManager.registerListener(this, new MotdListener());
			pluginManager.registerListener(this, new MaintenanceListener());
			pluginManager.registerListener(this, new AuthListener());
			pluginManager.registerListener(this, new BasicSecurityListener());
			pluginManager.registerListener(this, new SanctionListener());
			pluginManager.registerListener(this, new ServersListener());
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
			pluginManager.registerListener(this, new PlayerSwitchListener());
			pluginManager.registerListener(this, new PermissionCheckListener());
			pluginManager.registerListener(this, new ShowPingMotdListener());
			if (BungeeUtils.isWaterfall())
				pluginManager.registerListener(this, new BungeeCommandListenerWaterFall());

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
			new ReplyCommand(this).register();
			new PrivateMessageCommand(this).register();
			new PrivateMessageToggleCommand(this).register();
			new ListServerCommand(this).register();
			new ListPlayerCommand(this).register();
			new MaintenanceCommand(this).register();
			new LoginCommand(this).register().registerPreProcess();
			new RegisterCommand(this).register().registerPreProcess();
			new PasswdCommand(this).register().registerPreProcess();
			new EmailCommand(this).register();
			new ServerSwitchCommand(this).register().registerPreProcess();
			new InfoCommand(this).register();
			new StaffChatCommand(this).register();
			new StartServerCommand(this).register();
			new StopServerCommand(this).register();
			new RestartServerCommand(this).register();
			new LobbyCommand(this).register();
			new LeaveQueueCommand(this).register();
			new BungeeLagCommand(this).register();
			new RedisCommand(this).register();
			new BungeePingCommand(this).register();
			new BungeeQueueCommand(this).register();
			//		new BungeeConfigCommand(this).register();
			new NewBungeeCommand(this).register().registerPreProcess();
			new BungeeBroadcastCommand(this).register();
			new NickCommand(this).register();
			new IpCommand(this).register();
			new CreditCommand(this).register();
			new ShowPingMotdCommand(this).register();
			new AllPluginsCommand(this).register();

			MonitorServers.init(this);
			SQLGroup.init();

			CacheStats.addCache("VPN", VpnHandler.cache);
			CacheStats.addCache("WRONG_PASSWORD", HandlerLogin.timesFails);
			CacheStats.addCache("REDIS_ASK_SERVER_OF_PLAYER", RedisSpigotSend.askPlayerServer);
			CacheStats.addDebugMap("PERMISSION", OlympaPermission.permissions);
			try {
				BungeePackets.registerPackets();
			} catch (ReflectiveOperationException e) {
				e.printStackTrace();
			}
			isEnable = true;
			sendMessage("&2%s&a (%s) est activé.", getDescription().getName(), getDescription().getVersion());
		} catch (Error | Exception e) {
			setStatus(ServerStatus.MAINTENANCE);
			getLogger().severe(String.format("Une erreur est survenu lors de l'activation de %s. Le serveur est désormais en maintenance.", this.getClass().getSimpleName()));
			e.printStackTrace();
		}
	}

	private void setupDatabase(int... is) {
		int i1 = 0;
		if (is != null && is.length != 0)
			i1 = is[0] + 1;
		int i = i1;
		DbCredentials dbcredentials = new DbCredentials(defaultConfig.getConfig());
		database = new DbConnection(dbcredentials);
		if (database.connect()) {
			dbConnected = true;
			sendMessage("&aConnexion à la base de donnée &2" + dbcredentials.getDatabase() + "&a établie.");
		} else {
			if (i % 100 == 0)
				sendMessage("&cConnexion à la base de donnée &4" + dbcredentials.getDatabase() + "&c impossible.");
			getTask().runTaskLater("db_setup", () -> setupDatabase(i), 10, TimeUnit.SECONDS);
			dbConnected = false;
		}
	}

	private void setupRedis(int... is) {
		int i1 = 0;
		if (is != null && is.length != 0)
			i1 = is[0] + 1;
		int i = i1;
		redisAccess.connect();
		if (redisAccess.isConnected()) {
			redisConnected = true;
			registerRedisSub(redisAccess.getConnection(), new SpigotGroupChangeReceiverOnBungee(), RedisChannel.SPIGOT_CHANGE_GROUP.name());
			registerRedisSub(redisAccess.connect(), new SpigotAskServerNameReceiver(), RedisChannel.SPIGOT_ASK_SERVERNAME.name());
			registerRedisSub(redisAccess.connect(), new SpigotServerChangeStatusReceiver(), RedisChannel.SPIGOT_SERVER_CHANGE_STATUS.name());
			registerRedisSub(redisAccess.connect(), new SpigotServerSwitchReceiver(), RedisChannel.SPIGOT_PLAYER_SWITCH_SERVER.name());
			registerRedisSub(redisAccess.connect(), new SpigotOlympaPlayerReceiver(), RedisChannel.SPIGOT_SEND_OLYMPAPLAYER_TO_BUNGEE.name());
			registerRedisSub(redisAccess.connect(), new SpigotReportReceiver(), RedisChannel.SPIGOT_REPORT_SEND.name());
			registerRedisSub(redisAccess.connect(), new SiteGroupChangeReceiver(), RedisChannel.SITE_GROUP_CHANGE.name());
			registerRedisSub(redisAccess.connect(), new BungeeCommandReceiver(), RedisChannel.BUNGEE_COMMAND.name());
			registerRedisSub(redisAccess.connect(), new SpigotServerSwitchReceiver2(), RedisChannel.SPIGOT_PLAYER_SWITCH_SERVER2.name());
			registerRedisSub(redisAccess.connect(), new SpigotAskMonitorInfoReceiver(), RedisChannel.SPIGOT_ASK_SERVERINFO.name());
			RedisClass.registerBungeeSubChannels(this);
			sendMessage("&aConnexion à &2Redis&a établie.");
		} else {
			if (i % 100 == 0)
				sendMessage("&cConnexion à &4Redis&c impossible.");
			getTask().runTaskLater("redis_setup", () -> setupRedis(i), 10, TimeUnit.SECONDS);
			redisConnected = false;
		}
	}

	@Override
	public Connection getDatabase() throws SQLException {
		return database.getConnection();
	}

	@Override
	public DbConnection getDatabaseHandler() {
		return (DbConnection) database;
	}

	@Override
	public Gson getGson() {
		return GsonCustomizedObjectTypeAdapter.GSON;
	}

	@Override
	public Collection<ServerInfoAdvanced> getMonitorServers() {
		return MonitorServers.getServers().stream().map(monitorInfoBungee -> (ServerInfoAdvanced) monitorInfoBungee).collect(Collectors.toList());
	}

}
