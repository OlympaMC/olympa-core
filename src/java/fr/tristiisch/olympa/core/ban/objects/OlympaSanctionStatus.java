package fr.tristiisch.olympa.core.ban.objects;

import java.util.Arrays;

import net.md_5.bungee.api.ChatColor;

public enum OlympaSanctionStatus {

	EXPIRE(0, "Expiré", ChatColor.GRAY),
	ACTIVE(1, "Actif", ChatColor.RED),
	CANCEL(2, "Annulé", ChatColor.GREEN),
	DELETE(3, "Supprimé", ChatColor.GREEN);

	public static OlympaSanctionStatus getStatus(final int i) {
		return Arrays.stream(OlympaSanctionStatus.values()).filter(emeraldBanStatus -> emeraldBanStatus.getId() == i).findFirst().orElse(null);
	}

	final int id;

	final String name;
	final ChatColor color;

	private OlympaSanctionStatus(final int id, final String name, final ChatColor color) {
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

	public boolean isStatus(final OlympaSanctionStatus status) {
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