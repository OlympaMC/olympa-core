package fr.olympa.api.provider;

import fr.olympa.OlympaCore;
import fr.olympa.api.task.TaskManager;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisAccess {

	public static RedisAccess INSTANCE;

	public static void close() {
		INSTANCE.getJedisPool().close();
	}

	public static void init(String clientName) {
		new RedisAccess(new RedisCredentials("127.0.0.1", "1rWS1Fmj7s4snEDCQgw3Mcznf8ShfrLZpPkKtstu5coV9PpDI1", 6379, clientName));
	}

	private RedisCredentials redisCredentials;

	private JedisPool pool;

	public RedisAccess(RedisCredentials redisCredentials) {
		INSTANCE = this;
		this.redisCredentials = redisCredentials;
		this.initJedis();
		Jedis jedis = this.connect();
		OlympaCore core = OlympaCore.getInstance();
		if (jedis.isConnected()) {

			TaskManager task = core.getTask();

			// task.runTaskAsynchronously("redis1", () -> jedis.subscribe(new
			// TestListener(), "Test"));
			// task.runTaskAsynchronously("redis2", () -> this.connect().subscribe(new
			// OlympaPlayerListener(), "OlympaPlayer"));
			// task.runTaskAsynchronously("redis3", () -> this.connect().subscribe(new
			// OlympaPlayerReceiveListener(), "OlympaPlayerReceive"));
			// task.runTaskAsynchronously("redis4", () -> this.connect().subscribe(new
			// TestListener(), "Test2"));
			core.sendMessage("&aConnexion à Redis établie");
		} else {
			core.sendMessage("&cConnexion à Redis impossible");
		}
	}

	public void closeResource() {
		this.pool.getResource().close();
	}

	public Jedis connect() {
		Jedis jedis = this.pool.getResource();
		jedis.auth(this.redisCredentials.getPassword());
		jedis.clientSetname(this.redisCredentials.getClientName());
		jedis.select(1);

		return jedis;
	}

	public JedisPool getJedisPool() {
		return this.pool;
	}

	public void initJedis() {
		ClassLoader previous = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(Jedis.class.getClassLoader());
		this.pool = new JedisPool(this.redisCredentials.getIp(), this.redisCredentials.getPort());
		Thread.currentThread().setContextClassLoader(previous);
	}
}
