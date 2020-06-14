package fr.olympa.core.bungee.staffchat;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class StaffChatHandler {
	
	private static final Set<UUID> staffChat = new HashSet<>();
	
	@SuppressWarnings("deprecation")
	public static void sendMessage(OlympaPlayer olympaPlayer, ProxiedPlayer player, String msg) {
		String message = msg.replaceAll("( )\\1+", " ");
		BaseComponent[] messageComponent = TextComponent.fromLegacyText(Prefix.STAFFCHAT + Utils.capitalize(player.getServer().getInfo().getName()) + " " + olympaPlayer.getGroupNameColored() + " §l" + player.getName() + " §7: " + message);
		ProxyServer.getInstance().getPlayers().stream().filter(p -> OlympaCorePermissions.STAFF_CHAT.hasPermission(new AccountProvider(p.getUniqueId()).getFromRedis())).forEach(p -> p.sendMessage(messageComponent));
	}

	public static Set<UUID> getStaffchat() {
		return staffChat;
	}
}
