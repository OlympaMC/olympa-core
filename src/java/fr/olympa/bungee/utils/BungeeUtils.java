package fr.olympa.bungee.utils;

import java.util.stream.Collectors;

import fr.olympa.api.objects.OlympaGroup;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.SpigotUtils;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

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

	public static void sendMessageToStaff(TextComponent text) {
		for (ProxiedPlayer player : ProxyServer.getInstance()
				.getPlayers()
				.stream() 
				.filter(p -> {
					return AccountProvider.get(p.getUniqueId()).hasPower(OlympaGroup.BUILDER);
				})
				.collect(Collectors.toList())) {
			player.sendMessage(text);
		}
	}
}
