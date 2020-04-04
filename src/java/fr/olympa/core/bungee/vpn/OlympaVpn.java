package fr.olympa.core.bungee.vpn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.md_5.bungee.api.connection.Connection;

@SuppressWarnings("deprecation")
public class OlympaVpn {

	public static boolean isVPN(Connection con) {
		String ip = con.getAddress().getAddress().getHostAddress();
		try {
			URL url = new URL("http://proxycheck.io/v2/" + ip + "?key=2i9y52-019793-1d7248-01e861&vpn=1&asn=1&node=1&time=1&inf=0&port=1&seen=1&days=7&tag=msg");
			URLConnection connection = url.openConnection(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip, con.getAddress().getPort())));
			connection.setUseCaches(false);
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getURL().openStream(), Charset.forName("UTF-8")));
			StringBuilder stringBuilder = new StringBuilder();
			String lineNotFount;
			while ((lineNotFount = in.readLine()) != null) {
				stringBuilder.append(lineNotFount + "\n");
			}
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

	long id;
	String ip;
	boolean isVpn;
	Map<String, Boolean> users = new HashMap<>();

	public OlympaVpn(long id, String ip, boolean isVpn, String usersString) {
		this.id = id;
		this.ip = ip;
		this.isVpn = isVpn;
		if (usersString != null && !usersString.isEmpty()) {
			users = Arrays.stream(usersString.split(",")).collect(Collectors.toMap(entry -> entry.split(":")[0], entry -> {
				String[] split = entry.split(":");
				if (split.length > 1) {
					return entry.split(":")[1].equals("1");
				}
				return false;
			}));
		}
	}

	public OlympaVpn(String ip, boolean isVpn) {
		this.ip = ip;
		this.isVpn = isVpn;
	}

	public void addUser(String username, boolean onlineMode) {
		users.put(username, onlineMode);
	}

	public long getId() {
		return id;
	}

	public String getIp() {
		return ip;
	}

	public Map<String, Boolean> getUsers() {
		return users;
	}

	public boolean hasUser(String username, boolean onlineMode) {
		return users.entrySet().stream().filter(entry -> entry.getKey().equalsIgnoreCase(username) && entry.getValue() == onlineMode).findFirst().isPresent();
	}

	public boolean isVpn() {
		return isVpn;
	}
}
