package fr.olympa.core.bungee.staffchat;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.datamanagment.DataHandler;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class StaffChatHandler {

	private static final Set<UUID> staffChat = new HashSet<>();

	public static void sendMessage(OlympaPlayer olympaPlayer, CommandSender sender, String msg) {
		String message = msg.replaceAll("( )\\1+", " ");
		String senderName = "§eConsole";
		if (sender == null)
			senderName = "Discord " + olympaPlayer.getGroupNameColored() + " " + olympaPlayer.getName();
		else if (sender instanceof ProxiedPlayer)
			senderName = Utils.capitalize(((ProxiedPlayer) sender).getServer().getInfo().getName()) + " " + olympaPlayer.getGroupNameColored() + " " + sender.getName();

		BaseComponent[] messageComponent = TextComponent.fromLegacyText(Prefix.STAFFCHAT + senderName + " §7: " + message);
		ProxyServer.getInstance().getPlayers().stream().filter(p -> !DataHandler.isUnlogged(p) && OlympaCorePermissions.STAFF_CHAT.hasPermission(new AccountProvider(p.getUniqueId()).getFromRedis()))
				.forEach(p -> p.sendMessage(messageComponent));
		ProxyServer.getInstance().getConsole().sendMessage(messageComponent);
		ProxyServer.getInstance().getPluginManager().callEvent(new StaffChatEvent(sender, olympaPlayer, msg));
	}

	public static Set<UUID> getStaffchat() {
		return staffChat;
	}
}
