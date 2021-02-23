package fr.olympa.core.bungee.staffchat;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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

	public static int sendMessage(OlympaPlayer olympaPlayer, CommandSender sender, String msg) {
		String message = msg.replaceAll("( )\\1+", " ");
		String senderName;
		ProxiedPlayer player = null;
		boolean isDiscord = sender == null;
		if (olympaPlayer != null) {
			player = ProxyServer.getInstance().getPlayer(olympaPlayer.getName());
			if (player != null)
				sender = player;
		}
		if (sender == null)
			senderName = "Discord " + olympaPlayer.getGroupNameColored() + " " + olympaPlayer.getName();
		else if (sender instanceof ProxiedPlayer)
			senderName = Utils.capitalize(((ProxiedPlayer) sender).getServer().getInfo().getName()) + " " + olympaPlayer.getGroupNameColored() + " " + sender.getName();
		else
			senderName = "§e" + sender.getName();

		BaseComponent[] messageComponent = TextComponent.fromLegacyText(Prefix.STAFFCHAT + senderName + " §7: " + message);
		List<ProxiedPlayer> staff = ProxyServer.getInstance().getPlayers().stream().filter(p -> !DataHandler.isUnlogged(p) && OlympaCorePermissions.STAFF_CHAT.hasPermission(new AccountProvider(p.getUniqueId()).getFromRedis()))
				.collect(Collectors.toList());
		staff.forEach(p -> p.sendMessage(messageComponent));
		ProxyServer.getInstance().getConsole().sendMessage(messageComponent);
		if (!isDiscord)
			ProxyServer.getInstance().getPluginManager().callEvent(new StaffChatEvent(sender, olympaPlayer, msg));
		else
			return staff.size();
		return -1;
	}

	public static Set<UUID> getStaffchat() {
		return staffChat;
	}
}
