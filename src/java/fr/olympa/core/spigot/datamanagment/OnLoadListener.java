package fr.olympa.core.spigot.datamanagment;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;

import fr.olympa.api.utils.ColorUtils;
import fr.olympa.core.spigot.OlympaCore;

public class OnLoadListener implements Listener {

	@EventHandler(priority = EventPriority.LOWEST)
	public void on1PlayerPreLogin(AsyncPlayerPreLoginEvent event) {
		if (OlympaCore.getInstance().getServerName().contains(":"))
			event.disallow(Result.KICK_OTHER, ColorUtils.color("&cDémarrage du serveur, merci de patienter..."));
		else
			HandlerList.unregisterAll(this);
	}
}
