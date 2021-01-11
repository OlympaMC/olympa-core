package fr.olympa.core.bungee.vpn;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.io.CharStreams;
import com.google.gson.Gson;

import fr.olympa.core.bungee.connectionqueue.QueueHandler;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.PendingConnection;

@SuppressWarnings("deprecation")
public class VpnHandler {

	private static Cache<String, OlympaVpn> cache = CacheBuilder.newBuilder().expireAfterAccess(30, TimeUnit.MINUTES).maximumSize(100).removalListener(notification -> {
		OlympaVpn olympaVpn = (OlympaVpn) notification.getValue();
		try {
			if (olympaVpn.getId() == 0)
				try {
					VpnSql.addIp(olympaVpn);
				} catch (SQLIntegrityConstraintViolationException e) {
					VpnSql.saveIp(olympaVpn); // bad fix
				}
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
		URL url = new URL(String.format("http://ip-api.com/json/%s?fields=17034769", ip));
		URLConnection connection = url.openConnection(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip, con.getAddress().getPort())));
		connection.setUseCaches(false);
		String result = CharStreams.toString(new InputStreamReader(connection.getURL().openStream(), StandardCharsets.UTF_8));
		return OlympaVpn.fromJson(result);
	}

	protected static OlympaVpn checkIP(PendingConnection connection) throws SQLException, IOException {
		String username = connection.getName();
		String ip = connection.getAddress().getAddress().getHostAddress();
		OlympaVpn olympaVpn = null;
		olympaVpn = VpnHandler.get(ip);
		if (olympaVpn == null) {
			if (!QueueHandler.hasTooManyInQueue())
				return null;
			olympaVpn = VpnHandler.createVpnInfo(connection);
			if (!olympaVpn.isOk())
				throw new NullPointerException("OlympaVpn incomplete : " + new Gson().toJson(olympaVpn));
			if (username != null)
				olympaVpn.addUser(username);
			addIfNotExist(ip, olympaVpn);
		} else if (username != null && !olympaVpn.hasUser(username))
			olympaVpn.addUser(username);
		return olympaVpn;
	}

}
