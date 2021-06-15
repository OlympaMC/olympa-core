package fr.olympa.core.bungee;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.google.gson.Gson;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.bungee.command.BungeeCommandListener;
import fr.olympa.api.bungee.config.BungeeCustomConfig;
import fr.olympa.api.bungee.task.BungeeTaskManager;
import fr.olympa.api.common.chat.ColorUtils;
import fr.olympa.api.common.groups.SQLGroup;
import fr.olympa.api.common.permission.OlympaPermission;
import fr.olympa.api.common.permission.list.OlympaAPIPermissionsBungee;
import fr.olympa.api.common.permission.list.OlympaAPIPermissionsGlobal;
import fr.olympa.api.common.permission.list.OlympaCorePermissionsBungee;
import fr.olympa.api.common.plugin.OlympaBungeeInterface;
import fr.olympa.api.common.plugin.OlympaPluginInterface;
import fr.olympa.api.common.provider.AccountProviderAPI;
import fr.olympa.api.common.provider.AccountProviderGetter;
import fr.olympa.api.common.redis.RedisAccess;
import fr.olympa.api.common.redis.RedisChannel;
import fr.olympa.api.common.redis.RedisClass;
import fr.olympa.api.common.redis.RedisConnection;
import fr.olympa.api.common.server.OlympaServer;
import fr.olympa.api.common.server.ServerInfoBasic;
import fr.olympa.api.common.server.ServerStatus;
import fr.olympa.api.sql.DbConnection;
import fr.olympa.api.sql.DbCredentials;
import fr.olympa.api.sql.MySQL;
import fr.olympa.api.utils.CacheStats;
import fr.olympa.api.utils.GsonCustomizedObjectTypeAdapter;
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
import fr.olympa.core.bungee.vpn.VpnSql;
import fr.olympa.core.spigot.redis.RedisSpigotSend;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.config.Configuration;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

public class OlympaBungee extends Plugin implements LinkSpigotBungee, OlympaPluginInterface, OlympaBungeeInterface {

	private static OlympaBungee instance;

	public static OlympaBungee getInstance() {
		return instance;
	}

	protected DbConnection database = null;
	protected BungeeCustomConfig defaultConfig;
	protected BungeeCustomConfig maintConfig;
	private BungeeTaskManager task;
	private ServerStatus status;
	private String serverName = "bungee1";
	private RedisAccess redisAccess;

	@Override
	public Configuration getConfig() {
		return defaultConfig.getConfig();
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void onDisable() {
		if (task != null)
			task.cancelTaskByName("monitor_serveurs");
		//		RedisAccess.close();
		if (database != null)
			database.close();
		sendMessage("&4" + getDescription().getName() + "&c (" + getDescription().getVersion() + ") est désactivé.");
	}

	@Override
	public void onLoad() {
		instance = this;
		LinkSpigotBungee.Provider.link = this;
	}

	@Override
	public void onEnable() {
		OlympaPermission.registerPermissions(OlympaCorePermissionsBungee.class);
		OlympaPermission.registerPermissions(OlympaAPIPermissionsGlobal.class);
		OlympaPermission.registerPermissions(OlympaAPIPermissionsBungee.class);

		new RestartBungeeCommand(this).register();
		task = new BungeeTaskManager(this);
		defaultConfig = new BungeeCustomConfig(this, "config");
		defaultConfig.loadSafe();
		defaultConfig.addTask("redis_config", config -> {
			redisAccess = RedisAccess.init(config.getConfig());
			AccountProviderAPI.setRedisConnection(redisAccess);
		});
		maintConfig = new BungeeCustomConfig(this, "maintenance");
		maintConfig.loadSafe();
		status = ServerStatus.get(maintConfig.getConfig().getString("settings.status"));
		setupDatabase();
		try {
			MySQL sql = new MySQL(database);
			AccountProviderAPI.init(sql, new AccountProviderGetter(sql));
		} catch (SQLException ex) {
			sendMessage("§cUne erreur est survenue lors du chargement du MySQL.");
			ex.printStackTrace();
		}
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
		pluginManager.registerListener(this, new PlayerSwitchListener());
		pluginManager.registerListener(this, new PermissionCheckListener());

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
		new ServerSwitchCommand(this).register();
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
		new NewBungeeCommand(this).register();
		new BungeeBroadcastCommand(this).register();
		new NickCommand(this).register();
		new IpCommand(this).register();
		new CreditCommand(this).register();

		MonitorServers.init(this);
		SQLGroup.init();

		CacheStats.addCache("VPN", VpnHandler.cache);
		CacheStats.addCache("WRONG_PASSWORD", HandlerLogin.timesFails);
		CacheStats.addCache("REDIS_ASK_SERVER_OF_PLAYER", RedisSpigotSend.askPlayerServer);

		//				try {
		//					Field remoteAddressField = AbstractChannel.class.getDeclaredField("remoteAddress");
		//					remoteAddressField.setAccessible(true);
		//
		//					Field serverChild = PipelineUtils.class.getField("SERVER_CHILD");
		//					serverChild.setAccessible(true);
		//
		//					Field modifiersField = Field.class.getDeclaredField("modifiers");
		//					modifiersField.setAccessible(true);
		//					modifiersField.setInt(serverChild, serverChild.getModifiers() & ~Modifier.FINAL);
		//
		//					ChannelInitializer<Channel> bungeeChannelInitializer = PipelineUtils.SERVER_CHILD;
		//
		//					Method initChannelMethod = ChannelInitializer.class.getDeclaredMethod("initChannel", Channel.class);
		//					initChannelMethod.setAccessible(true);
		//					serverChild.set(null, new ChannelInitializer<>() {
		//						@Override
		//						protected void initChannel(Channel channel) throws Exception {
		//							initChannelMethod.invoke(bungeeChannelInitializer, channel);
		//							channel.pipeline().addAfter(PipelineUtils.TIMEOUT_HANDLER, "haproxy-decoder", new HAProxyMessageDecoder());
		//							channel.pipeline().addAfter("haproxy-decoder", "haproxy-handler", new ChannelInboundHandlerAdapter() {
		//								@Override
		//								public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		//									if (msg instanceof HAProxyMessage) {
		//										HAProxyMessage message = (HAProxyMessage) msg;
		//										remoteAddressField.set(channel, new InetSocketAddress(message.sourceAddress(), message.sourcePort()));
		//									} else
		//										super.channelRead(ctx, msg);
		//								}
		//							});
		//						}
		//					});
		//				} catch (Exception e) {
		//					getLogger().log(Level.SEVERE, e.getMessage(), e);
		//					getProxy().stop();
		//				}

		sendMessage("&2" + getDescription().getName() + "&a (" + getDescription().getVersion() + ") est activé.");
		CacheStats.addDebugMap("PERMISSION", OlympaPermission.permissions);

		try {
			BungeePackets.registerPackets();
		} catch (ReflectiveOperationException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void sendMessage(String message, Object... args) {
		getProxy().getConsole().sendMessage(TextComponent.fromLegacyText(String.format(ColorUtils.color(getPrefixConsole() + message), args)));
	}

	@Override
	public OlympaServer getOlympaServer() {
		return OlympaServer.BUNGEE;
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
		DbCredentials dbcredentials = new DbCredentials(defaultConfig.getConfig());
		database = new DbConnection(dbcredentials);
		if (database.connect())
			sendMessage("&aConnexion à la base de donnée &2" + dbcredentials.getDatabase() + "&a établie.");
		else {
			if (i % 100 == 0)
				sendMessage("&cConnexion à la base de donnée &4" + dbcredentials.getDatabase() + "&c impossible.");
			getTask().runTaskLater("db_setup", () -> setupDatabase(i), 10, TimeUnit.SECONDS);
		}
	}

	@Override
	public void registerRedisSub(JedisPubSub sub, String channel) {
		registerRedisSub(redisAccess.connect(), sub, channel);
	}

	@Override
	public RedisConnection getRedisAccess() {
		return redisAccess;
	}

	@Override
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

	private void setupRedis(int... is) {
		int i1 = 0;
		if (is != null && is.length != 0)
			i1 = is[0] + 1;
		int i = i1;
		redisAccess.connect();
		if (redisAccess.isConnected()) {
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
		}
	}

	@Override
	public ServerStatus getStatus() {
		return status;
	}

	@Override
	public void setStatus(ServerStatus status) {
		this.status = status;
	}

	@Override
	public boolean isSpigot() {
		return false;
	}

	@Override
	public Connection getDatabase() throws SQLException {
		return database.getConnection();
	}

	@Override
	public BungeeCustomConfig getDefaultConfig() {
		return defaultConfig;
	}

	public Configuration getMaintConfig() {
		return maintConfig != null ? maintConfig.getConfig() : null;
	}

	public BungeeCustomConfig getMaintCustomConfig() {
		return maintConfig;
	}

	@Override
	public String getPrefixConsole() {
		return "&f[&6" + getDescription().getName() + "&f] &e";
	}

	@Override
	public String getServerName() {
		return "bungee";
	}

	@Override
	public BungeeTaskManager getTask() {
		return task;
	}

	@Override
	public void launchAsync(Runnable run) {
		getTask().runTaskAsynchronously(run);
	}

	@Override
	public Gson getGson() {
		return GsonCustomizedObjectTypeAdapter.GSON;
	}

	@Override
	public List<String> getPlayersNames() {
		return ProxyServer.getInstance().getPlayers().stream().map(ProxiedPlayer::getDisplayName).collect(Collectors.toList());
	}

	@Override
	public boolean isServerName(String serverName) {
		return this.serverName.equals(serverName);
	}

	@Override
	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	@Override
	public Collection<ServerInfoBasic> getMonitorServers() {
		return MonitorServers.getServers().stream().map(monitorInfoBungee -> (ServerInfoBasic) monitorInfoBungee).collect(Collectors.toList());
	}
}
