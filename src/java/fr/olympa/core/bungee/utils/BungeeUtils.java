package fr.olympa.core.bungee.utils;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import fr.olympa.api.utils.SpigotUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

public class BungeeUtils {

	public static List<String> color(List<String> l) {
		return l.stream().map(s -> SpigotUtils.color(s)).collect(Collectors.toList());
	}

	public static String color(String s) {
		return s != null ? ChatColor.translateAlternateColorCodes('&', s) : "";
	}

	public static String connectScreen(String s) {
		return SpigotUtils.color(BungeeConfigUtils.getString("default.connectscreenprefix") + s + BungeeConfigUtils.getString("default.connectscreensuffix"));
	}

	public static TextComponent formatStringToJSON(String s) {
		TextComponent textcomponent = new TextComponent();
		BaseComponent[] msgs = TextComponent.fromLegacyText(SpigotUtils.color(s));
		for (final BaseComponent msg : msgs) {
			textcomponent.addExtra(msg);
		}
		return textcomponent;
	}

	public static String getName(UUID uuid) {
		return null;
	}

	public static String getPlayersNamesByIp(String ip) {
		// TODO Auto-generated method stub
		return null;
	}
}
