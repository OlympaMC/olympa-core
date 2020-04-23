package fr.olympa.api.provider;

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
	private Jedis jedis;

	public RedisAccess(RedisCredentials redisCredentials) {
		INSTANCE = this;
		this.redisCredentials = redisCredentials;
	}

	public void closeResource() {
		if (isPoolOpen()) {
			if (isConnected()) {
				jedis.disconnect();
			}
			pool.close();
		}
	}

	public Jedis connect() {
		jedis = newConnection();
		return jedis;
	}

	public Jedis getConnection() {
		if (!isConnected()) {
			connect();
		}
		return jedis;
	}

	public JedisPool getJedisPool() {
		if (!isPoolOpen()) {
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

	public boolean isConnected() {
		return jedis != null && jedis.isConnected();
	}

	public boolean isPoolOpen() {
		return pool != null && !pool.isClosed();
	}

	public Jedis newConnection() {
		Jedis jedis = getJedisPool().getResource();
		jedis.auth(redisCredentials.getPassword());
		jedis.clientSetname(redisCredentials.getClientName());
		jedis.select(1);
		return jedis;
	}
}
