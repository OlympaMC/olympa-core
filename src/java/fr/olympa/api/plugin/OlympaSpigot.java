package fr.olympa.api.plugin;

import java.sql.Connection;
import java.sql.SQLException;

import fr.olympa.api.hook.ProtocolAction;
import fr.olympa.api.provider.RedisAccess;
import fr.olympa.api.redis.RedisTestListener;
import fr.olympa.api.server.ServerStatus;
import fr.olympa.api.sql.DbConnection;
import fr.olympa.api.sql.DbCredentials;
import fr.olympa.core.spigot.redis.GiveOlympaPlayerListener;
import fr.olympa.core.spigot.redis.GiveToOlympaPlayerListener;
import fr.olympa.core.spigot.redis.RedisSpigotSend;
import fr.olympa.core.spigot.redis.SendServerNameListener;

public abstract class OlympaSpigot extends OlympaAPIPlugin implements OlympaCoreInterface {

	protected DbConnection database = null;
	protected ServerStatus status;
	private String serverName = getServer().getIp() + ":" + getServer().getPort();

	@Override
	public Connection getDatabase() throws SQLException {
		return database.getConnection();
	}

	public abstract ProtocolAction getProtocolSupport();

	@Override
	public String getServerName() {
		return serverName;
	}

	@Override
	public ServerStatus getStatus() {
		return status;
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
		RedisAccess redisAccess = RedisAccess.init(getServerName());
		redisAccess.connect();
		if (redisAccess.isConnected()) {
			// Test
			new Thread(() -> redisAccess.newConnection().subscribe(new RedisTestListener(), "test"), "subscriberThread").start();
			new Thread(() -> redisAccess.newConnection().subscribe(new SendServerNameListener(), "sendServerName"), "subscriberThread").start();
			new Thread(() -> redisAccess.newConnection().subscribe(new GiveOlympaPlayerListener(), "giveOlympaPlayer"), "subscriberThread").start();
			new Thread(() -> redisAccess.newConnection().subscribe(new GiveToOlympaPlayerListener(), "giveToOlympaPlayer"), "subscriberThread").start();
			RedisSpigotSend.askServerName();

			sendMessage("&aConnexion à &2Redis&a établie.");
		} else {
			if (i % 100 == 0)
				sendMessage("&cConnexion à &4Redis&c impossible.");
			getTask().runTaskLater(() -> setupRedis(i), 10 * 20);
		}
	}
}
