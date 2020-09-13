package fr.olympa.core.bungee.vpn;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Charsets;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.io.CharStreams;
import com.google.gson.Gson;

import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.PendingConnection;

@SuppressWarnings("deprecation")
public class VpnHandler {

	public static Cache<String, OlympaVpn> cache = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES).removalListener(notification -> {
		OlympaVpn olympaVpn = (OlympaVpn) notification.getValue();
		try {
			if (olympaVpn.getId() == 0)
				VpnSql.addIp(olympaVpn);
			else
				VpnSql.saveIp(olympaVpn);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}).build();

	private static void addIfNotExist(String ip, OlympaVpn olympaVpn) {
		OlympaVpn vpn2 = cache.getIfPresent(ip);
		if (vpn2 == null)
			cache.put(ip, olympaVpn);
	}

	public static OlympaVpn get(String ip) throws SQLException {
		OlympaVpn olympaVpn = cache.getIfPresent(ip);
		if (olympaVpn == null) {
			olympaVpn = VpnSql.getIpInfo(ip);
			if (olympaVpn != null)
				cache.put(ip, olympaVpn);
		}
		return olympaVpn;
	}

	public static OlympaVpn createVpnInfo(Connection con) throws IOException {
		String ip = con.getAddress().getAddress().getHostAddress();
		URL url = new URL(String.join("http://ip-api.com/json/%s?fields=17034769", ip));
		URLConnection connection = url.openConnection(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip, con.getAddress().getPort())));
		connection.setUseCaches(false);
		String result = CharStreams.toString(new InputStreamReader(connection.getURL().openStream(), Charsets.UTF_8));
		return OlympaVpn.fromJson(result);
	}

	protected static OlympaVpn checkIP(PendingConnection connection) throws SQLException, IOException {
		String username = connection.getName();
		String ip = connection.getAddress().getAddress().getHostAddress();
		OlympaVpn olympaVpn = null;
		olympaVpn = VpnHandler.get(ip);
		if (olympaVpn == null) {
			olympaVpn = VpnHandler.createVpnInfo(connection);
			if (!olympaVpn.isOk())
				throw new NullPointerException("OlympaVpn incomplete : " + new Gson().toJson(olympaVpn));
			olympaVpn.addUser(username);
			addIfNotExist(ip, olympaVpn);
		} else if (!olympaVpn.hasUser(username))
			olympaVpn.addUser(username);
		return olympaVpn;
	}

}
