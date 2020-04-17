package fr.olympa.core.spigot.groups;

import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import fr.olympa.api.customevents.OlympaPlayerLoadEvent;
import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.ColorUtils;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;

public class GroupListener implements Listener {

	@EventHandler(priority = EventPriority.LOWEST)
	public void onOlympaPlayerLoad(OlympaPlayerLoadEvent event) {
		long now = Utils.getCurrentTimeInSeconds();
		OlympaPlayer olympaPlayer = event.getOlympaPlayer();
		TreeMap<OlympaGroup, Long> groups = olympaPlayer.getGroups();

		List<OlympaGroup> oldGroups = groups.entrySet().stream().filter(entry -> entry.getValue() != 0 && entry.getValue() < now).map(entry -> entry.getKey()).collect(Collectors.toList());
		if (oldGroups.isEmpty()) {
			return;
		}
		oldGroups.forEach(oldGroup -> olympaPlayer.removeGroup(oldGroup));

		if (groups.isEmpty()) {
			olympaPlayer.addGroup(OlympaGroup.PLAYER);
		}

		Player player = event.getPlayer();
		AccountProvider account = new AccountProvider(player.getUniqueId());
		account.saveToRedis(olympaPlayer);
		account.saveToCache(olympaPlayer);
		if (oldGroups.size() == 1) {
			player.sendMessage(ColorUtils.color(Prefix.INFO + "Ton grade &6" + oldGroups.get(0).getName() + "&e a expiré, tu es désormais &6" + olympaPlayer.getGroupsToHumainString() + "&e."));
		} else {
			player.sendMessage(
					ColorUtils.color(Prefix.INFO + "Tes grades &6" + oldGroups.stream().map(OlympaGroup::getName).collect(Collectors.joining(", ")) + "&e ont expiré, tu es désormais &6" + olympaPlayer.getGroupsToHumainString() + "&e."));
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		OlympaPlayer olympaPlayer = new AccountProvider(player.getUniqueId()).getFromCache();
		if (olympaPlayer == null) {
			event.setFormat(ColorUtils.color("&cERREUR &7") + "%s : %s");
			return;
		}

		OlympaGroup group = olympaPlayer.getGroup();
		if (group != null) {
			if (OlympaCorePermissions.CHAT_COLOR.hasPermission(olympaPlayer)) {
				event.setMessage(ColorUtils.color(event.getMessage()));
			}
			event.setFormat(group.getPrefix() + "%s " + group.getChatSufix() + " %s");
		} else {
			event.setFormat(ColorUtils.color("&cGRADE ERREUR &7") + "%s : %s");
		}
	}
}
