package fr.olympa.core.bungee.privatemessage;

import java.util.List;
import java.util.stream.Collectors;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.TabCompleteEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PrivateMessageListener implements Listener {

	@EventHandler
	public void onTabComplete(TabCompleteEvent event) {
		String cursor = event.getCursor();
		if (cursor.lastIndexOf("/") == -1) {
			return;
		}
		String command = event.getCursor().substring(cursor.lastIndexOf("/"), cursor.length()).toLowerCase().replace(" ", "");

		if (!PrivateMessage.privateMessageCommand.contains(command)) {
			return;
		}

		String probabilityPlayerName = cursor.toLowerCase();

		int lastSpaceIndex = probabilityPlayerName.lastIndexOf(' ');
		if (lastSpaceIndex >= 0) {
			probabilityPlayerName = probabilityPlayerName.substring(lastSpaceIndex + 1);
		}

		String probabilityPlayerName2 = probabilityPlayerName;
		List<String> players = ProxyServer.getInstance().getPlayers().stream().filter(p -> p.getName().toLowerCase().startsWith(probabilityPlayerName2)).map(ProxiedPlayer::getName).collect(Collectors.toList());

		if (!players.isEmpty()) {
			event.getSuggestions().addAll(players);
		}
	}

	@EventHandler
	public void PlayerDisconnectEvent(PlayerDisconnectEvent event) {
		ProxiedPlayer player = event.getPlayer();
		PrivateMessage.delReply(player);
		PrivateMessageToggleCommand.players.remove(player.getUniqueId());
	}

}
