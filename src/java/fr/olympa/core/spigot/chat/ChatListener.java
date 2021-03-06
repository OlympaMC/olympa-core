package fr.olympa.core.spigot.chat;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import fr.olympa.api.common.chat.ColorUtils;
import fr.olympa.api.common.groups.OlympaGroup;
import fr.olympa.api.common.match.RegexMatcher;
import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.common.permission.list.OlympaCorePermissionsSpigot;
import fr.olympa.core.common.provider.AccountProvider;
import fr.olympa.core.spigot.module.CoreModules;

public class ChatListener implements Listener {

	public String getChatColor(String format) {
		int index = format.lastIndexOf("%s");
		//		return ChatColor.getLastColors(format) format.substring(index - 3, index - 1);
		return ChatColor.getLastColors(format.substring(0, index - 1));
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		OlympaPlayer olympaPlayer = AccountProvider.getter().get(player.getUniqueId());
		if (olympaPlayer == null) {
			event.setFormat(ColorUtils.color("&cERREUR &7") + "%s : %s");
			return;
		}

		OlympaGroup group = olympaPlayer.getGroup();
		if (group != null) {
			if (OlympaCorePermissionsSpigot.CHAT_COLOR.hasPermission(olympaPlayer))
				event.setMessage(ColorUtils.color(event.getMessage()));
			event.setFormat(olympaPlayer.getGroupPrefix() + "%s " + group.getChatSuffix() + " %s");
		} else
			event.setFormat(ColorUtils.color("&cGRADE ERREUR &7") + "%s : %s");
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerChatEvent(AsyncPlayerChatEvent event) {
		if (event.isCancelled())
			return;
		Player player = event.getPlayer();
		String message = event.getMessage();

		Map<Player, String> mentionned = new HashMap<>();
		for (String arg : message.split(" ")) {
			arg = ChatColor.stripColor(arg);
			if (arg.startsWith("@"))
				arg = arg.substring(1);
			if (!RegexMatcher.USERNAME.is(arg))
				continue;
			Player target = Bukkit.getPlayerExact(arg);
			if (target == null)
				continue;
			mentionned.put(target, arg);
		}
		for (Entry<Player, String> entry : mentionned.entrySet()) {
			Player target = entry.getKey();
			String arg = entry.getValue();
			String messageToTarget = message;
			String format = event.getFormat();
			do
				messageToTarget = messageToTarget.replace(arg, "??6" + target.getName() + getChatColor(format));
			while (messageToTarget.contains(" " + arg));
			target.playSound(target.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 3.0F, 0.533F);
			new FakeMsg(format, player.getDisplayName(), messageToTarget).send(target);
			event.getRecipients().remove(target);
			if (CoreModules.AFK.getApi().isAfk(target))
				Prefix.INFO.sendMessage(player, "&7%s est AFK et risque de ne pas te r??pondre.", target.getName());
		}
	}
}
