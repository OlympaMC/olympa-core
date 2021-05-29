package fr.olympa.core.spigot.chat;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;

import fr.olympa.api.chat.ColorUtils;
import fr.olympa.api.permission.list.OlympaCorePermissionsSpigot;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class Chat {

	public static class OlympaChat {

		private String lastMsg;
		private long lastMsgTime;

		public OlympaChat(String lastMsg, long lastMsgTime) {
			this.lastMsg = lastMsg;
			this.lastMsgTime = lastMsgTime;
		}

		public String getLastMsg() {
			return lastMsg;
		}

		public long getLastMsgTime() {
			return lastMsgTime;
		}

		public boolean isLastMsg(String msg) {
			return lastMsg != null && lastMsg.equalsIgnoreCase(msg);
		}

		public void setLastMsg(String lastMsg) {
			this.lastMsg = lastMsg;
		}

		public void setLastMsgTime(long lastMsgTime) {
			this.lastMsgTime = lastMsgTime;
		}
	}

	private static Map<UUID, OlympaChat> players = new HashMap<>();

	public static OlympaChat getPlayer(UUID uuid) {
		if (players.containsKey(uuid)) {
			return players.get(uuid);
		} else {
			players.put(uuid, new OlympaChat("", 0));
			return players.get(uuid);
		}
	}

	public static void sendToStaff(String type, Player player, String msg) {
		TextComponent text = new TextComponent("\u2623 [" + type + "] " + player.getName() + " > ");
		text.setColor(ChatColor.DARK_PURPLE);

		for (BaseComponent s : new ComponentBuilder(msg).color(ChatColor.LIGHT_PURPLE).create()) {
			text.addExtra(s);
		}
		text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ColorUtils.color("&cCliquez pour mute " + player.getName())).create()));
		text.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/mute " + player.getName()));

		OlympaCorePermissionsSpigot.CHAT_SEEINSULTS.sendMessage(text);
	}

	public static void sendToStaff(String type, Player player, String msg, String match) {
		TextComponent text = new TextComponent("\u2623 [" + type + "] " + player.getName() + " > ");
		text.setColor(ChatColor.DARK_PURPLE);
		msg = ColorUtils.color("&d" + msg.replace(match, "&l" + match + "&d"));
		text.addExtra(msg);
		text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ColorUtils.color("&cCliquez pour mute " + player.getName())).create()));
		text.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/mute " + player.getName()));

		OlympaCorePermissionsSpigot.CHAT_SEEINSULTS.sendMessage(text);
	}
}
