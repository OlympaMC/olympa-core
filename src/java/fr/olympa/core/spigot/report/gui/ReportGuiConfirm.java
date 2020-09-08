package fr.olympa.core.spigot.report.gui;

import java.sql.SQLException;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.gui.OlympaGUI;
import fr.olympa.api.item.OlympaItemBuild;
import fr.olympa.api.report.ReportReason;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.spigot.report.ReportHandler;

public class ReportGuiConfirm extends OlympaGUI {

	static OlympaItemBuild ITEM_CONFIRM_YES = new OlympaItemBuild(Material.GREEN_STAINED_GLASS_PANE, "&aConfirmer").lore(
			"", "&4[&c!&4] &cVous devez être sûr de votre report", "   &cIl sera enregistrer et vous pouvez", "   &cêtre sanctionner si vous en abusez");

	static OlympaItemBuild ITEM_CONFIRM_NO = new OlympaItemBuild(Material.RED_STAINED_GLASS_PANE, "&cAnnuler").lore("&4et revenir en arrière");

	public static void open(Player player, OfflinePlayer target, ReportReason reason, String note) {
		String signalName = "&e" + target.getName();
		String signalReason = "&6→ &e" + reason.getReason();
		ReportGuiConfirm gui = new ReportGuiConfirm(signalName + " " + signalReason, 3, target, reason, note);
		Inventory guiIventory = gui.getInventory();
		int slot = guiIventory.getSize() / 2 - 1;
		OlympaItemBuild yesBuild = ITEM_CONFIRM_YES.clone();
		if (note != null)
			yesBuild.addLoreBefore("&cNote: &4" + note);
		yesBuild.addLoreBefore("&cRaison: &4" + reason.getReason());
		gui.yes = yesBuild.build();
		guiIventory.setItem(slot, gui.yes);
		guiIventory.setItem(slot + 2, ITEM_CONFIRM_NO.build());
		guiIventory.setItem(slot - 8, new OlympaItemBuild(signalName).lore("", signalReason).skullowner(target).build());

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
		if (current == null)
			return true;
		if (yes.equals(current)) {
			try {
				ReportHandler.report(player, target, reason, note);
			} catch (SQLException e) {
				e.printStackTrace();
				player.sendMessage(Prefix.DEFAULT_BAD.formatMessage("Une erreur est survenu, ton report n'a pas été enregistrer ..."));
			}
			player.closeInventory();
		} else if (ITEM_CONFIRM_NO.build().equals(current))
			ReportGui.open(player, target, null);
		return true;
	}
}
