package fr.tristiisch.olympa.core.datamanagment.sql;

public class DbCredentials {

	final private String host;
	final private String user;
	final private String password;
	final private String dbName;
	final private int port;

	public DbCredentials(final String host, final String user, final String password, final String dbName) {
		this.host = host;
		this.user = user;
		this.password = password;
		this.dbName = dbName;
		this.port = 3306;
	}

	public DbCredentials(final String host, final String user, final String password, final String dbName, final int port) {
		this.host = host;
		this.user = user;
		this.password = password;
		this.dbName = dbName;
		this.port = port;
	}

	public String getDbName() {
		return this.dbName;
	}

	public String getHost() {
		return this.host;
	}

	public String getPassword() {
		return this.password;
	}

	public int getPort() {
		return this.port;
	}

	public String getUser() {
		return this.user;
	}

	public String toURI() {
		final StringBuilder sb = new StringBuilder();
		sb.append("jdbc:mariadb://").append(this.host).append(":").append(this.port).append("/").append(this.dbName).append("?autoReconnect=true");
		return sb.toString();
	}
}
