package fr.olympa.core.spigot.scoreboards.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class UtilsScoreboard {

	public static String deformat(String input) {
		return input.replace("§", "&");
	}

	public static String format(String[] text, int to, int from) {
		return StringUtils.join(text, ' ', to, from).replace("'", "");
	}

	public static List<Player> getOnline() {
		List<Player> list = new ArrayList<>();

		for (World world : Bukkit.getWorlds()) {
			list.addAll(world.getPlayers());
		}

		return Collections.unmodifiableList(list);
	}

}
