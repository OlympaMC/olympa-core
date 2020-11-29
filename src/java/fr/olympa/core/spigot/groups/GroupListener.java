package fr.olympa.core.spigot.groups;

import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_15_R1.CraftServer;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.permissions.PermissionAttachment;

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

	@EventHandler (priority = EventPriority.HIGHEST)
	public void onAsyncOlympaPlayerChangeGroup(AsyncOlympaPlayerChangeGroupEvent event) {
		Player player = event.getPlayer();
		if (player != null && player.isOnline()) {
			calculatePermissions(event.getOlympaPlayer(), player);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onOlympaPlayerLoad(OlympaPlayerLoadEvent event) {
		long now = Utils.getCurrentTimeInSeconds();
		OlympaPlayer olympaPlayer = event.getOlympaPlayer();
		
		calculatePermissions(olympaPlayer, event.getPlayer());
		
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
	
	private void calculatePermissions(OlympaPlayer olympaPlayer, Player player) {
		if (player.hasMetadata("permAttachment")) {
			PermissionAttachment attachment = (PermissionAttachment) player.getMetadata("permAttachment").remove(0).value();
			attachment.remove();
			OlympaCore.getInstance().sendMessage("Mise à jour des permissions Bukkit du joueur §6%s", player.getName());
		}
		PermissionAttachment attachment = player.addAttachment(OlympaCore.getInstance());
		player.setMetadata("permAttachment", new FixedMetadataValue(OlympaCore.getInstance(), attachment));
		olympaPlayer.getGroup().getAllGroups().forEach(group -> group.runtimePermissions.forEach((perm, value) -> attachment.setPermission(perm, value)));
		player.recalculatePermissions();
		((CraftServer) Bukkit.getServer()).getHandle().getServer().getCommandDispatcher().a(((CraftPlayer) player).getHandle());
	}
	
}
