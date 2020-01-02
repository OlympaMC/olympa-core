package fr.olympa.core.ban.objects;

import java.util.Arrays;

import net.md_5.bungee.api.ChatColor;

public enum OlympaSanctionStatus {

	EXPIRE(0, "Expiré", ChatColor.GRAY),
	ACTIVE(1, "Actif", ChatColor.RED),
	CANCEL(2, "Annulé", ChatColor.GREEN),
	DELETE(3, "Supprimé", ChatColor.GREEN);

	public static OlympaSanctionStatus getStatus(int i) {
		return Arrays.stream(OlympaSanctionStatus.values()).filter(emeraldBanStatus -> emeraldBanStatus.getId() == i).findFirst().orElse(null);
	}

	int id;

	String name;
	ChatColor color;

	private OlympaSanctionStatus(int id, String name, ChatColor color) {
		this.id = id;
		this.name = name;
		this.color = color;
	}

	public ChatColor getColor() {
		return this.color;
	}

	public int getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public String getNameColored() {
		return this.color + this.name;
	}

	public boolean isStatus(OlympaSanctionStatus status) {
		if (this == status) {
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return String.valueOf(this.id);
	}
}