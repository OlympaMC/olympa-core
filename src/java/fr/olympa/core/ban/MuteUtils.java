package fr.olympa.core.ban;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import fr.olympa.api.utils.Utils;
import fr.olympa.core.ban.objects.OlympaSanction;
import fr.olympa.core.ban.objects.OlympaSanctionType;

public class MuteUtils {

	static private List<OlympaSanction> mutes = new ArrayList<>();

	public static void addMute(OlympaSanction mute) {
		mutes.add(mute);
	}

	public static boolean chechExpireBan(OlympaSanction mute) {
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

	public static OlympaSanction getMute(UUID uuid) {
		return mutes.stream().filter(emeraldMute -> String.valueOf(emeraldMute.getPlayer()).equals(String.valueOf(uuid))).findFirst().orElse(null);
	}

	public static OlympaSanction getMuteFromMySQL(UUID uuid) {
		return BanMySQL.getSanctionActive(uuid, OlympaSanctionType.MUTE);
	}

	public static void removeMute(OlympaSanction mute) {
		mutes.remove(mute);
	}

	public static void removeMute(UUID uuid) {
		removeMute(getMute(uuid));
	}

}
