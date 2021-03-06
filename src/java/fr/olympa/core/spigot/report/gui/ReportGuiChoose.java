package fr.olympa.core.spigot.report.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryCloseEvent.Reason;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import fr.olympa.api.spigot.gui.OlympaGUI;
import fr.olympa.api.spigot.item.OlympaItemBuild;
import fr.olympa.api.utils.Prefix;

public class ReportGuiChoose extends OlympaGUI {

	public static void open(Player player) {

		List<Player> potentials = new ArrayList<>();
		EntityDamageEvent lastDmgCause = player.getLastDamageCause();
		if (player.getKiller() != null)
			potentials.add(player.getKiller());
		if (lastDmgCause != null && lastDmgCause.getEntity() instanceof Player lastDmgPlayer)
			potentials.add(lastDmgPlayer);
		if (potentials.isEmpty())
			potentials = Bukkit.getOnlinePlayers().stream().limit(6 * 9).collect(Collectors.toList());
		List<OlympaItemBuild> items = potentials.stream().map(p -> new OlympaItemBuild("&cReport &4" + p.getName()).skullowner(p)).collect(Collectors.toList());
		ReportGuiChoose gui = new ReportGuiChoose(items.size(), "&6Report");
		int slot = gui.inv.getSize() / 2 - items.size() / 2;
		for (OlympaItemBuild item : items) {
			int finalSlotForItem = slot;
			item.buildPlayerHead(itemFinal -> gui.inv.setItem(finalSlotForItem, itemFinal));
			slot++;
//			gui.inv.setItem(slot++, item.build());
		}
		gui.create(player);
	}

	public ReportGuiChoose(int placeNeeded, String name) {
		super(placeNeeded, name);
	}

	@Override
	public boolean onClick(Player player, ItemStack current, int slot, ClickType click) {
		if (current == null)
			return true;
		SkullMeta itemMeta = ((SkullMeta) current.getItemMeta());
		OfflinePlayer target = itemMeta.getOwningPlayer();
		if (target == null) { // TODO It is always null, need to fix this (item is player head with good texture)
			Prefix.DEFAULT_BAD.sendMessage(player, "Le joueur est introuvable.");
			player.closeInventory(Reason.PLUGIN);
			return true;
		}
		ReportGui.open(player, target, null);
		return true;
	}
}
