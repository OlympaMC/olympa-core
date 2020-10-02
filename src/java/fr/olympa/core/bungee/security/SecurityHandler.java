package fr.olympa.core.bungee.security;

import java.util.concurrent.TimeUnit;

import fr.olympa.api.LinkSpigotBungee;

public class SecurityHandler {

	public static boolean PING_BEFORE_JOIN = false;
	public static boolean CHECK_IP = true;
	public static boolean CHECK_VPN = true;
	public static boolean ALLOW_CRACK = true;
	public static boolean ALLOW_PREMIUM = true;

	{
		LinkSpigotBungee.Provider.link.getTask().runTaskLater(() -> PING_BEFORE_JOIN = true, 1, TimeUnit.MINUTES);
	}
}
