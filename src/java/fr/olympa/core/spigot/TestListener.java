package fr.olympa.core.spigot;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class TestListener implements Listener {

	@EventHandler
	public void onPreLogin(AsyncPlayerPreLoginEvent event) {
		String eventName = event.getEventName();
		OlympaCore.getInstance().sendMessage("EVENT : " + eventName + " §6(" + event.getName() + ")");
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		String eventName = event.getEventName();
		OlympaCore.getInstance().sendMessage("EVENT : " + eventName + " §6(" + event.getPlayer().getName() + ")");
	}

}
