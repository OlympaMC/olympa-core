package fr.tristiisch.olympa.core.report.items;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import fr.tristiisch.olympa.api.item.OlympaItemBuild;

public enum ItemGui {

	CHAT(new OlympaItemBuild(Material.BOOK_AND_QUILL, "&7Chat")),
	CHEAT(new OlympaItemBuild(Material.GOLD_SWORD, "&7Cheat")),
	OTHER(new OlympaItemBuild(Material.CAULDRON, "&7Autre"));

	public static ItemGui get(ItemStack itemStack) {
		return Arrays.stream(ItemGui.values()).filter(itemGui -> itemGui.getItem().build().isSimilar(itemStack)).findFirst().orElse(null);
	}

	final OlympaItemBuild item;

	private ItemGui(OlympaItemBuild item) {
		this.item = item;
	}

	public OlympaItemBuild getItem() {
		return this.item;
	}
}
