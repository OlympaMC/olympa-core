package fr.olympa.core.common.redis;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import net.md_5.bungee.config.Configuration;
import redis.clients.jedis.Jedis;

public class RedisCredentials {

	private String ip;
	private String password;
	private int port = 6379;
	private String clientName;

	public RedisCredentials(String ip, String password, int port) {
		this.ip = ip;
		this.password = password;
		this.port = port;
		setClientName("Default");
	}

	public RedisCredentials(String ip, String password, int port, String clientName) {
		this.ip = ip;
		this.password = password;
		this.port = port;
		setClientName(clientName);
	}

	public RedisCredentials(FileConfiguration config) {
		ConfigurationSection path = config.getConfigurationSection("redis");
		if (path == null)
			return;
		ip = path.getString("host").replace("localhost", "127.0.0.1");
		password = path.getString("password");
		setClientName(path.getString("clientname"));
		int configInt = path.getInt("port");
		if (configInt != 0)
			port = configInt;
	}

	public RedisCredentials(Configuration config) {
		Configuration path = config.getSection("redis");
		ip = path.getString("host").replace("localhost", "127.0.0.1");
		password = path.getString("password");
		setClientName(path.getString("clientname"));
		int configInt = path.getInt("port");
		if (configInt != 0)
			port = configInt;
	}

	public Jedis auth(Jedis jedis) {
		jedis.auth(password);
		jedis.clientSetname(clientName);
		return jedis;
	}

	public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = "Olympa_" + clientName;
	}

	public String getIp() {
		return ip;
	}

	public int getPort() {
		return port;
	}

	public String toRedisURL() {
		return ip + ":" + port;
	}
}
