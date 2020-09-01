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
		setClientName("Default");
	}

	public RedisCredentials(String ip, String password, int port, String clientName) {
		this.ip = ip;
		this.password = password;
		this.port = port;
		setClientName(clientName);
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

	public String getPassword() {
		return password;
	}

	public int getPort() {
		return port;
	}

	public String toRedisURL() {
		return ip + ":" + port;
	}
}
