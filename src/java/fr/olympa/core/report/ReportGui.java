package fr.olympa.core.report;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import fr.olympa.api.gui.OlympaGuiBuild;
import fr.olympa.api.item.OlympaItemBuild;
import fr.olympa.core.report.items.ReportReason;

public class ReportGui {

	public static void open(Player player, Player target) {
		OlympaGuiBuild gui = new OlympaGuiBuild("&6Report &e" + target.getName(), "report", 3);
		gui.setData(target.getUniqueId().toString());

		List<OlympaItemBuild> items = Arrays.stream(ReportReason.values()).map(ReportReason::getItem).collect(Collectors.toList());
		int slot = gui.getMiddleSlot() - items.size() / 2;
		for (OlympaItemBuild item : items) {
			gui.setItem(slot++, item.build());
		}
		gui.openInventory(player);
	}

	public static void openConfirm(Player player, Player target, ReportReason reason) {
		OlympaGuiBuild gui = new OlympaGuiBuild("&6Report &e" + target.getName() + "&6 pour &e" + reason.getReason(), "report.confirm", 3);
		gui.setData(target.getUniqueId().toString() + ";" + reason.toString());

		OlympaItemBuild item = new OlympaItemBuild(Material.GREEN_STAINED_GLASS, "&aConfirmer")
				.lore("", "&4[&c!&4] &cVous devez être sûr de votre report", "&cIl sera enregistrer et vous pouvez", "&cêtre sanctionner si vous en abusez");
		gui.setItem(gui.getMiddleSlot(), item.build());
		gui.openInventory(player);
	}

	public static void report(Player player, Player target, ReportReason reason) {
		OlympaReport report = new OlympaReport(target.getUniqueId(), player.getUniqueId(), reason, "serverName");
		// report.save();
	}
}
