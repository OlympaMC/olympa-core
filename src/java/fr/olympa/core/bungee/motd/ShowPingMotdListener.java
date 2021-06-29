package fr.olympa.core.bungee.motd;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalCause;
import com.google.gson.Gson;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.spigot.utils.ProtocolAPI;
import fr.olympa.api.utils.CacheStats;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.common.provider.AccountProvider;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

/**
 * Need to be review, cache is misused
 */
public class ShowPingMotdListener implements Listener {

	public static boolean isEnable = false;
	//	public static boolean debug = false;

	@SuppressWarnings("unchecked")
	private final Cache<String, List<PlayerPingServer>> cache = CacheBuilder.newBuilder().expireAfterWrite(60, TimeUnit.SECONDS).removalListener(removalNotif -> {
		//		System.out.println(removalNotif.getKey() + " cause " + removalNotif.getCause().name());
		if (removalNotif.getCause() != RemovalCause.REPLACED)
			print((String) removalNotif.getKey(), (List<PlayerPingServer>) removalNotif.getValue());
	}).build();

	public ShowPingMotdListener() {
		CacheStats.addCache("PING_MOTD", cache);
	}

	@EventHandler
	public void onPing(ProxyPingEvent event) {
		if (!isEnable)
			return;
		add(event.getConnection());
		/*PendingConnection connection = event.getConnection();
		InetSocketAddress virtualHost = connection.getVirtualHost();
		String ip = connection.getAddress().getAddress().getHostAddress();
		String potentialNames = null;
		try {
			potentialNames = AccountProvider.getter().getSQL().getPlayersByIp(ip).stream().map(OlympaPlayer::getName).collect(Collectors.joining(", "));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		String connectIp = null;
		String connectVirtualIp = null;
		if (virtualHost != null) {
			connectIp = virtualHost.getHostName();
			if (virtualHost.getAddress() != null)
				connectVirtualIp = virtualHost.getAddress().getHostAddress();
		}
		LinkSpigotBungee.Provider.link.sendMessage("&dPing de &5%s&d - &5%s&d (%s) -> &5%s&d - %s", potentialNames, ip, ProtocolAPI.getName(connection.getVersion()), connectIp, connectVirtualIp);
		if (debug)
			LinkSpigotBungee.Provider.link.sendMessage(new Gson().toJson(connection.getSocketAddress()));*/
	}

	public void add(PendingConnection connection) {
		String ip = connection.getAddress().getAddress().getHostAddress();
		List<PlayerPingServer> oldValues = cache.getIfPresent(ip);
		boolean isPresent = oldValues != null;
		PlayerPingServer ping = new PlayerPingServer(connection);
		if (isPresent)
			oldValues.add(ping);
		else {
			List<PlayerPingServer> newValues = new ArrayList<>();
			newValues.add(ping);
			cache.put(ip, newValues);
		}
	}

	public void print(String ip, List<PlayerPingServer> playerPingServer) {
		LinkSpigotBungee.Provider.link.launchAsync(() -> {
			StringJoiner sj = new StringJoiner(" ");
			sj.add("&5Ping");
			String potentialNames;
			try {
				potentialNames = AccountProvider.getter().getSQL().getPlayersByIp(ip).stream().map(OlympaPlayer::getName).collect(Collectors.joining(", "));
				if (!potentialNames.isBlank())
					sj.add("&d" + potentialNames + "&5");
			} catch (SQLException e) {
				e.printStackTrace();
			}
			sj.add(ip);
			String versions = playerPingServer.stream().map(PlayerPingServer::getVersion).filter(o -> o != null).distinct().collect(Collectors.joining(" | "));
			if (!versions.isBlank())
				sj.add("(&d" + versions + "&5)");
			String connectIps = playerPingServer.stream().map(PlayerPingServer::getConnectIp).filter(o -> o != null).distinct().collect(Collectors.joining(" | "));
			if (!connectIps.isBlank())
				sj.add("-> " + connectIps);
			String connectVirtualIps = playerPingServer.stream().map(PlayerPingServer::getConnectVirtualIp).filter(o -> o != null).distinct().collect(Collectors.joining(" | "));
			if (!connectVirtualIps.isBlank())
				sj.add("VirtualIP -> " + connectVirtualIps);
			String socketAdresss = playerPingServer.stream().map(PlayerPingServer::getSocketAdress).filter(o -> o != null).distinct().collect(Collectors.joining(" | "));
			if (!socketAdresss.isBlank())
				sj.add("SocketAdress -> " + socketAdresss);
			if (playerPingServer.size() > 1) {
				long time = playerPingServer.get(playerPingServer.size() - 1).getTime() - playerPingServer.get(0).getTime();
				sj.add(playerPingServer.size() + " fois en " + time + "s");
			}
			LinkSpigotBungee.Provider.link.sendMessage(sj.toString());
		});
	}

	public class PlayerPingServer {
		long time;
		String potentialNames;
		String connectIp;
		String connectVirtualIp;
		String version;
		SocketAddress socketAdress;

		public PlayerPingServer(PendingConnection connection) {
			time = Utils.getCurrentTimeInSeconds();
			version = ProtocolAPI.getName(connection.getVersion());
			InetSocketAddress virtualHost = connection.getVirtualHost();
			if (virtualHost != null) {
				connectIp = virtualHost.getHostName();
				if (virtualHost.getAddress() != null)
					connectVirtualIp = virtualHost.getAddress().getHostAddress();
			}
			socketAdress = connection.getSocketAddress();
		}

		public String getPotentialNames() {
			return potentialNames;
		}

		public String getConnectIp() {
			return connectIp;
		}

		public String getConnectVirtualIp() {
			return connectVirtualIp;
		}

		public String getVersion() {
			return version;
		}

		public String getSocketAdress() {
			String json = new Gson().toJson(socketAdress);
			if (json.length() <= 2)
				return null;
			return json;
		}

		public long getTime() {
			return time;
		}

	}
}
