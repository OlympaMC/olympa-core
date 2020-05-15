package fr.olympa.core.bungee.staffchat;

import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class StaffChatListener implements Listener {

	@EventHandler
	public void onChat(ChatEvent event) {
		if (!(event.getSender() instanceof ProxiedPlayer)) {
			return;
		}
		ProxiedPlayer player = (ProxiedPlayer) event.getSender();
		String message = event.getMessage();
		if (!StaffChatHandler.staffChat.contains(player)) {
			if (message.charAt(0) != '$') {
				return;
			}
			message = message.substring(1);
		}
		OlympaPlayer olympaPlayer = AccountProvider.get(player.getUniqueId());
		if (!OlympaCorePermissions.STAFF_CHAT.hasPermission(olympaPlayer)) {
			StaffChatHandler.staffChat.remove(player);
			return;
		}
		StaffChatHandler.sendMessage(olympaPlayer, player, message);
		event.setCancelled(true);
	}
}
