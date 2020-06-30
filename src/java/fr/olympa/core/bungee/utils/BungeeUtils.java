package fr.olympa.core.bungee.utils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
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
		for (final BaseComponent msg : msgs)
			textcomponent.addExtra(msg);
		return textcomponent;
	}

	public static String getName(UUID uuid) {
		return null;
	}

	public static Set<ProxiedPlayer> getPlayers(List<String> l) {
		return OlympaBungee.getInstance().getProxy().getPlayers().stream().filter(p -> !DataHandler.isUnlogged(p)).collect(Collectors.toSet());
	}

	public static void getPlayers(OlympaPermission permission, Consumer<? super Set<ProxiedPlayer>> success, Consumer<? super Set<ProxiedPlayer>> noPerm) {
		Set<ProxiedPlayer> playersWithNoPerm = new HashSet<>();
		Set<ProxiedPlayer> playersWithPerm = new HashSet<>();
		OlympaBungee.getInstance().getProxy().getPlayers().stream().forEach(p -> {
			OlympaPlayer op = new AccountProvider(p.getUniqueId()).getFromRedis();
			if (op != null && permission.hasPermission(op))
				playersWithPerm.add(p);
			else
				playersWithNoPerm.add(p);
		});
		if (!playersWithPerm.isEmpty() && success != null)
			success.accept(playersWithPerm);
		if (!playersWithNoPerm.isEmpty() && noPerm != null)
			noPerm.accept(playersWithPerm);
	}
}
