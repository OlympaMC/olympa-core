package fr.olympa.core.spigot.scoreboards;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import fr.olympa.api.customevents.OlympaPlayerLoadEvent;
import fr.olympa.api.groups.AsyncOlympaPlayerChangeGroupEvent;

public class ScoreboardListener implements Listener {

	@EventHandler
	public void onOlympaPlayerChangeGroup(AsyncOlympaPlayerChangeGroupEvent event) {
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onOlympaPlayerLoad(OlympaPlayerLoadEvent event) {
		Player player = event.getPlayer();
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerQuit(PlayerQuitEvent event) {

	}
}
