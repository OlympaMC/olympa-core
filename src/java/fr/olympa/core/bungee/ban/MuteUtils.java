package fr.olympa.core.bungee.ban;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.ban.objects.OlympaSanction;
import fr.olympa.core.bungee.ban.objects.OlympaSanctionType;

public class MuteUtils {

	static private List<OlympaSanction> mutes = new ArrayList<>();

	public static void addMute(OlympaSanction mute) {
		mutes.add(mute);
	}

	public static boolean chechExpireBan(OlympaSanction mute) {
		if (Utils.getCurrentTimeInSeconds() > mute.getExpires()) {
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
		return mutes.stream().filter(olympaMute -> String.valueOf(olympaMute.getPlayer()).equals(String.valueOf(uuid))).findFirst().orElse(null);
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
