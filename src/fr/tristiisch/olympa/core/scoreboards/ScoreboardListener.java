package fr.tristiisch.olympa.core.scoreboards;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import fr.tristiisch.olympa.api.customevents.OlympaPlayerLoadEvent;
import fr.tristiisch.olympa.core.groups.AsyncOlympaPlayerChangeGroupEvent;

public class ScoreboardListener implements Listener {

	@EventHandler
	public void onOlympaPlayerChangeGroup(AsyncOlympaPlayerChangeGroupEvent event) {
		// TODO BUGS
		// TaskManager.runTask(() -> ScoreboardPrefix.create(event.getOlympaPlayer()));
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onOlympaPlayerLoad(OlympaPlayerLoadEvent event) {
		Player player = event.getPlayer();
		ScoreboardPrefix.sendCurrentTeams(player);
		ScoreboardPrefix.create(event.getOlympaPlayer());
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		ScoreboardPrefix.removeFromTeam(player);
		ScoreboardPrefix.removeFromView(player);
	}
}
