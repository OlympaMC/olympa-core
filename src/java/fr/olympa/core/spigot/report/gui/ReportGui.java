package fr.olympa.core.spigot.report.gui;

import java.util.Collection;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.gui.OlympaGUI;
import fr.olympa.api.item.OlympaItemBuild;
import fr.olympa.api.report.ReportReason;

public class ReportGui extends OlympaGUI {

	public static void open(Player player, OfflinePlayer target2, String note) {
		Collection<ReportReason> reportReasons = ReportReason.values();
		ReportGui gui = new ReportGui("&6Report &e" + target2.getName(), reportReasons.size() / 9 + 2, target2, note);

		int slot = gui.inv.getSize() / 2 - reportReasons.size() / 2;
		for (ReportReason rp : reportReasons)
			if (rp.getItem() != null)
				gui.inv.setItem(slot++, ((OlympaItemBuild) rp.getItem()).build());
			else
				new NullPointerException("L'item du ReportReason " + rp.getName() + " n'a été ajouté. Il est juste caché.");
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
