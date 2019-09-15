package fr.tristiisch.olympa.core.permission.groups.listener;

import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import fr.tristiisch.olympa.api.customevents.AsyncOlympaPlayerLoadEvent;
import fr.tristiisch.olympa.api.objects.OlympaGroup;
import fr.tristiisch.olympa.api.objects.OlympaPlayer;
import fr.tristiisch.olympa.api.permission.OlympaPermission;
import fr.tristiisch.olympa.api.utils.Prefix;
import fr.tristiisch.olympa.api.utils.SpigotUtils;
import fr.tristiisch.olympa.api.utils.Utils;
import fr.tristiisch.olympa.core.datamanagment.redis.access.OlympaAccountObject;
import fr.tristiisch.olympa.core.permission.scoreboard.ScoreboardList;

public class GroupListener implements Listener {

	@EventHandler(priority = EventPriority.LOWEST)
	public void onOlympaPlayerLoad(AsyncOlympaPlayerLoadEvent event) {
		long now = Utils.getCurrentTimeinSeconds();
		OlympaPlayer olympaPlayer = event.getOlympaPlayer();
		TreeMap<OlympaGroup, Long> groups = olympaPlayer.getGroups();

		OlympaGroup group = groups.firstKey();
		new ScoreboardList(event.getPlayer(), group);

		List<OlympaGroup> oldGroups = groups.entrySet().stream().filter(entry -> entry.getValue() != 0 && entry.getValue() < now).map(entry -> entry.getKey()).collect(Collectors.toList());
		if (oldGroups.isEmpty()) {
			return;
		}
		oldGroups.forEach(oldGroup -> groups.remove(oldGroup));

		if (groups.isEmpty()) {
			olympaPlayer.addGroup(OlympaGroup.PLAYER);
		}

		Player player = event.getPlayer();

		if (oldGroups.size() == 1) {
			player.sendMessage(SpigotUtils.color(Prefix.INFO + "Votre grade &6" + oldGroups.get(0).getName() + "&e a expiré, vous êtes désormais &6" + olympaPlayer.getGroupsToHumainString() + "&e."));
		} else {
			player.sendMessage(SpigotUtils
					.color(Prefix.INFO + "Vos grades &6" + oldGroups.stream().map(OlympaGroup::getName).collect(Collectors.joining(", ")) + "&e ont expiré, vous êtes désormais &6" + olympaPlayer.getGroupsToHumainString() + "&e."));
		}
	}

	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		OlympaPlayer olympaPlayer = new OlympaAccountObject(player.getUniqueId()).getFromCache();
		if (olympaPlayer == null) {
			event.setFormat(SpigotUtils.color("&cERREUR &7") + "%s : %s");
		}

		OlympaGroup group = olympaPlayer.getGroups().firstKey();

		if (group != null) {
			if (OlympaPermission.CHAT_COLOR.hasPermission(olympaPlayer)) {
				event.setFormat(group.getPrefix() + "%s " + group.getChatSufix() + SpigotUtils.color(" %s"));
			} else {
				event.setFormat(group.getPrefix() + "%s " + group.getChatSufix() + " %s");
			}
		} else {
			event.setFormat(SpigotUtils.color("&cGRADE ERREUR &7") + "%s : %s");
		}
	}
}
