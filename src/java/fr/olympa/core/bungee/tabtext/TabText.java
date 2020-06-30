package fr.olympa.core.bungee.tabtext;

import java.util.Collection;
import java.util.StringJoiner;

import fr.olympa.api.utils.ColorUtils;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.OlympaBungee;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class TabText {
	
	public static void sendAll() {
		send(ProxyServer.getInstance().getPlayers());
	}
	
	public static void send(Collection<ProxiedPlayer> collection) {
		String header = getHeader();
		String footer = getFooter();
		collection.forEach(p -> send(p, header, footer));
	}

	public static void send(ProxiedPlayer player) {
		String header = getHeader();
		String footer = getFooter();
		send(player, header, footer);
	}

	public static void send(ProxiedPlayer player, String header, String footer) {
		player.setTabHeader(TextComponent.fromLegacyText(header), TextComponent.fromLegacyText(footer));
	}

	private static String getHeader() {
		StringJoiner sj = new StringJoiner("\n");
		sj.add("&e&lOlympa &eΩ");
		sj.add("&6Versions 1.9 à 1.15");
		return ColorUtils.color(sj.toString());
		
	}

	private static String getFooter() {
		StringJoiner sj = new StringJoiner("\n");
		//		sj.add("&6Serveur &e%n");
		int onlineCount = OlympaBungee.getInstance().getProxy().getOnlineCount();
		sj.add("&e%o &6joueur%s".replace("%o", String.valueOf(onlineCount)).replace("%s", Utils.withOrWithoutS(onlineCount)));
		sj.add("");
		sj.add("&6Discord &e&n&lwww.discord.olympa.fr &7| &6Teamspeak &e&n&lts.olympa.fr");
		sj.add("");
		sj.add("&6Twitter &e&l@Olympa_fr &7| &6Site &e&n&lwww.olympa.fr");
		return ColorUtils.color(sj.toString());
	}
}
