package fr.olympa.core.bungee.ban.objects;

import java.util.Arrays;

import net.md_5.bungee.api.ChatColor;

public enum OlympaSanctionStatus {

	EXPIRE("Expiré", ChatColor.GRAY, "exp"),
	ACTIVE("Actif", ChatColor.RED, ""),
	END("Fini", ChatColor.GRAY, "end"),
	CANCEL("Annulé", ChatColor.GREEN, "un"),
	DELETE("Supprimé", ChatColor.GREEN, "del");

	public static OlympaSanctionStatus getStatus(int i) {
		return Arrays.stream(OlympaSanctionStatus.values()).filter(olympaBanStatus -> olympaBanStatus.getId() == i).findFirst().orElse(null);
	}

	String name;
	ChatColor color;
	String prefix;

	private OlympaSanctionStatus(String name, ChatColor color, String prefix) {
		this.name = name;
		this.color = color;
		this.prefix = prefix;
	}

	public ChatColor getColor() {
		return color;
	}

	public String getPrefix() {
		return prefix;
	}

	public int getId() {
		return ordinal();
	}

	public String getName() {
		return name;
	}

	public String getNameColored() {
		return color + name;
	}

	public boolean isStatus(OlympaSanctionStatus status) {
		if (this == status)
			return true;
		return false;
	}

	@Override
	public String toString() {
		return String.valueOf(ordinal());
	}
}