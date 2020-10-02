package fr.olympa.core.spigot.afk;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import fr.olympa.api.customevents.AsyncOlympaPlayerChangeGroupEvent;
import fr.olympa.api.customevents.AsyncOlympaPlayerChangeGroupEvent.ChangeType;
import fr.olympa.api.customevents.OlympaPlayerLoadEvent;
import fr.olympa.api.customevents.PlayerNameTagEditEvent;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.scoreboard.tab.INametagApi;
import fr.olympa.api.scoreboard.tab.Nametag;
import fr.olympa.api.utils.spigot.SpigotUtils;
import fr.olympa.core.spigot.OlympaCore;

public class AfkListener implements Listener {

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		AfkHandler.removeLastAction(player);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerJoin(OlympaPlayerLoadEvent event) {
		Player player = event.getPlayer();
		AfkHandler.updateLastAction(player, false);
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		if (SpigotUtils.isSameLocationXZ(event.getFrom(), event.getTo()))
			return;
		AfkHandler.updateLastAction(player, false);
	}

	@EventHandler
	public void onOlympaPlayerChangeGroup(AsyncOlympaPlayerChangeGroupEvent event) {
		ChangeType changeType = event.getChangeType();
		Player player = event.getPlayer();
		OlympaPlayer olympaPlayer = event.getOlympaPlayer();
		INametagApi nameTagApi = OlympaCore.getInstance().getNameTagApi();
		if (Arrays.stream(event.getGroupsChanges()).noneMatch(OlympaCorePermissions.AFK_SEE_IN_TAB::hasPermission))
			return;
		if ((ChangeType.SET.equals(changeType) || ChangeType.ADD.equals(changeType)) && OlympaCorePermissions.AFK_SEE_IN_TAB.hasPermission(olympaPlayer))
			AfkHandler.get().stream().forEach(entry -> {
				Player target = Bukkit.getPlayer(entry.getKey());
				Nametag nameTag = nameTagApi.getNametag(target);
				nameTag.appendSuffix(AfkPlayer.AFK_SUFFIX);
				nameTagApi.updateFakeNameTag(target, nameTag, Arrays.asList(player));
			});
		else if (ChangeType.REMOVE.equals(changeType))
			AfkHandler.get().stream().forEach(entry -> {
				Player target = Bukkit.getPlayer(entry.getKey());
				Nametag nameTag = nameTagApi.getNametag(target);
				nameTag.setSuffix(nameTag.getSuffix().replace(AfkPlayer.AFK_SUFFIX, ""));
				nameTagApi.updateFakeNameTag(target, nameTag, Arrays.asList(player));
			});
	}

	@EventHandler
	public void onPlayerNameTagEdit(PlayerNameTagEditEvent event) {
		Player player = event.getPlayer();
		AfkPlayer afkPlayer = AfkHandler.get(player);
		if (!afkPlayer.isAfk())
			return;
		Nametag nameTag = event.getNameTag();
		nameTag.appendSuffix(AfkPlayer.AFK_SUFFIX);
	}
}
