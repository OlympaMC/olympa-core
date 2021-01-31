package fr.olympa.core.spigot.report.gui;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.gui.OlympaGUI;
import fr.olympa.api.item.OlympaItemBuild;
import fr.olympa.api.report.ReportReason;
import fr.olympa.api.report.ReportReasonItem;

public class ReportGui extends OlympaGUI {

	public static void open(Player player, OfflinePlayer target2, String note) {
		ReportGui gui = new ReportGui("&6Report &e" + target2.getName(), 3, target2, note);

		List<OlympaItemBuild> items = ReportReason.values().stream().map(rr -> ((ReportReasonItem) rr).getItem()).collect(Collectors.toList());
		int slot = gui.inv.getSize() / 2 - items.size() / 2;
		for (OlympaItemBuild item : items)
			gui.inv.setItem(slot++, item.build());
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
		if (current == null)
			return true;

		//		ReportReason reason = ReportReason.get(current);
		ReportReason reason = ReportReason.values().stream().filter(itemGui -> ((ReportReasonItem) itemGui).getItem().build().isSimilar(current)).findFirst().orElse(null);
		ReportGuiConfirm.open(player, target, reason, note);
		return true;
	}
}
