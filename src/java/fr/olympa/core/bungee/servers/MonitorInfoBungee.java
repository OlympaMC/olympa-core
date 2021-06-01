package fr.olympa.core.bungee.servers;

import java.util.Arrays;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import fr.olympa.api.match.RegexMatcher;
import fr.olympa.api.module.OlympaModule;
import fr.olympa.api.server.MonitorInfo;
import fr.olympa.api.server.OlympaServer;
import fr.olympa.api.server.ServerDebug;
import fr.olympa.api.server.ServerStatus;
import fr.olympa.core.bungee.OlympaBungee;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.ServerPing.Players;
import net.md_5.bungee.api.config.ServerInfo;

public class MonitorInfoBungee extends MonitorInfo {

	ServerInfo serverInfo;
	@Nullable
	ServerDebug serverDebugInfo;

	public MonitorInfoBungee(ServerInfo serverInfo, long time, ServerPing serverPing, Throwable error) {
		this.serverInfo = serverInfo;
		serverName = serverInfo.getName();

		Entry<OlympaServer, Integer> serverInfos = getOlympaServer(serverName);
		olympaServer = serverInfos.getKey();
		serverID = serverInfos.getValue();

		ping = Math.round((System.nanoTime() - time) / 1000000f);
		if (error == null) {
			String allMotd = serverPing.getDescriptionComponent().toPlainText();
			Players players = serverPing.getPlayers();
			onlinePlayers = players.getOnline();
			maxPlayers = players.getMax();
			if (allMotd.startsWith("§"))
				allMotd = allMotd.substring(2);
			String[] motd = allMotd.split(" ");
			if (motd.length >= 1)
				status = ServerStatus.get(motd[0]);
			if (motd.length >= 2 && RegexMatcher.FLOAT.is(motd[1]))
				tps = RegexMatcher.FLOAT.parse(motd[1]);
			if (motd.length >= 3 && RegexMatcher.INT.is(motd[2]))
				ramUsage = RegexMatcher.INT.parse(motd[2]);
			if (motd.length >= 4) {
				String[] threadsWithAllThreads = motd[3].split("/");
				threads = RegexMatcher.INT.parse(threadsWithAllThreads[0]);
				if (threadsWithAllThreads.length > 1)
					allThreads = RegexMatcher.INT.parse(threadsWithAllThreads[1]);
			}
			if (motd.length >= 5)
				firstVersion = motd[4];
			if (motd.length >= 6)
				lastVersion = motd[5];
			if (motd.length >= 7)
				lastModifiedCore = motd[6];
			try {
				String json = String.join(" ", Arrays.copyOfRange(motd, 7, motd.length));
				if (motd.length >= 8)
					serverDebugInfo = ServerDebug.fromJson(json);
			} catch (Error e) {
				e.printStackTrace();
			}
		} else {
			status = ServerStatus.UNKNOWN;
			this.error = error.getMessage() == null ? error.getClass().getName() : error.getMessage().replaceFirst("finishConnect\\(\\.\\.\\) failed: Connection refused: .+:\\d+", "");
			if (OlympaModule.DEBUG && !this.error.isEmpty())
				OlympaBungee.getInstance().sendMessage("&cLe serveur &4%s&c renvoie une erreur lors du ping %s", serverInfo.getName(), this.error);
		}
	}

	public ServerInfo getServerInfo() {
		return serverInfo;
	}

	public ServerDebug getServerDebugInfo() {
		return serverDebugInfo;
	}
}
