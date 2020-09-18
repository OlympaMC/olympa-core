package fr.olympa.api.plugin;

import java.sql.Connection;
import java.sql.SQLException;

import fr.olympa.api.hook.IProtocolSupport;
import fr.olympa.api.redis.RedisAccess;
import fr.olympa.api.redis.RedisChannel;
import fr.olympa.api.server.ServerStatus;
import fr.olympa.api.sql.DbConnection;
import fr.olympa.api.sql.DbCredentials;
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
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

public abstract class OlympaSpigot extends OlympaAPIPlugin implements OlympaCoreInterface {

	protected DbConnection database = null;
	protected ServerStatus status;
	private String serverNameIp = getServer().getIp() + ":" + getServer().getPort();
	private String serverName;

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

	public void registerRedisSub(Jedis jedis, JedisPubSub sub, String channel) {
		new Thread(() -> {
			jedis.subscribe(sub, channel);
			jedis.disconnect();
		}, "Redis sub " + channel).start();

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
			setupDatabase();
			setupRedis();
			String statusString = config.getString("status");
			if (statusString != null && !statusString.isEmpty()) {
				ServerStatus status2 = ServerStatus.get(statusString);
				if (status2 != null)
					status = status2;
			} else
				setStatus(ServerStatus.UNKNOWN);
		}
	}

	@Override
	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	@Override
	public void setStatus(ServerStatus status) {
		this.status = status;
		RedisSpigotSend.changeStatus(status);
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
		RedisAccess redisAccess = RedisAccess.init(getConfig());
		redisAccess.connect();
		if (redisAccess.isConnected()) {
			registerRedisSub(redisAccess.getConnection(), new BungeeServerNameReceiver(), RedisChannel.BUNGEE_ASK_SEND_SERVERNAME.name());
			registerRedisSub(redisAccess.connect(), new SpigotOlympaPlayerReceiver(), RedisChannel.BUNGEE_ASK_SEND_OLYMPAPLAYER.name()); // BUG blocked thread
			registerRedisSub(redisAccess.connect(), new SpigotReceiveOlympaPlayerReceiver(), RedisChannel.SPIGOT_SEND_OLYMPAPLAYER.name());
			registerRedisSub(redisAccess.connect(), new BungeeSendOlympaPlayerReceiver(), RedisChannel.BUNGEE_SEND_OLYMPAPLAYER.name());
			registerRedisSub(redisAccess.connect(), new SpigotGroupChangedReceiver(), RedisChannel.SPIGOT_CHANGE_GROUP.name());
			registerRedisSub(redisAccess.connect(), new SpigotGroupChangedReceiveReceiver(), RedisChannel.SPIGOT_CHANGE_GROUP_RECEIVE.name());
			registerRedisSub(redisAccess.connect(), new BungeeAskPlayerServerReceiver(), RedisChannel.BUNGEE_SEND_PLAYERSERVER.name());
			registerRedisSub(redisAccess.connect(), new SpigotCommandReceiver(), RedisChannel.SPIGOT_COMMAND.name());
			registerRedisSub(redisAccess.connect(), new BungeeTeamspeakIdReceiver(), RedisChannel.BUNGEE_SEND_TEAMSPEAKID.name());
			RedisSpigotSend.askServerName();
			sendMessage("&aConnexion à &2Redis&a établie.");
		} else {
			if (i % 100 == 0)
				sendMessage("&cConnexion à &4Redis&c impossible.");
			getTask().runTaskLater(() -> setupRedis(i), 10 * 20);
		}
	}
}
