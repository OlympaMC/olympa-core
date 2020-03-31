package fr.olympa.api.provider;

import fr.olympa.core.spigot.OlympaCore;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisAccess {

	public static RedisAccess INSTANCE;

	public static void close() {
		if (INSTANCE != null) {
			INSTANCE.closeResource();
		}
	}

	public static RedisAccess init(String clientName) {
		return new RedisAccess(new RedisCredentials("127.0.0.1", "1rWS1Fmj7s4snEDCQgw3Mcznf8ShfrLZpPkKtstu5coV9PpDI1", 6379, clientName));
	}

	private RedisCredentials redisCredentials;

	private JedisPool pool;

	public RedisAccess(RedisCredentials redisCredentials) {
		INSTANCE = this;
		this.redisCredentials = redisCredentials;
	}

	public void addListenerSpigot(OlympaCore core, Jedis jedis) {
		// core.getTask().runTask(() -> jedis.subscribe(new
		// ReportRedisReceiveListener(), "ReportRedisReceive"));
	}

	public void closeResource() {
		if (pool != null) {
			pool.close();
		}
	}

	public Jedis connect() {
		Jedis jedis = getJedisPool().getResource();
		jedis.auth(redisCredentials.getPassword());
		jedis.clientSetname(redisCredentials.getClientName());
		jedis.select(1);

		return jedis;
	}

	public JedisPool getJedisPool() {
		if (pool == null || pool.isClosed()) {
			initJedis();
		}
		return pool;
	}

	public void initJedis() {
		ClassLoader previous = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(Jedis.class.getClassLoader());
		pool = new JedisPool(redisCredentials.getIp(), redisCredentials.getPort());
		Thread.currentThread().setContextClassLoader(previous);
	}
}
