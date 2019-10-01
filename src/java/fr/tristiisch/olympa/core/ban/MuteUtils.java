package fr.tristiisch.olympa.core.ban;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import fr.tristiisch.olympa.api.utils.Utils;
import fr.tristiisch.olympa.core.ban.objects.OlympaSanction;
import fr.tristiisch.olympa.core.ban.objects.OlympaSanctionType;

public class MuteUtils {

	static private List<OlympaSanction> mutes = new ArrayList<>();

	public static void addMute(final OlympaSanction mute) {
		mutes.add(mute);
	}

	public static boolean chechExpireBan(final OlympaSanction mute) {
		if (Utils.getCurrentTimeinSeconds() > mute.getExpires()) {
			removeMute(mute);
			BanMySQL.expireSanction(mute);
			return true;
		}
		return false;
	}

	public static List<OlympaSanction> getMute() {
		return mutes;
	}

	public static OlympaSanction getMute(final UUID uuid) {
		return mutes.stream().filter(emeraldMute -> String.valueOf(emeraldMute.getPlayer()).equals(String.valueOf(uuid))).findFirst().orElse(null);
	}

	public static OlympaSanction getMuteFromMySQL(final UUID uuid) {
		return BanMySQL.getSanctionActive(uuid, OlympaSanctionType.MUTE);
	}

	public static void removeMute(final OlympaSanction mute) {
		mutes.remove(mute);
	}

	public static void removeMute(final UUID uuid) {
		removeMute(getMute(uuid));
	}

}
