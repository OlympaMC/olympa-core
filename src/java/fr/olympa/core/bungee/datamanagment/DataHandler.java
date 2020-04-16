package fr.olympa.core.bungee.datamanagment;

import java.util.HashSet;
import java.util.Set;

public class DataHandler {

	static Set<CachePlayer> players = new HashSet<>();

	public static void addPlayer(CachePlayer player) {
		DataHandler.players.add(player);
	}

	public static CachePlayer get(String name) {
		return players.stream().filter(p -> p.getName() != null && name.equals(p.getName())).findFirst().orElse(null);
	}

	public static Set<CachePlayer> getPlayers() {
		return players;
	}

	public static void removePlayer(CachePlayer player) {
		if (player != null) {
			DataHandler.players.remove(player);
		}
	}

	public static void removePlayer(String name) {
		removePlayer(get(name));
	}
}
