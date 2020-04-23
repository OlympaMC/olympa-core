package fr.olympa.core.bungee.utils;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.datamanagment.DataHandler;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;

public class BungeeUtils {

	public static List<String> color(List<String> l) {
		return l.stream().map(s -> BungeeUtils.color(s)).collect(Collectors.toList());
	}

	public static String color(String s) {
		return s != null ? ChatColor.translateAlternateColorCodes('&', s) : "";
	}

	public static String connectScreen(String s) {
		Configuration config = OlympaBungee.getInstance().getConfig();
		return BungeeUtils.color(config.getString("default.connectscreenprefix") + s + config.getString("default.connectscreensuffix"));
	}

	public static TextComponent formatStringToJSON(String s) {
		TextComponent textcomponent = new TextComponent();
		BaseComponent[] msgs = TextComponent.fromLegacyText(BungeeUtils.color(s));
		for (final BaseComponent msg : msgs) {
			textcomponent.addExtra(msg);
		}
		return textcomponent;
	}

	public static String getName(UUID uuid) {
		return null;
	}

	public static Set<ProxiedPlayer> getPlayers(List<String> l) {
		return OlympaBungee.getInstance().getProxy().getPlayers().stream().filter(p -> !DataHandler.isUnlogged(p)).collect(Collectors.toSet());
	}

	public static String getPlayersNamesByIp(String ip) {
		// TODO Auto-generated method stub
		return null;
	}
}
