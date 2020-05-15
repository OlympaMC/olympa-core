package fr.olympa.core.bungee.staffchat;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class StaffChatHandler {

	public static Set<ProxiedPlayer> staffChat = new HashSet<>();

	@SuppressWarnings("deprecation")
	public static void sendMessage(OlympaPlayer olympaPlayer, ProxiedPlayer player, String msg) {
		String message = msg.replaceAll("( )\\1+", " ");
		Collection<ProxiedPlayer> players = ProxyServer.getInstance().getPlayers();
		Set<ProxiedPlayer> playerswithPerm = players.stream().filter(p -> OlympaCorePermissions.STAFF_CHAT.hasPermission(AccountProvider.<OlympaPlayer>get(p.getUniqueId()))).collect(Collectors.toSet());
		String serverName = Utils.capitalize(player.getServer().getInfo().getName());
		playerswithPerm.forEach(p -> p.sendMessages(Prefix.STAFFCHAT + " " + serverName + " " + olympaPlayer.getGroupNameColored() + " " + player.getName() + " " + message));
	}
}
