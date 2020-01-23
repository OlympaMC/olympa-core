package fr.olympa.core.bungee.datamanagment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.UUID;

import net.md_5.bungee.api.connection.PendingConnection;

@SuppressWarnings("deprecation")
public class AuthUtils {

	public static UUID getUuid(PendingConnection con) {
		try {
			URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + con.getName());
			URLConnection connection = url.openConnection(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(con.getAddress().getAddress().getHostAddress(), con.getAddress().getPort())));
			connection.setUseCaches(false);
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getURL().openStream(), Charset.forName("UTF-8")));
			StringBuilder stringBuilder = new StringBuilder();
			String lineNotFount;
			while ((lineNotFount = in.readLine()) != null) {
				stringBuilder.append(lineNotFount + "\n");
			}
			in.close();
			if (stringBuilder.toString().isEmpty()) {
				return null;
			}
			return UUID.fromString(
					stringBuilder.toString().replace("{", "").replace("}", "").replace("\"", "").split(",")[0].split(":")[1].replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5"));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

}
