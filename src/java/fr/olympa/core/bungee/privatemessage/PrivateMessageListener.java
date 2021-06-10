package fr.olympa.core.bungee.privatemessage;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PrivateMessageListener implements Listener {

	@EventHandler
	public void PlayerDisconnectEvent(PlayerDisconnectEvent event) {
		ProxiedPlayer player = event.getPlayer();
		PrivateMessage.delReply(player);
		PrivateMessageToggleCommand.players.remove(player.getUniqueId());
	}

}
