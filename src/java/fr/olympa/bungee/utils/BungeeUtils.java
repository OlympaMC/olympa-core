package fr.olympa.bungee.utils;

import java.util.UUID;

import fr.olympa.api.utils.SpigotUtils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

public class BungeeUtils {

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
