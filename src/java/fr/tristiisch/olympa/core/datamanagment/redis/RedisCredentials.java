package fr.tristiisch.olympa.core.datamanagment.redis;

public class RedisCredentials {

	final private String ip;
	final private String password;
	final private int port;
	final private String clientName;

	public RedisCredentials(final String ip, final String password, final int port) {
		this.ip = ip;
		this.password = password;
		this.port = port;
		this.clientName = "Olympa_Default";
	}

	public RedisCredentials(final String ip, final String password, final int port, final String clientName) {
		this.ip = ip;
		this.password = password;
		this.port = port;
		this.clientName = "Olympa_" + clientName;
	}

	public String getClientName() {
		return this.clientName;
	}

	public String getIp() {
		return this.ip;
	}

	public String getPassword() {
		return this.password;
	}

	public int getPort() {
		return this.port;
	}

	public String toRedisURL() {
		return this.ip + ":" + this.port;
	}
}
