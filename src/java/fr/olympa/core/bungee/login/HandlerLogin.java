package fr.olympa.core.bungee.login;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.md_5.bungee.api.connection.ProxiedPlayer;

public class HandlerLogin {
	
	public static List<String> command = new ArrayList<>();
	public static Map<ProxiedPlayer, Integer> timesFails = new HashMap<>();
	public static List<ProxiedPlayer> unlogged = new ArrayList<>(); // temp
	
	public static boolean isLogged(ProxiedPlayer player) {
		return !unlogged.contains(player);
	}
	
}
