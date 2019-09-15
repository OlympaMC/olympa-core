package fr.tristiisch.olympa.core.permission.groups.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import fr.tristiisch.olympa.api.customevents.AsyncOlympaPlayerLoadEvent;
import fr.tristiisch.olympa.core.permission.groups.customevents.AsyncOlympaPlayerChangeGroupEvent;
import fr.tristiisch.olympa.core.permission.scoreboard.ScoreboardList;

public class PlayerUpdateListener implements Listener {

	@EventHandler
	public void onAsyncOlympaPlayerLoad(AsyncOlympaPlayerLoadEvent event) {
		new ScoreboardList(event.getPlayer(), event.getOlympaPlayer().getGroup());
	}

	@EventHandler
	public void onOlympaPlayerChangeGroup(AsyncOlympaPlayerChangeGroupEvent event) {
		new ScoreboardList(event.getPlayer(), event.getOlympaPlayer().getGroup());
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		ScoreboardList.deleteTeam(event.getPlayer());
	}
}
