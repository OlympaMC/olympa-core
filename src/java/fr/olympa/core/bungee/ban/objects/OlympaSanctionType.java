package fr.olympa.core.bungee.ban.objects;

import java.util.Arrays;

public enum OlympaSanctionType {

	BAN(1, "Ban"),
	BANIP(2, "BanIP"),
	MUTE(3, "Mute"),
	KICK(4, "Kick");

	public static OlympaSanctionType getByID(int i) {
		return Arrays.stream(OlympaSanctionType.values()).filter(id -> id.getId() == i).findFirst().orElse(null);
	}

	int id;
	String s;

	private OlympaSanctionType(int id, String s) {
		this.id = id;
		this.s = s;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return s;
	}

	public String getNameForPlayer() {
		if (this == BANIP)
			return BAN.s;
		return s;
	}
}
