package fr.tristiisch.olympa.core.report.items;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import fr.tristiisch.olympa.api.item.OlympaItemBuild;

public enum ItemGuiCheat {

	COMBAT(new OlympaItemBuild(Material.GOLD_SWORD, "&7Combat (KillAura, Aimbot, TriggerBot, AutoClick ...)")),
	FLY(new OlympaItemBuild(Material.FEATHER, "&7Fly")),
	XRAY(new OlympaItemBuild(Material.DIAMOND_ORE, "&7Xray"));

	public static ItemGui get(ItemStack itemStack) {
		return Arrays.stream(ItemGui.values()).filter(itemGui -> itemGui.getItem().build().isSimilar(itemStack)).findFirst().orElse(null);
	}

	final OlympaItemBuild item;

	private ItemGuiCheat(OlympaItemBuild item) {
		this.item = item;
	}

	public OlympaItemBuild getItem() {
		return this.item;
	}
}
