package fr.olympa.core.spigot.report.gui;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.gui.OlympaGUI;
import fr.olympa.api.item.OlympaItemBuild;
import fr.olympa.core.spigot.report.items.ReportReason;

public class ReportGui extends OlympaGUI {

	public static void open(Player player, OfflinePlayer target2, String note) {
		ReportGui gui = new ReportGui("&6Report &e" + target2.getName(), 3 * 9, target2, note);

		List<OlympaItemBuild> items = Arrays.stream(ReportReason.values()).map(ReportReason::getItem).collect(Collectors.toList());
		int slot = gui.inv.getSize() / 2 - items.size() / 2;
		for (OlympaItemBuild item : items) {
			gui.inv.setItem(slot++, item.build());
		}
		gui.create(player);
	}

	OfflinePlayer target;
	String note;

	public ReportGui(String name, int rows, OfflinePlayer target, String note) {
		super(name, rows);
		this.target = target;
		this.note = note;
	}

	@Override
	public boolean onClick(Player player, ItemStack current, int slot, ClickType click) {
		if (current == null) {
			return false;
		}

		ReportReason reason = ReportReason.get(current);
		ReportGuiConfirm.open(player, target, reason, note);
		return false;
	}
}
