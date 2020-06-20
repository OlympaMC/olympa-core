package fr.olympa.core.bungee.servers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.olympa.api.server.OlympaServer;
import fr.olympa.api.server.ServerStatus;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.ServerPing.Players;
import net.md_5.bungee.api.config.ServerInfo;

public class MonitorInfo {

	private static final Pattern ID_PATTERN = Pattern.compile("\\d*$");

	private String serverName;
	private OlympaServer olympaServer;
	private int serverID;

	private Integer ping;
	private Integer onlinePlayers;
	private Integer maxPlayers;
	private ServerStatus status = ServerStatus.UNKNOWN;
	private String error;
	private Float tps;

	public MonitorInfo(ServerInfo server, long time, ServerPing serverPing, Throwable error) {
		serverName = server.getName();

		Matcher matcher = ID_PATTERN.matcher(serverName);
		matcher.find();
		serverID = Integer.parseInt(matcher.group());
		olympaServer = OlympaServer.valueOf(matcher.replaceAll("").toUpperCase());

		ping = Math.round((System.nanoTime() - time) / 1000000);
		if (error == null) {
			Players players = serverPing.getPlayers();
			onlinePlayers = players.getOnline();
			maxPlayers = players.getMax();
			String allMotd = serverPing.getDescriptionComponent().toLegacyText();
			if (allMotd.startsWith("§"))
				allMotd = allMotd.substring(2);
			String[] motd = allMotd.split(" ");
			if (motd.length >= 1)
				status = ServerStatus.get(motd[0]);
			if (motd.length >= 2)
				tps = Float.valueOf(motd[1]);
		} else {
			status = ServerStatus.CLOSE;
			this.error = error.getMessage();
		}
	}

	public OlympaServer getOlympaServer() {
		return olympaServer;
	}

	public int getServerID() {
		return serverID;
	}

	public String getError() {
		return error;
	}

	public Integer getMaxPlayers() {
		return maxPlayers;
	}

	public String getName() {
		return serverName;
	}

	public Integer getOnlinePlayers() {
		return onlinePlayers;
	}

	public Integer getPing() {
		return ping;
	}

	public ServerInfo getServerInfo() {
		return ProxyServer.getInstance().getServers().get(serverName);
	}

	public ServerStatus getStatus() {
		return status;
	}

	public Float getTps() {
		return tps;
	}

	public boolean isOpen() {
		return error == null;
	}

}
