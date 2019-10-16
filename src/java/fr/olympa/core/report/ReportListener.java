package fr.olympa.core.report;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.customevents.GuiClickEvent;
import fr.olympa.api.gui.OlympaGuiBuild;
import fr.olympa.core.report.items.ReportReason;

public class ReportListener implements Listener {

	@EventHandler
	public void onGuiClick(GuiClickEvent event) {
		OlympaGuiBuild gui = event.getGui();

		if (!gui.getId().equals("report")) {
			return;
		}

		Player player = event.getPlayer();
		Player target = Bukkit.getPlayer(gui.getData());

		if (target == null) {
			player.sendMessage("&cLe joueur s'est déconnecter, utilisez le forum pour signaler ce joueur.");
			player.closeInventory();
			return;
		}

		InventoryClickEvent event2 = event.getInventoryClickEvent();
		ItemStack item = event2.getCurrentItem();

		if (item == null) {
			return;
		}

		ReportReason reason = ReportReason.get(item);
		ReportGui.openConfirm(player, target, reason);
	}

	@EventHandler
	public void onGuiClick2(GuiClickEvent event) {
		OlympaGuiBuild gui = event.getGui();

		if (!gui.getId().equals("report.confirm")) {
			return;
		}

		Player player = event.getPlayer();
		String[] data = gui.getData().split(";");
		Player target = Bukkit.getPlayer(data[0]);

		if (target == null) {
			player.sendMessage("&cLe joueur s'est déconnecter, utilisez le forum pour signaler ce joueur.");
			player.closeInventory();
			return;
		}

		InventoryClickEvent event2 = event.getInventoryClickEvent();
		ItemStack item = event2.getCurrentItem();

		if (item == null) {
			return;
		}

		ReportReason reason = ReportReason.get(data[1]);
		ReportGui.report(player, target, reason);
	}

}
