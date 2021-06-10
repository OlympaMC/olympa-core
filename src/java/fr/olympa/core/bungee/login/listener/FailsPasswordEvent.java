package fr.olympa.core.bungee.login.listener;

import fr.olympa.core.bungee.login.HandlerLogin;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class FailsPasswordEvent implements Listener {
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerDisconnect(PlayerDisconnectEvent event) {
		ProxiedPlayer player = event.getPlayer();
		HandlerLogin.timesFails.invalidate(player.getAddress().getAddress().getHostAddress());
	}
}
