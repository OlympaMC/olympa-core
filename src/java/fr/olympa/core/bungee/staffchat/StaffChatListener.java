package fr.olympa.core.bungee.staffchat;

import java.util.UUID;

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
		if (!(event.getSender() instanceof ProxiedPlayer)) return;

		ProxiedPlayer player = (ProxiedPlayer) event.getSender();
		UUID uuid = player.getUniqueId();
		String message = event.getMessage();

		OlympaPlayer olympaPlayer;
		if (StaffChatHandler.staffChat.contains(uuid)) {
			olympaPlayer = AccountProvider.get(uuid);
			if (!OlympaCorePermissions.STAFF_CHAT.hasPermission(olympaPlayer)) {
				StaffChatHandler.staffChat.remove(uuid);
				return;
			}
		}else {
			if (message.charAt(0) != '$') return;
			olympaPlayer = AccountProvider.get(uuid);
		}

		StaffChatHandler.sendMessage(olympaPlayer, player, message);
		event.setCancelled(true);
	}
}
