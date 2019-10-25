package fr.olympa.core.report.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.gui.CustomInventory;
import fr.olympa.api.gui.OlympaGui;
import fr.olympa.api.item.OlympaItemBuild;
import fr.olympa.core.report.items.ReportReason;

public class ReportGuiConfirm implements CustomInventory {

	OlympaItemBuild ITEM_CONFIRM = new OlympaItemBuild(Material.GREEN_STAINED_GLASS, "&aConfirmer").lore("", "&4[&c!&4] &cVous devez être sûr de votre report", "&cIl sera enregistrer et vous pouvez", "&cêtre sanctionner si vous en abusez");

	@Override
	public boolean onClick(Player player, OlympaGui gui, ItemStack current, int slot, ClickType click) {
		String[] data = ((String) gui.getData()).split(";");
		Player target = Bukkit.getPlayer(data[0]);

		if (target == null) {
			player.sendMessage("&cLe joueur s'est déconnecter, utilisez le forum pour signaler ce joueur.");
			player.closeInventory();
			return false;
		}

		if (current == null) {
			return false;
		}

		ReportReason reason = ReportReason.get(data[1]);
		ReportGui.report(player, target, reason);
		return false;
	}

	public void open(Player player, Player target, ReportReason reason) {
		OlympaGui gui = new OlympaGui("&6Report &e" + target.getName() + "&6 pour &e" + reason.getReason(), "report.confirm", 3);
		gui.setData(target.getUniqueId().toString() + ";" + reason.toString());

		gui.setItem(gui.getMiddleSlot(), this.ITEM_CONFIRM.build());

		gui.openInventory(player);
		this.create(player, gui);
	}

}
