package fr.olympa.core.common.sql;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import net.md_5.bungee.config.Configuration;

public class DbCredentials {

	private String host;
	private String user = null;
	private String password;
	private int port = 3306;
	private String database;

	public DbCredentials(FileConfiguration config) {
		ConfigurationSection path = config.getConfigurationSection("database.default");
		if (path == null)
			return;
		host = path.getString("host");
		user = path.getString("user");
		password = path.getString("password");
		database = path.getString("database");
		int configInt = path.getInt("port");
		if (configInt != 0)
			port = configInt;
	}

	public DbCredentials(Configuration config) {
		Configuration path = config.getSection("database.default");
		host = path.getString("host");
		user = path.getString("user");
		password = path.getString("password");
		database = path.getString("database");
		int configInt = path.getInt("port");
		if (configInt != 0)
			port = configInt;
	}

	public DbCredentials(String host, String user, String password) {
		this.host = host;
		this.user = user;
		this.password = password;
	}

	public DbCredentials(String host, String user, String password, String dbName, int port) {
		this.host = host;
		this.user = user;
		this.password = password;
		database = dbName;
		this.port = port;
	}

	public String getDatabase() {
		return database;
	}

	public String getHost() {
		return host;
	}

	public String getPassword() {
		return password;
	}

	public int getPort() {
		return port;
	}

	public String getUser() {
		return user;
	}

	public String toURI() {
		StringBuilder sb = new StringBuilder();
		sb.append("jdbc:mariadb://").append(host).append(":").append(port).append("/").append(database).append("?autoReconnect=true");
		return sb.toString();
	}
}
