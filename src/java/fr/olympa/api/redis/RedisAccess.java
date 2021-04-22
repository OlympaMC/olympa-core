package fr.olympa.api.redis;

import org.bukkit.configuration.file.FileConfiguration;

import net.md_5.bungee.config.Configuration;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisAccess {

	public static RedisAccess INSTANCE;

	public static void close() {
		if (INSTANCE != null)
			INSTANCE.disconnect();
	}

	public static RedisAccess init(String clientName) {
		return new RedisAccess(new RedisCredentials("127.0.0.1", "1rWS1Fmj7s4snEDCQgw3Mcznf8ShfrLZpPkKtstu5coV9PpDI1", 6379, clientName));
	}

	public static RedisAccess init(FileConfiguration config) {
		return new RedisAccess(new RedisCredentials(config));
	}

	public static RedisAccess init(Configuration config) {
		return new RedisAccess(new RedisCredentials(config));
	}

	private RedisCredentials redisCredentials;
	private JedisPool pool;
	private Jedis jedis;
	//	private List<Jedis> allJedis = new ArrayList<>();

	public RedisAccess(RedisCredentials redisCredentials) {
		INSTANCE = this;
		this.redisCredentials = redisCredentials;
	}

	public Jedis connect() {
		jedis = newConnection();
		//		allJedis.add(jedis);
		return jedis;
	}

	public void disconnect() {
		//		allJedis.remove(jedis);
		if (isPoolOpen())
			pool.close();
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
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxTotal(20); // le Saint-Graal ? par défaut à 8
		config.setMaxIdle(12);
		pool = new JedisPool(config, redisCredentials.getIp(), redisCredentials.getPort());
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
