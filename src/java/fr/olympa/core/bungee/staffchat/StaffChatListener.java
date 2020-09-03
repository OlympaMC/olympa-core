package fr.olympa.core.bungee.staffchat;

import java.util.UUID;

import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.Prefix;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class StaffChatListener implements Listener {

	@EventHandler
	public void onChat(ChatEvent event) {
		if (!(event.getSender() instanceof ProxiedPlayer))
			return;

		String message = event.getMessage();
		if (message.startsWith("/"))
			return;

		ProxiedPlayer player = (ProxiedPlayer) event.getSender();
		UUID uuid = player.getUniqueId();

		OlympaPlayer olympaPlayer;
		if (StaffChatHandler.getStaffchat().contains(uuid)) {
			if (message.charAt(0) == '$')
				message = message.substring(1);
		} else if (message.charAt(0) == '$')
			message = message.substring(1);
		else
			return;
		olympaPlayer = new AccountProvider(player.getUniqueId()).getFromRedis();

		if (!OlympaCorePermissions.STAFF_CHAT.hasPermission(olympaPlayer)) {
			player.sendMessage(TextComponent.fromLegacyText(Prefix.ERROR.formatMessage("Tu n'as pas la permission d'écrire dans le chat du staff.")));
			StaffChatHandler.getStaffchat().remove(uuid);
			return;
		}

		StaffChatHandler.sendMessage(olympaPlayer, player, message);
		event.setCancelled(true);

	}
}
