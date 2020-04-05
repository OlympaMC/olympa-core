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

	public static void open(Player player, OfflinePlayer target, ReportReason reason, String note) {
		String signalName = "&6Signaler &e" + target.getName();
		String signalReason = "&6pour &e" + reason.getReason();
		ReportGuiConfirm gui = new ReportGuiConfirm(signalName + " " + signalReason, 3, target, reason, note);
		Inventory guiIventory = gui.getInventory();
		int slot = guiIventory.getSize() / 2 - 1;
		OlympaItemBuild yesBuild = ITEM_CONFIRM_YES.lore("&cRaison: &4" + reason.getReason());
		if (note != null) {
			yesBuild = yesBuild.addlore("&cNote: &4" + note);
		}
		gui.yes = yesBuild.build();
		guiIventory.setItem(slot, gui.yes);
		guiIventory.setItem(slot + 2, ITEM_CONFIRM_NO.build());
		guiIventory.setItem(slot - 7, new OlympaItemBuild(signalName).lore("", signalReason).skullowner(target).build());

		gui.create(player);
	}

	OfflinePlayer target;
	ReportReason reason;
	String note;
	ItemStack yes;

	public ReportGuiConfirm(String name, int rows, OfflinePlayer target, ReportReason reason, String note) {
		super(name, rows);
		this.target = target;
		this.reason = reason;
		this.note = note;
	}

	@Override
	public boolean onClick(Player player, ItemStack current, int slot, ClickType click) {
		if (current == null) {
			return false;
		}
		if (yes.equals(current)) {
			ReportHandler.report(player, target, reason, note);
		} else if (ITEM_CONFIRM_NO.build().equals(current)) {
			player.closeInventory();
		}
		return false;
	}
}
