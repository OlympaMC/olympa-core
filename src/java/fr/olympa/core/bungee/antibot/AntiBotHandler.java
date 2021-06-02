package fr.olympa.core.bungee.antibot;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.common.permission.list.OlympaCorePermissionsBungee;
import fr.olympa.api.common.permission.list.OlympaCorePermissionsSpigot;

public class AntiBotHandler {

	private static boolean ENABLE = false;

	public static boolean isEnable() {
		return ENABLE;
	}

	public static void toggleEnable(String source) {
		ENABLE = !ENABLE;
		printInfo(source);
	}

	public static void setEnable(boolean b, String source) {
		if (ENABLE == b)
			return;
		printInfo(source);
		ENABLE = b;
	}

	private static void printInfo(String source) {
		LinkSpigotBungee link = LinkSpigotBungee.Provider.link;
		String msg = "&cANTIBOT > L'anti bot est maintenant &4%s&c par &4%s&c.";
		Object[] arg = new String[] { ENABLE ? "activé" : "désactiver", source != null ? source : "automatisme" };
		if (link.isSpigot())
			OlympaCorePermissionsSpigot.SPIGOT_COMMAND_ANTIBOT.sendMessage(msg, arg);
		else
			OlympaCorePermissionsBungee.BUNGEE_COMMAND_ANTIBOT.sendMessage(msg, arg);
		link.sendMessage(msg, arg);
	}
}
