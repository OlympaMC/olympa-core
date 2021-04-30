package fr.olympa.core.spigot.report.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import fr.olympa.api.gui.OlympaGUI;
import fr.olympa.api.item.OlympaItemBuild;

public class ReportGuiChoose extends OlympaGUI {

	public static void open(Player player) {

		List<Player> potentials = new ArrayList<>();
		EntityDamageEvent lastDmgCause = player.getLastDamageCause();
		if (player.getKiller() != null)
			potentials.add(player.getKiller());
		if (lastDmgCause != null && lastDmgCause.getEntity() instanceof Player)
			potentials.add((Player) lastDmgCause.getEntity());
		if (potentials.isEmpty())
			potentials = Bukkit.getOnlinePlayers().stream().limit(6 * 9).collect(Collectors.toList());
		List<OlympaItemBuild> items = potentials.stream().map(p -> new OlympaItemBuild("&cReport &4" + p.getName()).skullowner(p)).collect(Collectors.toList());
		ReportGuiChoose gui = new ReportGuiChoose("&6Report", items.size() / 9);
		int slot = gui.inv.getSize() / 2 - items.size() / 2;
		for (OlympaItemBuild item : items)
			gui.inv.setItem(slot++, item.build());
		gui.create(player);
	}

	public ReportGuiChoose(String name, int rows) {
		super(name, rows);
	}

	@Override
	public boolean onClick(Player player, ItemStack current, int slot, ClickType click) {
		if (current == null)
			return true;

		OfflinePlayer target = ((SkullMeta) current.getItemMeta()).getOwningPlayer();
		ReportGui.open(player, target, null);
		return true;
	}
}
