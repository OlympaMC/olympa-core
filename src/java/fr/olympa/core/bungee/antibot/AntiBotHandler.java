package fr.olympa.core.bungee.antibot;

import fr.olympa.api.LinkSpigotBungee;

public class AntiBotHandler {

	static private boolean ENABLE = false;

	public static boolean isEnable() {
		return ENABLE;
	}

	public static void toggleEnable() {
		ENABLE = !ENABLE;
		printInfo();
	}

	public static void setEnable(boolean b) {
		if (ENABLE == b)
			return;
		printInfo();
		ENABLE = b;
	}

	private static void printInfo() {
		LinkSpigotBungee.Provider.link.sendMessage("&cANTIBOT > L'anti bot est maintenant &4%s&c.", ENABLE ? "activé" : "désactiver");
	}
}
