package fr.olympa.spigot.core.report.items;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.item.OlympaItemBuild;

public enum ReportReason {

	CHAT(1, "Chat abusif", new OlympaItemBuild(Material.BOOK, "&7Chat abusif").lore("", "&eSpam, Insulte, Provocations, Publicité ...")),
	CHEAT_AURA(2, "Cheat Combat", new OlympaItemBuild(Material.GOLDEN_SWORD, "&7Cheat Combat").lore("", "&eKillAura, Aimbot, TriggerBot, AutoClick ...")),
	CHEAT_XRAY(3, "Cheat XRay", new OlympaItemBuild(Material.DIAMOND_ORE, "&7XRay")),
	CHEAT_FLY(4, "Cheat Fly", new OlympaItemBuild(Material.FEATHER, "&7Fly")),
	OTHER(5, "Autre", new OlympaItemBuild(Material.CAULDRON, "&7Autre"));

	public static ReportReason get(ItemStack itemStack) {
		return Arrays.stream(ReportReason.values()).filter(itemGui -> itemGui.getItem().build().isSimilar(itemStack)).findFirst().orElse(null);
	}

	public static ReportReason get(String name) {
		return Arrays.stream(ReportReason.values()).filter(itemGui -> itemGui.name().equals(name)).findFirst().orElse(null);
	}

	int id;
	String reason;
	OlympaItemBuild item;

	private ReportReason(int id, String reason, OlympaItemBuild item) {
		this.id = id;
		this.reason = reason;
		this.item = item;
	}

	public int getId() {
		return this.id;
	}

	public OlympaItemBuild getItem() {
		return this.item;
	}

	public String getReason() {
		return this.reason;
	}
}
