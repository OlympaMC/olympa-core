package fr.olympa.core.spigot.afk;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalCause;

public class AfkHandler {

	final private static Map<UUID, AfkPlayer> afks = new HashMap<>();

	private static Cache<UUID, AfkPlayer> lastActions = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.SECONDS).removalListener(entry -> {
		if (!entry.getCause().equals(RemovalCause.EXPIRED))
			return;
		UUID uuid = (UUID) entry.getKey();
		AfkPlayer afkPlayer = (AfkPlayer) entry.getValue();
		Player player = Bukkit.getPlayer(uuid);
		if (!player.isOnline())
			return;
		afkPlayer.setAfk(player);
		afks.put(uuid, afkPlayer);
	}).build();

	public static void updateLastAction(Player player, boolean afk, String lastAction) {
		AfkPlayer afkPlayer = get(player);
		boolean oldStatue = afkPlayer.isAfk();
		afkPlayer.update(afk, lastAction);
		UUID uuid = player.getUniqueId();
		if (afk) {
			if (oldStatue != afk)
				afkPlayer.setAfk(player);
			AfkHandler.afks.put(uuid, afkPlayer);
			AfkHandler.lastActions.invalidate(uuid);
		} else {
			if (oldStatue != afk)
				afkPlayer.setNotAfk(player);
			AfkHandler.afks.remove(uuid);
			lastActions.put(uuid, afkPlayer);
		}
	}

	public static void removeLastAction(Player player) {
		AfkHandler.lastActions.invalidate(player.getUniqueId());
	}

	public static AfkPlayer get(Player player) {
		AfkPlayer afk = afks.get(player.getUniqueId());
		if (afk == null) {
			afk = lastActions.getIfPresent(player.getUniqueId());
			if (afk == null) {
				afk = new AfkPlayer();
				AfkHandler.lastActions.put(player.getUniqueId(), afk);
			}
		}
		return afk;
	}
}
