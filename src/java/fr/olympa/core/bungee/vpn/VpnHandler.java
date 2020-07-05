package fr.olympa.core.bungee.vpn;

import java.io.BufferedReader;
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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.md_5.bungee.api.connection.Connection;

@SuppressWarnings("deprecation")
public class VpnHandler {

	public static Cache<String, OlympaVpn> cache = CacheBuilder.newBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build();

	public static OlympaVpn get(String ip) throws SQLException {
		OlympaVpn vpn = cache.getIfPresent(ip);
		if (vpn == null) {
			vpn = VpnSql.getIpInfo(ip);
			if (vpn != null)
				cache.put(ip, vpn);
		}
		return vpn;
	}

	public static OlympaVpn getInfo(Connection con) {
		String ip = con.getAddress().getAddress().getHostAddress();
		try {
			URL url = new URL("http://ip-api.com/json/%ip?fields=17034769".replace("%ip", ip));
			URLConnection connection = url.openConnection(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip, con.getAddress().getPort())));
			connection.setUseCaches(false);
			String result = CharStreams.toString(new InputStreamReader(connection.getURL().openStream(), Charsets.UTF_8));
			System.out.println("String: " + result);
			return OlympaVpn.fromJson(result);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static boolean isVPN(Connection con) {
		String ip = con.getAddress().getAddress().getHostAddress();
		try {
			URL url = new URL("http://proxycheck.io/v2/" + ip + "?key=2i9y52-019793-1d7248-01e861&vpn=1&asn=1&node=1&time=1&inf=0&port=1&seen=1&days=7&tag=msg");
			URLConnection connection = url.openConnection(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip, con.getAddress().getPort())));
			connection.setUseCaches(false);
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getURL().openStream(), Charsets.UTF_8));
			StringBuilder stringBuilder = new StringBuilder();
			String lineNotFount;
			while ((lineNotFount = in.readLine()) != null)
				stringBuilder.append(lineNotFount + "\n");
			in.close();

			JsonParser jp = new JsonParser();
			JsonElement root = jp.parse(stringBuilder.toString());
			JsonObject rootobj = root.getAsJsonObject();
			if (rootobj.get("status").getAsString().equals("ok")) {
				JsonObject infoJson = rootobj.getAsJsonObject(ip);
				boolean isVPN = infoJson.get("proxy").getAsString().equals("yes");
				return isVPN;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

}
