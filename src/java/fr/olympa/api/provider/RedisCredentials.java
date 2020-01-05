package fr.olympa.api.provider;

public class RedisCredentials {

	private String ip;
	private String password;
	private int port;
	private String clientName;

	public RedisCredentials(String ip, String password, int port) {
		this.ip = ip;
		this.password = password;
		this.port = port;
		this.clientName = "Olympa_Default";
	}

	public RedisCredentials(String ip, String password, int port, String clientName) {
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
