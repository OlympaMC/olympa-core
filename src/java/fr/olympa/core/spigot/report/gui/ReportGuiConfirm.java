package fr.olympa.core.spigot.report.gui;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.gui.OlympaGUI;
import fr.olympa.api.item.OlympaItemBuild;
import fr.olympa.core.spigot.report.ReportHandler;
import fr.olympa.core.spigot.report.items.ReportReason;

public class ReportGuiConfirm extends OlympaGUI {

	static OlympaItemBuild ITEM_CONFIRM_YES = new OlympaItemBuild(Material.GREEN_STAINED_GLASS, "&aConfirmer").lore("", "", "", "&4[&c!&4] &cVous devez être sûr de votre report", "&cIl sera enregistrer et vous pouvez",
			"&cêtre sanctionner si vous en abusez");

	static OlympaItemBuild ITEM_CONFIRM_NO = new OlympaItemBuild(Material.GREEN_STAINED_GLASS, "&cAnnuler");

	public static void open(Player player, OfflinePlayer target, ReportReason reason) {
		String signalName = "&6Signaler &e" + target.getName();
		String signalReason = "&6pour &e" + reason.getReason();
		ReportGuiConfirm gui = new ReportGuiConfirm(signalName + " " + signalReason, 3, target, reason);
		Inventory guiIventory = gui.getInventory();
		int slot = guiIventory.getSize() / 2 - 1;
		gui.yes = ITEM_CONFIRM_YES.setLore(1, "&cRaison: &4" + reason.getReason()).build();
		guiIventory.setItem(slot, gui.yes);
		guiIventory.setItem(slot + 2, ITEM_CONFIRM_NO.build());
		guiIventory.setItem(slot - 7, new OlympaItemBuild(signalName).lore("", signalReason).skullowner(target).build());

		gui.create(player);
	}

	OfflinePlayer target;
	ReportReason reason;
	ItemStack yes;

	public ReportGuiConfirm(String name, int rows, OfflinePlayer target, ReportReason reason) {
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
			ReportHandler.report(player, this.target, this.reason);
		} else if (current != null) {
			player.closeInventory();
		}
		return false;
	}
}
