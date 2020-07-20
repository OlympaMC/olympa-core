package fr.olympa.api.plugin;

import java.sql.Connection;
import java.sql.SQLException;

import fr.olympa.api.hook.IProtocolSupport;
import fr.olympa.api.provider.RedisAccess;
import fr.olympa.api.redis.RedisChannel;
import fr.olympa.api.server.ServerStatus;
import fr.olympa.api.sql.DbConnection;
import fr.olympa.api.sql.DbCredentials;
import fr.olympa.core.spigot.redis.RedisSpigotSend;
import fr.olympa.core.spigot.redis.receiver.BungeeSendOlympaPlayerReceiver;
import fr.olympa.core.spigot.redis.receiver.BungeeServerNameReceiver;
import fr.olympa.core.spigot.redis.receiver.SpigotGroupChangedReceiveReceiver;
import fr.olympa.core.spigot.redis.receiver.SpigotGroupChangedReceiver;
import fr.olympa.core.spigot.redis.receiver.SpigotReceiveOlympaPlayerReceiver;
import redis.clients.jedis.JedisPubSub;

public abstract class OlympaSpigot extends OlympaAPIPlugin implements OlympaCoreInterface {

	protected DbConnection database = null;
	protected ServerStatus status;
	private String serverNameIp = getServer().getIp() + ":" + getServer().getPort();
	private String serverName;
	private RedisAccess redisAccess;

	@Override
	public Connection getDatabase() throws SQLException {
		return database.getConnection();
	}

	public abstract IProtocolSupport getProtocolSupport();

	@Override
	public String getServerName() {
		return serverName != null ? serverName : serverNameIp;
	}

	@Override
	public boolean isServerName(String serverName) {
		if (this.serverName != null)
			return this.serverName.equalsIgnoreCase(serverName) || serverName.equalsIgnoreCase(serverNameIp);
		return serverName.equalsIgnoreCase(serverNameIp);
	}

	@Override
	public ServerStatus getStatus() {
		return status;
	}

	public void registerRedisSub(JedisPubSub sub, String channel) {
		new Thread(() -> redisAccess.newConnection().subscribe(sub, channel), "Redis sub " + channel).start();
	}

	@Override
	public void onDisable() {
		super.onDisable();
		if (database != null)
			database.close();
	}

	@Override
	public void onEnable() {
		super.onEnable();
		if (config != null) {
			String statusString = config.getString("status");
			if (statusString != null && !statusString.isEmpty()) {
				ServerStatus status2 = ServerStatus.get(statusString);
				if (status2 != null)
					setStatus(status2);
			}
			setupDatabase();
			setupRedis();
		}
	}

	@Override
	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	@Override
	public void setStatus(ServerStatus status) {
		this.status = status;
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
			getTask().runTaskLater(() -> setupDatabase(i), 10 * 20);
		}
	}

	private void setupRedis(int... is) {
		int i1 = 0;
		if (is != null && is.length != 0)
			i1 = is[0] + 1;
		int i = i1;
		redisAccess = RedisAccess.init(getServerName());
		redisAccess.connect();
		if (redisAccess.isConnected()) {
			registerRedisSub(new BungeeServerNameReceiver(), RedisChannel.BUNGEE_SEND_SERVERNAME.name());
			// BUG blocked thread
			//			registerRedisSub(new SpigotSendOlympaPlayerReceiver(), RedisChannel.BUNGEE_ASK_SEND_OLYMPAPLAYER.name());
			registerRedisSub(new SpigotReceiveOlympaPlayerReceiver(), RedisChannel.SPIGOT_SEND_OLYMPAPLAYER.name());
			registerRedisSub(new BungeeSendOlympaPlayerReceiver(), RedisChannel.BUNGEE_SEND_OLYMPAPLAYER.name());
			registerRedisSub(new SpigotGroupChangedReceiver(), RedisChannel.SPIGOT_CHANGE_GROUP.name());
			registerRedisSub(new SpigotGroupChangedReceiveReceiver(), RedisChannel.SPIGOT_CHANGE_GROUP_RECEIVE.name());
			RedisSpigotSend.askServerName();

			sendMessage("&aConnexion à &2Redis&a établie.");
		} else {
			if (i % 100 == 0)
				sendMessage("&cConnexion à &4Redis&c impossible.");
			getTask().runTaskLater(() -> setupRedis(i), 10 * 20);
		}
	}
}
