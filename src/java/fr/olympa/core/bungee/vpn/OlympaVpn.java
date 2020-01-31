package fr.olympa.core.bungee.vpn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import fr.olympa.api.objects.OlympaPlayer;
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
	List<Long> users = new ArrayList<>();

	public OlympaVpn(long id, String ip, boolean isVpn, String usersString) {
		this.id = id;
		this.ip = ip;
		this.isVpn = isVpn;
		if (usersString != null && !usersString.isEmpty()) {
			this.users = Arrays.stream(usersString.split(",")).map(s -> Long.parseLong(s)).collect(Collectors.toList());
		}
	}

	public OlympaVpn(String ip, boolean isVpn) {
		this.ip = ip;
		this.isVpn = isVpn;
	}

	public void addUser(OlympaPlayer olympaPlayer) {
		this.users.add(olympaPlayer.getId());
	}

	public long getId() {
		return this.id;
	}

	public String getIp() {
		return this.ip;
	}

	public List<Long> getUsers() {
		return this.users;
	}

	public boolean hasUser(long id) {
		return this.users.contains(id);
	}

	public boolean hasUser(OlympaPlayer olympaPlayer) {
		return this.hasUser(olympaPlayer.getId());
	}

	public boolean isVpn() {
		return this.isVpn;
	}
}
