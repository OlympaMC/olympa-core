package fr.olympa.core.spigot.chat;

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

		for (String arg : message.split(" ")) {
			if (!Matcher.isUsername(arg)) {
				continue;
			}
			Player target = Bukkit.getPlayer(arg);
			if (target == null) {
				continue;
			}
			event.getRecipients().remove(target);
			String messageToTarget = new String(message);
			String format = event.getFormat();
			messageToTarget = messageToTarget.replace(arg, "§6@" + target.getName() + getChatColor(format));
			target.playSound(target.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 3.0F, 0.533F);
			new FakeMsg(format, player.getDisplayName(), messageToTarget).send(target);
		}
	}
}
