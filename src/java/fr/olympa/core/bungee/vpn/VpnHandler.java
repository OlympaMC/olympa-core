package fr.olympa.core.bungee.vpn;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalCause;
import com.google.common.io.CharStreams;
import com.google.gson.Gson;

import fr.olympa.api.utils.TimeEvaluator;
import fr.olympa.core.bungee.connectionqueue.QueueHandler;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.PendingConnection;

@SuppressWarnings("deprecation")
public class VpnHandler {

	private static List<String> inCheck = new ArrayList<>();
	public static Cache<String, OlympaVpn> cache = CacheBuilder.newBuilder().recordStats().expireAfterWrite(10, TimeUnit.SECONDS).removalListener(notification -> {
		OlympaVpn olympaVpn = (OlympaVpn) notification.getValue();
		//		OlympaBungee.getInstance().sendMessage("&6DEBUG VPN > notification %s id %d ip %s", notification.getCause(), olympaVpn.getId(), notification.getKey());

		if (notification.getCause() == RemovalCause.REPLACED)
			return;
		try {
			save(olympaVpn);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}).build();

	private static synchronized void save(OlympaVpn olympaVpn) throws SQLException {
		if (!olympaVpn.isUpWithDB())
			//			OlympaBungee.getInstance().sendMessage("&6DEBUG VPN > save id %d", olympaVpn.getId());
			VpnSql.saveIp(olympaVpn);
		//		else
		//			OlympaBungee.getInstance().sendMessage("&6DEBUG VPN > id %d is already up with db", olympaVpn.getId());
	}

	public static void saveAll() {
		for (OlympaVpn info : cache.asMap().values())
			try {
				save(info);
			} catch (SQLException e) {
				e.printStackTrace();
			}
	}

	private static void add(OlympaVpn olympaVpn) {
		cache.put(olympaVpn.getIp(), olympaVpn);
	}

	public static OlympaVpn get(String ip) throws SQLException {
		OlympaVpn olympaVpn = cache.getIfPresent(ip);
		if (olympaVpn == null) {
			olympaVpn = VpnSql.getIpInfo(ip);
			if (olympaVpn != null)
				add(olympaVpn);
		}
		return olympaVpn;
	}

	public static OlympaVpn createVpnInfo(Connection con, String ip, boolean addToDb) throws IOException, SQLException {
		OlympaVpn olympaVpn = null;
		URL url = new URL(String.format("http://ip-api.com/json/%s?fields=17034769", ip));
		URLConnection connection;
		TimeEvaluator time = new TimeEvaluator("VPN " + ip);
		if (con != null)
			connection = url.openConnection(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(con.getAddress().getAddress().getHostAddress(), con.getAddress().getPort())));
		else
			connection = url.openConnection();
		connection.setUseCaches(false);
		String result = CharStreams.toString(new InputStreamReader(connection.getURL().openStream(), StandardCharsets.UTF_8));
		time.print();
		olympaVpn = OlympaVpn.fromJson(result);
		if (!olympaVpn.isOk(ip))
			throw new IllegalAccessError("OlympaVpn incomplete : " + new Gson().toJson(olympaVpn));
		if (addToDb) {
			olympaVpn.setDefaultTimesIfNeeded();
			add(olympaVpn);
			olympaVpn = VpnSql.addIp(olympaVpn);
		}
		return olympaVpn;
	}

	public static OlympaVpn createVpnInfo(String ip) throws IOException, SQLException {
		return createVpnInfo(null, ip, true);
	}

	protected static OlympaVpn checkIP(PendingConnection connection) throws SQLException, IOException, InterruptedException {
		String username = connection.getName();
		String ip = connection.getAddress().getAddress().getHostAddress();
		if (ip.equalsIgnoreCase("127.0.0.1"))
			return null;
		OlympaVpn olympaVpn = null;
		if (inCheck.contains(ip)) {
			while (inCheck.contains(ip))
				Thread.sleep(500L);
			olympaVpn = cache.getIfPresent(ip);
		}
		if (olympaVpn == null) {
			inCheck.add(ip);
			olympaVpn = VpnHandler.get(ip);
			if (olympaVpn == null) {
				if (QueueHandler.hasTooManyInQueue()) {
					inCheck.remove(ip);
					return null;
				}
				try {
					olympaVpn = VpnHandler.createVpnInfo(connection, ip, true);
					olympaVpn.setDefaultTimesIfNeeded();
				} catch (Exception | NoClassDefFoundError e) {
					inCheck.remove(ip);
					throw e;
				}
			} else if (olympaVpn.isOutDate())
				try {
					olympaVpn.update(VpnHandler.createVpnInfo(connection, ip, false));
					VpnSql.saveIp(olympaVpn);
				} catch (Exception | NoClassDefFoundError e) {
					inCheck.remove(ip);
					throw e;
				}
			inCheck.remove(ip);
		}
		olympaVpn.addUser(username);
		return olympaVpn;
	}

}
