package fr.tristiisch.olympa.core.report;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;

import fr.tristiisch.olympa.api.gui.OlympaGuiBuild;
import fr.tristiisch.olympa.api.item.OlympaItemBuild;
import fr.tristiisch.olympa.core.report.items.ItemGui;
import fr.tristiisch.olympa.core.report.items.ItemGuiChat;
import fr.tristiisch.olympa.core.report.items.ItemGuiCheat;

public class ReportGui {

	public static void open(Player player, Player target) {
		OlympaGuiBuild gui = new OlympaGuiBuild("&6Report &e" + target.getName(), "report", 3);
		gui.setData(target.getUniqueId().toString());

		List<OlympaItemBuild> items = Arrays.stream(ItemGui.values()).map(ItemGui::getItem).collect(Collectors.toList());
		int slot = gui.getMiddleSlot() - items.size() / 2;
		for (OlympaItemBuild item : items) {
			gui.setItem(slot++, item.build());
		}
		gui.openInventory(player);
	}

	public static void openChat(Player player, Player target) {
		OlympaGuiBuild gui = new OlympaGuiBuild("&6Report &e" + target.getName() + " &l&6| &eChat", "report.chat", 3);
		gui.setData(target.getUniqueId().toString());

		List<OlympaItemBuild> items = Arrays.stream(ItemGuiChat.values()).map(ItemGuiChat::getItem).collect(Collectors.toList());
		int slot = gui.getMiddleSlot() - items.size() / 2;
		for (OlympaItemBuild item : items) {
			gui.setItem(slot++, item.build());
		}
		gui.openInventory(player);
	}

	public static void openCheat(Player player, Player target) {
		OlympaGuiBuild gui = new OlympaGuiBuild("&6Report &e" + target.getName() + " &l&6| &eCheat", "report.cheat", 3);
		gui.setData(target.getUniqueId().toString());

		List<OlympaItemBuild> items = Arrays.stream(ItemGuiCheat.values()).map(ItemGuiCheat::getItem).collect(Collectors.toList());
		int slot = gui.getMiddleSlot() - items.size() / 2;
		for (OlympaItemBuild item : items) {
			gui.setItem(slot++, item.build());
		}
		gui.openInventory(player);
	}

	public static void openOther(Player player, Player target) {

	}
}
