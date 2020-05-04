package fr.olympa.core.spigot.scoreboards.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;

import fr.olympa.core.spigot.scoreboards.packets.VersionChecker;

public class UtilsScoreboard {

	public static String deformat(String input) {
		return input.replace("§", "&");
	}

	public static String format(String input) {
		return format(input, false);
	}

	public static String format(String input, boolean limitChars) {
		String colored = ChatColor.translateAlternateColorCodes('&', input);

		switch (VersionChecker.getBukkitVersion()) {
		case v1_13_R1:
		case v1_14_R1:
		case v1_14_R2:
		case v1_15_R1:
		case v1_15_R2:
			return limitChars && colored.length() > 128 ? colored.substring(0, 128) : colored;
		default:
			return limitChars && colored.length() > 16 ? colored.substring(0, 16) : colored;
		}
	}

	public static String format(String[] text, int to, int from) {
		return StringUtils.join(text, ' ', to, from).replace("'", "");
	}

	public static String generateUUID() {
		String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < 5; i++) {
			builder.append(chars.charAt((int) (Math.random() * chars.length())));
		}
		return builder.toString();
	}

	public static List<Player> getOnline() {
		List<Player> list = new ArrayList<>();

		for (World world : Bukkit.getWorlds()) {
			list.addAll(world.getPlayers());
		}

		return Collections.unmodifiableList(list);
	}

}
