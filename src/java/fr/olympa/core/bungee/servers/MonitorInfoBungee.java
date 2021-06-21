package fr.olympa.core.bungee.servers;

import java.util.Arrays;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import fr.olympa.api.common.match.RegexMatcher;
import fr.olympa.api.common.module.OlympaModule;
import fr.olympa.api.common.server.OlympaServer;
import fr.olympa.api.common.server.ServerInfoAdvanced;
import fr.olympa.api.common.server.ServerInfoAdvancedBungee;
import fr.olympa.api.common.server.ServerInfoBasic;
import fr.olympa.api.common.server.ServerStatus;
import fr.olympa.core.bungee.OlympaBungee;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.ServerPing.Players;
import net.md_5.bungee.api.config.ServerInfo;

public class MonitorInfoBungee extends ServerInfoBasic {

	ServerInfo serverInfo;
	@Nullable
	ServerInfoAdvancedBungee serverDebugInfo;

	int maxErrorSend = 10;
	int maxReadTimeException = 2;

	public MonitorInfoBungee(ServerInfo serverInfo, long time, ServerPing serverPing, Throwable error) {
		this.serverInfo = serverInfo;
		serverName = serverInfo.getName();

		Entry<OlympaServer, Integer> serverInfos = OlympaServer.getOlympaServerWithId(serverName);
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
				lastModifiedCore = RegexMatcher.INT.parse(motd[6]);
			try {
				if (motd.length >= 8) {
					String json = String.join(" ", Arrays.copyOfRange(motd, 7, motd.length));
					serverDebugInfo = ServerInfoAdvancedBungee.fromJson(json);
					serverDebugInfo.setPing(ping);
					//					serverDebugInfo.setError(this.error);
				}
				if (OlympaModule.DEBUG)
					OlympaBungee.getInstance().sendMessage("&7Réponse du serveur &2%s&7 : &d%s", serverInfo.getName(), allMotd);
			} catch (Exception | Error e) {
				OlympaBungee.getInstance().sendMessage("&4Une erreur est survenu, réponse du serveur &c%s&7 : &d%s", serverInfo.getName(), allMotd);
				if (maxErrorSend-- >= 0)
					e.printStackTrace();
				else
					OlympaBungee.getInstance().sendMessage("&c%s", e.getMessage());

			}
		} else {
			this.error = error.getMessage() == null ? error.getClass().getName() : error.getMessage().replaceFirst("finishConnect\\(\\.\\.\\) failed: Connection refused: .+:\\d+", "");
			if (this.error.isEmpty())
				status = ServerStatus.CLOSE;
			else {
				status = ServerStatus.UNKNOWN;
				if (OlympaModule.DEBUG) {
					OlympaBungee.getInstance().sendMessage("&cLe serveur &4%s&c renvoie une erreur lors du ping", serverInfo.getName());
					error.printStackTrace();
				}
			}
		}
	}

	public ServerInfo getServerInfo() {
		return serverInfo;
	}

	public ServerInfoAdvanced getServerDebugInfo() {
		return serverDebugInfo;
	}

	public void setMaxReadTimeException(int maxReadTimeException) {
		this.maxReadTimeException = maxReadTimeException;
	}
}
