package fr.olympa.api.provider;

import java.util.ArrayList;
import java.util.List;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisAccess {

	public static RedisAccess INSTANCE;

	public static void close() {
		if (INSTANCE != null)
			INSTANCE.closeResource();
	}

	public static RedisAccess init(String clientName) {
		return new RedisAccess(new RedisCredentials("127.0.0.1", "1rWS1Fmj7s4snEDCQgw3Mcznf8ShfrLZpPkKtstu5coV9PpDI1", 6379, clientName));
	}

	private RedisCredentials redisCredentials;
	private JedisPool pool;
	private Jedis jedis;
	private List<Jedis> allJedis = new ArrayList<>();

	public RedisAccess(RedisCredentials redisCredentials) {
		INSTANCE = this;
		this.redisCredentials = redisCredentials;
	}

	public void closeResource() {
		if (isPoolOpen()) {
			allJedis.clear();
			allJedis.forEach(j -> j.disconnect());
			pool.close();
		}
	}

	public Jedis connect() {
		jedis = newConnection();
		allJedis.add(jedis);
		return jedis;
	}

	public void disconnect() {
		allJedis.remove(jedis);
		if (!isConnected())
			return;
		jedis.disconnect();
	}

	public Jedis getConnection() {
		if (!isConnected())
			newConnection();
		return jedis;
	}

	public JedisPool getJedisPool() {
		if (!isPoolOpen())
			initJedis();
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

	public void updateClientName(String clientName) {
		redisCredentials.setClientName(clientName);
		//		allJedis.removeIf(j -> !j.isConnected());
		//		for (Jedis j : allJedis)
		//			j.clientSetname(clientName);
	}

	private Jedis newConnection() {
		Jedis jedis = getJedisPool().getResource();
		jedis.auth(redisCredentials.getPassword());
		jedis.clientSetname(redisCredentials.getClientName());
		jedis.select(1);
		return jedis;
	}
}
