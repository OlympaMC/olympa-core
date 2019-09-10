package fr.tristiisch.olympa.core.datamanagment.redis;

import fr.tristiisch.olympa.core.datamanagment.redis.listeners.OlympaPlayerListener;
import fr.tristiisch.olympa.core.datamanagment.redis.listeners.OlympaPlayerReceiveListener;
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
	}

	public void closeResource() {
		this.pool.getResource().close();
	}

	public Jedis connect() {
		final Jedis jedis = this.getJedisPool().getResource();
		jedis.auth(this.getCredentials().getPassword());
		jedis.clientSetname(this.getCredentials().getClientName());
		jedis.select(1);

		jedis.subscribe(new OlympaPlayerListener(), "OlympaPlayer");
		jedis.subscribe(new OlympaPlayerReceiveListener(), "OlympaPlayerReceive");
		return jedis;
	}

	public RedisCredentials getCredentials() {
		return this.redisCredentials;
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
