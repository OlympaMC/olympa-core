package fr.olympa.core.report.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.gui.OlympaGUI;
import fr.olympa.api.item.OlympaItemBuild;
import fr.olympa.core.report.items.ReportReason;

public class ReportGuiConfirm extends OlympaGUI {

	static OlympaItemBuild ITEM_CONFIRM_YES = new OlympaItemBuild(Material.GREEN_STAINED_GLASS, "&aConfirmer").lore("", "", "", "&4[&c!&4] &cVous devez être sûr de votre report", "&cIl sera enregistrer et vous pouvez",
			"&cêtre sanctionner si vous en abusez");

	static OlympaItemBuild ITEM_CONFIRM_NO = new OlympaItemBuild(Material.GREEN_STAINED_GLASS, "&cAnnuler");

	public static void open(Player player, Player target, ReportReason reason) {
		ReportGuiConfirm gui = new ReportGuiConfirm("&6Signaler &e" + target.getName() + "&6 pour &e" + reason.getReason(), 3, target, reason);
		Inventory guiIventory = gui.getInventory();
		int slot = guiIventory.getSize() / 2 - 1;
		gui.yes = ITEM_CONFIRM_YES.setLore(1, "&cRaison: &4" + reason.getReason()).build();
		guiIventory.setItem(slot, gui.yes);
		guiIventory.setItem(slot + 2, ITEM_CONFIRM_NO.build());

		gui.create(player);
	}

	Player target;
	ReportReason reason;
	ItemStack yes;

	public ReportGuiConfirm(String name, int rows, Player target, ReportReason reason) {
		super(name, rows);
		this.target = target;
		this.reason = reason;
	}

	@Override
	public boolean onClick(Player player, ItemStack current, int slot, ClickType click) {
		if (current == null) {
			return false;
		}

		if (this.yes.equals(current)) {
			ReportGui.report(player, this.target, this.reason);
		} else if (ITEM_CONFIRM_NO.build().equals(current)) {
			player.closeInventory();
		}

		return false;
	}
}
