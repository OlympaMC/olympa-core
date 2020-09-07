package fr.olympa.core.bungee.servers;

import java.util.AbstractMap;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import fr.olympa.api.server.OlympaServer;
import fr.olympa.api.server.ServerStatus;
import fr.olympa.api.utils.Matcher;
import fr.olympa.api.utils.Utils;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.ServerPing.Players;
import net.md_5.bungee.api.config.ServerInfo;

public class MonitorInfo {

	private static final Pattern ID_PATTERN = Pattern.compile("\\d*$");

	public static Entry<OlympaServer, Integer> getOlympaServer(String serverName) {
		java.util.regex.Matcher matcher = ID_PATTERN.matcher(serverName);
		matcher.find();
		String id = matcher.group();
		int serverID = Utils.isEmpty(id) ? 0 : Integer.parseInt(id);
		OlympaServer olympaServer = OlympaServer.valueOf(matcher.replaceAll("").toUpperCase());
		return new AbstractMap.SimpleEntry<>(olympaServer, serverID);
	}

	private String serverName;
	private OlympaServer olympaServer;
	private int serverID;

	private Integer ping, onlinePlayers, maxPlayers, ramUsage, threads;
	private ServerStatus status = ServerStatus.UNKNOWN;
	private String error;
	private Float tps;
	private String lastVersion = "unknown";
	private String firstVersion = "unknown";

	public MonitorInfo(ServerInfo server, long time, ServerPing serverPing, Throwable error) {
		serverName = server.getName();

		Entry<OlympaServer, Integer> serverInfo = getOlympaServer(serverName);
		olympaServer = serverInfo.getKey();
		serverID = serverInfo.getValue();

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
			if (motd.length >= 2 && Matcher.isDouble(motd[1]))
				tps = Float.valueOf(motd[1]);
			if (motd.length >= 3 && Matcher.isInt(motd[2]))
				ramUsage = Integer.valueOf(motd[2]);
			if (motd.length >= 4 && Matcher.isInt(motd[3]))
				threads = Integer.valueOf(motd[3]);
			if (motd.length >= 5)
				firstVersion = motd[4];
			if (motd.length >= 6)
				lastVersion = motd[5];
		} else {
			status = ServerStatus.CLOSE;
			this.error = error.getMessage() == null ? error.getClass().getName() : error.getMessage().replaceFirst("finishConnect\\(\\.\\.\\) failed: Connection refused: .+:\\d+", "Connexion refusée");
		}
	}

	public static Pattern getIdPattern() {
		return ID_PATTERN;
	}

	public Integer getRamUsage() {
		return ramUsage;
	}

	public Integer getThreads() {
		return threads;
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

	public void setStatus(ServerStatus status) {
		this.status = status;
	}

	public Float getTps() {
		return tps;
	}

	public boolean isOpen() {
		return error == null;
	}

	public String getLastVersion() {
		return lastVersion;
	}

	public String getFirstVersion() {
		return firstVersion;
	}

	public String getRangeVersion() {
		if (firstVersion.equals(lastVersion))
			return firstVersion;
		return firstVersion + " à " + lastVersion;
	}
}
