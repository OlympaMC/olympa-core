package fr.olympa.core.spigot.groups;

import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import fr.olympa.api.customevents.AsyncOlympaPlayerChangeGroupEvent;
import fr.olympa.api.customevents.AsyncOlympaPlayerChangeGroupEvent.ChangeType;
import fr.olympa.api.customevents.OlympaPlayerLoadEvent;
import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.ColorUtils;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.spigot.OlympaCore;

@SuppressWarnings("deprecation")
public class GroupListener implements Listener {

	//	@EventHandler(priority = EventPriority.HIGHEST)
	//	public void onAsyncOlympaPlayerChangeGroup(AsyncOlympaPlayerChangeGroupEvent event) {
	//		if (event.getPlayer() == null || event.getDone() != null)
	//			RedisSpigotSend.sendOlympaGroupChange(event.getOlympaPlayer(), event.getGroupsChanges()[0], event.getTimeStamp(), event.getChangeType(), event.getDone());
	//	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onOlympaPlayerLoad(OlympaPlayerLoadEvent event) {
		long now = Utils.getCurrentTimeInSeconds();
		OlympaPlayer olympaPlayer = event.getOlympaPlayer();
		TreeMap<OlympaGroup, Long> groups = olympaPlayer.getGroups();

		List<OlympaGroup> oldGroups = groups.entrySet().stream().filter(entry -> entry.getValue() != 0 && entry.getValue() < now).map(entry -> entry.getKey()).collect(Collectors.toList());
		if (oldGroups.isEmpty())
			return;
		oldGroups.forEach(oldGroup -> olympaPlayer.removeGroup(oldGroup));

		if (groups.isEmpty())
			olympaPlayer.addGroup(OlympaGroup.PLAYER);

		Player player = event.getPlayer();
		AccountProvider account = new AccountProvider(player.getUniqueId());
		OlympaCore.getInstance().getServer().getPluginManager().callEvent(new AsyncOlympaPlayerChangeGroupEvent(player, ChangeType.REMOVE, olympaPlayer, oldGroups.toArray(OlympaGroup[]::new)));
		account.saveToRedis(olympaPlayer);
		account.saveToCache(olympaPlayer);
		if (oldGroups.size() == 1)
			player.sendMessage(ColorUtils.color(Prefix.INFO + "Ton grade &6" + oldGroups.get(0).getName() + "&e a expiré, tu es désormais &6" + olympaPlayer.getGroupsToHumainString() + "&e."));
		else
			player.sendMessage(
					ColorUtils.color(Prefix.INFO + "Tes grades &6" + oldGroups.stream().map(OlympaGroup::getName).collect(Collectors.joining(", ")) + "&e ont expiré, tu es désormais &6" + olympaPlayer.getGroupsToHumainString() + "&e."));
	}
}
