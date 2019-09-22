package fr.tristiisch.olympa.core.report;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import fr.tristiisch.olympa.api.customevents.GuiClickEvent;
import fr.tristiisch.olympa.api.gui.OlympaGuiBuild;
import fr.tristiisch.olympa.api.utils.SpigotUtils;
import fr.tristiisch.olympa.core.report.items.ItemGui;

public class ReportListener implements Listener {

	@EventHandler
	public void onGuiClick(GuiClickEvent event) {
		OlympaGuiBuild gui = event.getGui();

		if (!gui.getId().equals("report")) {
			return;
		}

		Player player = event.getPlayer();
		Player target = Bukkit.getPlayer(gui.getData());
		InventoryClickEvent event2 = event.getInventoryClickEvent();
		ItemStack item = event2.getCurrentItem();

		if (item == null) {
			return;
		}

		ItemGui itemGui = ItemGui.get(item);

		switch (itemGui) {
		case CHAT:
			ReportGui.openChat(player, target);
			break;
		case CHEAT:
			ReportGui.openCheat(player, target);
			break;
		case OTHER:
			player.sendMessage(SpigotUtils.color("&4En développement"));
			// ReportGui.openOther(player, target);
			break;
		default:
			break;
		}
	}

}
