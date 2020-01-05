package fr.olympa.bungee.auth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class AuthUtils {

	public static boolean isUsernamePremium(String username) throws IOException {
		URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + username);
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		String line;
		StringBuilder result = new StringBuilder();
		while ((line = in.readLine()) != null) {
			result.append(line);
		}
		return !result.toString().equals("");
	}
}
