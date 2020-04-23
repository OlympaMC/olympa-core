package fr.olympa.core.spigot.chat;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import fr.olympa.api.utils.Matcher;

public class MentionListener implements Listener {

	public String getChatColor(String format) {
		int index = format.lastIndexOf("%s");
		return format.substring(index - 3, index - 1);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerChatEvent(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		String message = event.getMessage();

		Map<Player, String> mentionned = new HashMap<>();
		for (String arg : message.split(" ")) {
			if (!Matcher.isUsername(arg)) {
				continue;
			}
			if (arg.startsWith("@")) {
				arg = arg.substring(1);
			}
			Player target = Bukkit.getPlayer(arg);
			if (target == null) {
				continue;
			}
			mentionned.put(target, arg);
		}
		for (Entry<Player, String> entry : mentionned.entrySet()) {
			Player target = entry.getKey();
			String arg = entry.getValue();
			String messageToTarget = new String(message);
			String format = event.getFormat();
			do {
				messageToTarget = messageToTarget.replace(arg, "§6@" + target.getName() + getChatColor(format));
			} while (messageToTarget.contains(" " + arg));
			target.playSound(target.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 3.0F, 0.533F);
			new FakeMsg(format, player.getDisplayName(), messageToTarget).send(target);
			event.getRecipients().remove(target);
		}
	}
}