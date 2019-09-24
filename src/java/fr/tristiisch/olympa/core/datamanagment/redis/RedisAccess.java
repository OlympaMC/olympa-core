package fr.tristiisch.olympa.core.datamanagment.redis;

import fr.tristiisch.olympa.api.plugin.OlympaPlugin;
import fr.tristiisch.olympa.api.task.TaskManager;
import fr.tristiisch.olympa.core.datamanagment.redis.listeners.OlympaPlayerListener;
import fr.tristiisch.olympa.core.datamanagment.redis.listeners.OlympaPlayerReceiveListener;
import fr.tristiisch.olympa.core.datamanagment.redis.listeners.TestListener;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisAccess {

	public static RedisAccess INSTANCE;

	public static void close() {
		INSTANCE.getJedisPool().close();
	}

	public static void init(String clientName) {
		new RedisAccess(new RedisCredentials("127.0.0.1", "Qfr0HgyhGqX9T94BOMNLG3PI7o65JKyh", 6379, clientName));
	}

	private final RedisCredentials redisCredentials;

	private JedisPool pool;

	public RedisAccess(final RedisCredentials redisCredentials) {
		INSTANCE = this;
		this.redisCredentials = redisCredentials;
		this.initJedis();
		Jedis jedis = this.connect();
		if (jedis.isConnected()) {

			TaskManager.runTaskAsynchronously("redis1", () -> jedis.subscribe(new TestListener(), "Test"));
			TaskManager.runTaskAsynchronously("redis2", () -> this.connect().subscribe(new OlympaPlayerListener(), "OlympaPlayer"));
			TaskManager.runTaskAsynchronously("redis3", () -> this.connect().subscribe(new OlympaPlayerReceiveListener(), "OlympaPlayerReceive"));
			TaskManager.runTaskAsynchronously("redis4", () -> this.connect().subscribe(new TestListener(), "Test2"));
			OlympaPlugin.getInstance().sendMessage("&aConnexion à Redis établie");
		} else {
			OlympaPlugin.getInstance().sendMessage("&cConnexion à Redis impossible");
		}
	}

	public void closeResource() {
		this.pool.getResource().close();
	}

	public Jedis connect() {
		final Jedis jedis = this.pool.getResource();
		jedis.auth(this.redisCredentials.getPassword());
		jedis.clientSetname(this.redisCredentials.getClientName());
		jedis.select(1);

		return jedis;
	}

	public JedisPool getJedisPool() {
		return this.pool;
	}

	public void initJedis() {
		final ClassLoader previous = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(Jedis.class.getClassLoader());
		this.pool = new JedisPool(this.redisCredentials.getIp(), this.redisCredentials.getPort());
		Thread.currentThread().setContextClassLoader(previous);
	}
}
