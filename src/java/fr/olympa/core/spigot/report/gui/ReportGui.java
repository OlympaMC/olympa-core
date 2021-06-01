package fr.olympa.core.spigot.report.gui;

import java.util.List;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.common.report.ReportReason;
import fr.olympa.api.spigot.gui.OlympaGUI;
import fr.olympa.api.spigot.item.OlympaItemBuild;

public class ReportGui extends OlympaGUI {

	public static void open(Player player, OfflinePlayer target2, String note) {
		List<ReportReason> reportReasons = ReportReason.valuesSorted();
		ReportGui gui = new ReportGui("&6Report &e" + target2.getName(), reportReasons.size() / 9 + 2, target2, note);

		//		int slot = gui.inv.getSize() / 2 - reportReasons.size() / 2;
		int slot = 0;
		for (ReportReason rp : reportReasons)
			if (rp.getItem() != null)
				gui.inv.setItem(slot++, ((OlympaItemBuild) rp.getItem()).build());
			else
				new NullPointerException("L'item du ReportReason " + rp.getReason() + " n'a été ajouté, il n'a jamais été set. Il est juste caché.").printStackTrace();
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
		ReportReason reason = ReportReason.values().stream().filter(itemGui -> ((OlympaItemBuild) itemGui.getItem()).build().isSimilar(current)).findFirst().orElse(null);
		ReportGuiConfirm.open(player, target, reason, note);
		return true;
	}
}
