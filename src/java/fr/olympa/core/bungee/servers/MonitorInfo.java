package fr.olympa.core.bungee.servers;

import fr.olympa.api.maintenance.MaintenanceStatus;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.ServerPing.Players;
import net.md_5.bungee.api.config.ServerInfo;

public class MonitorInfo {

	String serverName;
	Integer ping;
	Integer onlinePlayer;
	Integer maxPlayers;
	MaintenanceStatus status = MaintenanceStatus.UNKNOWN;
	// Float[] tpss;
	String error;
	Float tps;

	public MonitorInfo(ServerInfo server, long time, ServerPing serverPing, Throwable error) {
		serverName = server.getName();
		ping = Math.round((System.nanoTime() - time) / 1000000);
		if (error == null) {
			Players players = serverPing.getPlayers();
			onlinePlayer = players.getOnline();
			maxPlayers = players.getMax();
			String allMotd = serverPing.getDescriptionComponent().toLegacyText();
			if (allMotd.startsWith("§")) {
				allMotd = allMotd.substring(2);
			}
			String[] motd = allMotd.split(" ");
			if (motd.length >= 1) {
				status = MaintenanceStatus.get(motd[0]);
			}
			if (motd.length >= 2) {
				tps = Float.valueOf(motd[1]);
			}
			// tpss =
			// Arrays.copyOfRange(Arrays.stream(motd).map(Float::valueOf).toArray(Float[]::new),
			// 1, motd.length);
		} else {
			status = MaintenanceStatus.CLOSE;
			this.error = error.getMessage();
		}
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

	public Integer getOnlinePlayer() {
		return onlinePlayer;
	}

	public Integer getPing() {
		return ping;
	}

	public ServerInfo getServerInfo() {
		return ProxyServer.getInstance().getServers().get(serverName);
	}

	public MaintenanceStatus getStatus() {
		return status;
	}

	public Float getTps() {
		return tps;
	}

}
