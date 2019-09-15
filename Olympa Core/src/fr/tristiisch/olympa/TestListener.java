package fr.tristiisch.olympa;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import fr.tristiisch.olympa.api.plugin.OlympaPlugin;

public class TestListener implements Listener {

	@EventHandler
	public void onJoin(AsyncPlayerPreLoginEvent event) {
		String eventName = event.getEventName();
		OlympaPlugin.getInstance().sendMessage("EVENT : " + eventName);
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		String eventName = event.getEventName();
		OlympaPlugin.getInstance().sendMessage("EVENT : " + eventName);
	}

	@EventHandler
	public void onJoin(PlayerLoginEvent event) {
		String eventName = event.getEventName();
		OlympaPlugin.getInstance().sendMessage("EVENT : " + eventName);
	}

	@EventHandler
	public void onJoin(PlayerQuitEvent event) {
		String eventName = event.getEventName();
		OlympaPlugin.getInstance().sendMessage("EVENT : " + eventName);
	}

}