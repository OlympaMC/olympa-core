package fr.tristiisch.olympa.core.report.items;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import fr.tristiisch.olympa.api.item.OlympaItemBuild;

public enum ItemGuiChat {

	SPAM(new OlympaItemBuild(Material.BOOK_AND_QUILL, "&7Spam")),
	PUB(new OlympaItemBuild(Material.BOOK_AND_QUILL, "&7Pub")),
	INSULTE(new OlympaItemBuild(Material.BOOK_AND_QUILL, "&7Insulte"));

	public static ItemGui get(ItemStack itemStack) {
		return Arrays.stream(ItemGui.values()).filter(itemGui -> itemGui.getItem().build().isSimilar(itemStack)).findFirst().orElse(null);
	}

	final OlympaItemBuild item;

	private ItemGuiChat(OlympaItemBuild item) {
		this.item = item;
	}

	public OlympaItemBuild getItem() {
		return this.item;
	}
}
