package fr.tristiisch.olympa.core.ban;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import fr.tristiisch.olympa.api.utils.Utils;
import fr.tristiisch.olympa.core.ban.objects.EmeraldBan;
import fr.tristiisch.olympa.core.ban.objects.EmeraldBanType;

public class MuteUtils {

	static private List<EmeraldBan> mutes = new ArrayList<>();

	public static void addMute(final EmeraldBan mute) {
		mutes.add(mute);
	}

	public static boolean chechExpireBan(final EmeraldBan mute) {
		if (Utils.getCurrentTimeinSeconds() > mute.getExpires()) {
			removeMute(mute);
			BanMySQL.expireBan(mute);
			return true;
		}
		return false;
	}

	public static List<EmeraldBan> getMute() {
		return mutes;
	}

	public static EmeraldBan getMute(final UUID uuid) {
		return mutes.stream().filter(emeraldMute -> String.valueOf(emeraldMute.getPlayer()).equals(String.valueOf(uuid))).findFirst().orElse(null);
	}

	public static EmeraldBan getMuteFromMySQL(final UUID uuid) {
		return BanMySQL.getActiveSanction(uuid, EmeraldBanType.MUTE);
	}

	public static void removeMute(final EmeraldBan mute) {
		mutes.remove(mute);
	}

	public static void removeMute(final UUID uuid) {
		removeMute(getMute(uuid));
	}

}
