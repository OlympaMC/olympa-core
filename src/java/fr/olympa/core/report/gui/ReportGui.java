package fr.olympa.core.report.gui;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.OlympaCore;
import fr.olympa.api.gui.OlympaGUI;
import fr.olympa.api.item.OlympaItemBuild;
import fr.olympa.core.report.OlympaReport;
import fr.olympa.core.report.items.ReportReason;

public class ReportGui extends OlympaGUI {

	public static void open(Player player, Player target) {
		ReportGui gui = new ReportGui("&6Report &e" + target.getName(), 3 * 9, target);

		List<OlympaItemBuild> items = Arrays.stream(ReportReason.values()).map(ReportReason::getItem).collect(Collectors.toList());
		int slot = gui.inv.getSize() / 2 - items.size() / 2;
		for (OlympaItemBuild item : items) {
			gui.inv.setItem(slot++, item.build());
		}
		gui.create(player);
	}

	public static void report(Player player, Player target, ReportReason reason) {
		OlympaReport report = new OlympaReport(target.getUniqueId(), player.getUniqueId(), reason, OlympaCore.getInstance().getServer().getName());

		if (target.isOnline()) {

		}
		// TODO
	}

	Player target;

	public ReportGui(String name, int rows, Player target) {
		super(name, rows);
		this.target = target;
	}

	@Override
	public boolean onClick(Player player, ItemStack current, int slot, ClickType click) {
		if (current == null) {
			return false;
		}

		ReportReason reason = ReportReason.get(current);
		ReportGuiConfirm.open(player, this.target, reason);
		return false;
	}
}
