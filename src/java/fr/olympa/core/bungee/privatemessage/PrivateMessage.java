package fr.olympa.core.bungee.privatemessage;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import fr.olympa.api.player.OlympaConsole;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.core.bungee.utils.BungeeUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class PrivateMessage {

	public static List<String> privateMessageCommand = new ArrayList<>();
	public static List<String> replyCommand = new ArrayList<>();
	private static Map<UUID, UUID> reply = new HashMap<>();

	public static void delReply(ProxiedPlayer player) {
		if (reply.containsKey(player.getUniqueId()))
			reply.remove(player.getUniqueId());
	}

	public static UUID getReply(ProxiedPlayer player) {
		if (reply.containsKey(player.getUniqueId()))
			return reply.get(player.getUniqueId());
		return null;
	}

	@SuppressWarnings("deprecation")
	public static void send(CommandSender sender, ProxiedPlayer target, String message) {
		// play sound mob.cat.meow

		OlympaPlayer emeraldPlayer = null;
		OlympaPlayer emeraldTarget;
		try {
			if (sender instanceof ProxiedPlayer)
				emeraldPlayer = new AccountProvider(((ProxiedPlayer) sender).getUniqueId()).get();
			else if (OlympaConsole.getDevConnected() != null)
				emeraldPlayer = OlympaConsole.getDevConnected();
			emeraldTarget = new AccountProvider(target.getUniqueId()).get();
		} catch (SQLException e) {
			sender.sendMessage(BungeeUtils.color("&cUne erreur est survenue, impossible d'envoyer ce message."));
			e.printStackTrace();
			return;
		}

		TextComponent msgPlayer = new TextComponent(TextComponent.fromLegacyText(BungeeUtils.color("&6Message &c\u2B06 " + emeraldTarget.getGroupPrefix() + target.getName() + "&9 : ")));
		msgPlayer.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(BungeeUtils.color("&aRépondre à " + emeraldTarget.getGroupPrefix() + target.getName())).create()));
		msgPlayer.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + target.getName() + " "));
		msgPlayer.addExtra(new TextComponent(TextComponent.fromLegacyText(BungeeUtils.color("&9") + message)));
		sender.sendMessage(msgPlayer);

		String groupPrefix = "";
		if (emeraldPlayer != null)
			groupPrefix = emeraldPlayer.getGroupPrefix();
		TextComponent msgTarget = new TextComponent(TextComponent.fromLegacyText(BungeeUtils.color("&6Message &a\u2B07 " + groupPrefix + sender.getName() + "&9 : ")));
		msgTarget.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(BungeeUtils.color("&aRépondre à " + groupPrefix + sender.getName())).create()));
		msgTarget.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + sender.getName() + " "));
		msgTarget.addExtra(new TextComponent(TextComponent.fromLegacyText(BungeeUtils.color("&9") + message)));
		target.sendMessage(msgTarget);
	}

	public static void setReply(ProxiedPlayer player, ProxiedPlayer target) {
		if (reply.containsKey(player.getUniqueId()) && reply.get(player.getUniqueId()).equals(target.getUniqueId()))
			return;
		reply.put(player.getUniqueId(), target.getUniqueId());
	}

}
