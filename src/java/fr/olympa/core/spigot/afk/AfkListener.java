package fr.olympa.core.spigot.afk;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import fr.olympa.api.utils.spigot.SpigotUtils;

public class AfkListener implements Listener {

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		AfkHandler.removeLastAction(player);
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		//		AfkHandler.updateLastAction(player, false);
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		if (SpigotUtils.isSameLocationXZ(event.getFrom(), event.getTo()))
			return;
		//		AfkHandler.updateLastAction(player, false);
	}
}
