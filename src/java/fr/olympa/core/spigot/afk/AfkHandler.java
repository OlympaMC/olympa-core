package fr.olympa.core.spigot.afk;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;

public class AfkHandler {

	final private static Map<UUID, AfkPlayer> lastActions = new HashMap<>();

	public static void updateLastAction(Player player, boolean afk) {
		AfkPlayer afkPlayer = get(player);
		boolean oldStatue = afkPlayer.isAfk();
		if (oldStatue == afk) {
			if (afk)
				afkPlayer.launchTask(player);
			return;
		}
		if (afk)
			afkPlayer.setAfk(player);
		else
			afkPlayer.setNotAfk(player);
	}

	public static AfkPlayer get(Player player) {
		AfkPlayer afk = lastActions.get(player.getUniqueId());
		if (afk == null) {
			afk = new AfkPlayer(player);
			AfkHandler.lastActions.put(player.getUniqueId(), afk);
		}
		return afk;
	}

	public static void removeLastAction(Player player) {
		AfkPlayer afkPlayer = lastActions.remove(player.getUniqueId());
		if (afkPlayer != null)
			afkPlayer.disableTask();
	}
}
