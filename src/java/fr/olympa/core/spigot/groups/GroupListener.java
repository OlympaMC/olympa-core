package fr.olympa.core.spigot.groups;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.common.chat.Chat;
import fr.olympa.api.common.groups.OlympaGroup;
import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.spigot.customevents.AsyncOlympaPlayerChangeGroupEvent;
import fr.olympa.api.spigot.customevents.AsyncOlympaPlayerChangeGroupEvent.ChangeType;
import fr.olympa.api.spigot.customevents.OlympaPlayerLoadEvent;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.common.permission.list.OlympaCorePermissionsSpigot;
import fr.olympa.core.common.provider.AccountProvider;
import fr.olympa.core.spigot.OlympaCore;

@SuppressWarnings("deprecation")
public class GroupListener implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onAsyncOlympaPlayerChangeGroup(AsyncOlympaPlayerChangeGroupEvent event) {
		Player player = event.getPlayer();
		if (player != null && player.isOnline())
			calculatePermissions(event.getOlympaPlayer(), player);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onOlympaPlayerLoad(OlympaPlayerLoadEvent event) {
		long now = Utils.getCurrentTimeInSeconds();
		OlympaPlayer olympaPlayer = event.getOlympaPlayer();

		calculatePermissions(olympaPlayer, event.getPlayer());
		Map<OlympaGroup, Long> groups = olympaPlayer.getGroups();

		List<OlympaGroup> oldGroups = groups.entrySet().stream().filter(entry -> entry.getValue() != 0 && entry.getValue() < now).map(entry -> entry.getKey()).collect(Collectors.toList());
		if (oldGroups.isEmpty())
			return;
		OlympaGroup oldFirstGroup = olympaPlayer.getGroup();
		oldGroups.forEach(oldGroup -> olympaPlayer.removeGroup(oldGroup));
		if (groups.isEmpty())
			olympaPlayer.addGroup(OlympaGroup.PLAYER);

		Player player = event.getPlayer();
		AccountProvider account = new AccountProvider(player.getUniqueId());
		OlympaCore.getInstance().getServer().getPluginManager().callEvent(new AsyncOlympaPlayerChangeGroupEvent(player, ChangeType.REMOVE, olympaPlayer, oldGroups.toArray(OlympaGroup[]::new)));
		account.saveToRedis(olympaPlayer);
		account.saveToCache(olympaPlayer);
		List<String> out = new ArrayList<>();
		out.add("&m------------------------------");
		out.add("");
		if (oldGroups.size() == 1)
			out.add("&cTon grade &4" + oldGroups.get(0).getName() + "&c a expir??.");
		else
			out.add("&cTes grades &4" + oldGroups.stream().map(OlympaGroup::getName).collect(Collectors.joining(", ")) + "&c ont expir??.");
		if (oldFirstGroup != olympaPlayer.getGroup())
			out.add("&2Tu es d??sormais &a" + olympaPlayer.getNameWithPrefix() + "&e.");
		out.add("");
		out.add("&m------------------------------");
		player.sendMessage(Chat.getCenteredMessage(out));
		player.playSound(player.getLocation(), Sound.ENTITY_BAT_DEATH, 1.5f, 0.5f);
	}

	private void calculatePermissions(OlympaPlayer olympaPlayer, Player player) {
		for (PermissionAttachmentInfo attachmentInfo : player.getEffectivePermissions())
			if (attachmentInfo.getAttachment() != null && attachmentInfo.getAttachment().getPlugin() == OlympaCore.getInstance()) {
				attachmentInfo.getAttachment().remove();
				OlympaCore.getInstance().sendMessage("Mise ?? jour des permissions Bukkit du joueur ??6%s", player.getName());
				break;
			}
		if (player.isOp() && !OlympaCorePermissionsSpigot.STAFF.hasPermission(olympaPlayer)) {
			player.setOp(false);
			LinkSpigotBungee.getInstance().sendMessage("&4%s&c &c??tait encore OP, heureusement que je suis l?? pour lui enlever...", olympaPlayer.getName());
		}
		PermissionAttachment attachment = player.addAttachment(OlympaCore.getInstance());
		olympaPlayer.getGroup().getAllGroups().sorted(Comparator.comparing(OlympaGroup::getPower)).forEach(group -> group.runtimePermissions.forEach((perm, value) -> attachment.setPermission(perm, value)));
		olympaPlayer.getCustomPermissions().entrySet().stream().filter(e -> e.getValue() == null || e.getValue().isSame(OlympaCore.getInstance().getOlympaServer()))
		.forEach(e -> {
			String permName = e.getKey();
					if (permName.indexOf(0) == '-') {
						LinkSpigotBungee.getInstance().sendMessage("&7[CUSTOM PERMISSION] &eLa permission &6%s&e de &6%s&e lui a ??t?? retirer%s.", permName, olympaPlayer.getName(),
								e.getValue() != null ? " &esur les serveurs &6" + e.getValue().getNameCaps() + "&e uniquement " : "");
				attachment.setPermission(permName.substring(1), false);
					} else {
						LinkSpigotBungee.getInstance().sendMessage("&7[CUSTOM PERMISSION] &eLa permission &6%s&e de &6%s&e lui a ??t?? ajout??%s.", permName, olympaPlayer.getName(),
								e.getValue() != null ? " &esur les serveurs &6" + e.getValue().getNameCaps() + "&e uniquement " : "");
				attachment.setPermission(permName, true);
					}
		});
		player.recalculatePermissions();
		((CraftServer) Bukkit.getServer()).getHandle().getServer().getCommandDispatcher().a(((CraftPlayer) player).getHandle());
	}

}
