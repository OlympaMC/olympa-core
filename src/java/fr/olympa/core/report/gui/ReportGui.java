package fr.olympa.core.report.gui;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.gui.CustomInventory;
import fr.olympa.api.gui.OlympaGui;
import fr.olympa.api.item.OlympaItemBuild;
import fr.olympa.core.report.OlympaReport;
import fr.olympa.core.report.items.ReportReason;

public class ReportGui implements CustomInventory {

	public static void report(Player player, Player target, ReportReason reason) {
		OlympaReport report = new OlympaReport(target.getUniqueId(), player.getUniqueId(), reason, "serverName");
		// report.save();
	}

	@Override
	public boolean onClick(Player player, OlympaGui gui, ItemStack current, int slot, ClickType click) {
		Player target = Bukkit.getPlayer((String) gui.getData());

		if (target == null) {
			player.sendMessage("&cLe joueur s'est déconnecter, utilisez le forum pour signaler ce joueur.");
			player.closeInventory();
			return false;
		}

		if (current == null) {
			return false;
		}

		ReportReason reason = ReportReason.get(current);
		new ReportGuiConfirm().open(player, target, reason);
		return false;
	}

	public void open(Player player, Player target) {
		OlympaGui gui = new OlympaGui("&6Report &e" + target.getName(), "report", 3);
		gui.setData(target.getUniqueId().toString());

		List<OlympaItemBuild> items = Arrays.stream(ReportReason.values()).map(ReportReason::getItem).collect(Collectors.toList());
		int slot = gui.getMiddleSlot() - items.size() / 2;
		for (OlympaItemBuild item : items) {
			gui.setItem(slot++, item.build());
		}
		gui.openInventory(player);

		this.create(player, gui);
	}
}
