package fr.olympa.core.bungee.ban;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.ban.objects.OlympaSanction;
import fr.olympa.core.bungee.ban.objects.OlympaSanctionHistory;
import fr.olympa.core.bungee.ban.objects.OlympaSanctionStatus;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class SanctionHandler {

	public static int maxTimeBan = 527040;
	public static int minTimeBan = 60;

	public static int maxTimeMute = 10080;
	public static int minTimeMute = 15;

	static private List<OlympaSanction> mutes = new ArrayList<>();

	public static void addMute(OlympaSanction mute) {
		mutes.add(mute);
	}

	public static OlympaSanction isMuted(ProxiedPlayer player, Function<? super OlympaSanction, ? extends OlympaSanction> mapper) {
		OlympaSanction mute;
		List<OlympaSanction> mutes = getMutes(player.getUniqueId());
		Set<OlympaSanction> mutes2 = mutes.stream().map(mapper).collect(Collectors.toSet());
		mutes.removeAll(mutes2.stream().filter(m -> m.getStatus() != OlympaSanctionStatus.ACTIVE).collect(Collectors.toSet()));
		mute = mutes2.stream().filter(m -> m.getStatus() == OlympaSanctionStatus.ACTIVE).findFirst().orElse(null);
		return mute;
	}

	public static OlympaSanction isMutedThenExpire(ProxiedPlayer player) {
		return isMuted(player, SanctionHandler::expireIfNeeded);
	}

	public static OlympaSanction isMutedThenEnd(ProxiedPlayer player) {
		return isMuted(player, SanctionHandler::endIfNeeded);
	}

	//	@Deprecated
	//	public static boolean chechExpireBan(OlympaSanction mute) throws SQLException {
	//		if (mute.getExpires() != 0 && Utils.getCurrentTimeInSeconds() > mute.getExpires()) {
	//			removeMute(mute);
	//			BanMySQL.changeStatus(new OlympaSanctionHistory(OlympaSanctionStatus.EXPIRE), mute.getId());
	//			return true;
	//		}
	//		return false;
	//	}

	public static OlympaSanction expireIfNeeded(OlympaSanction sanction) {
		OlympaSanctionStatus status = sanction.getStatus();
		if (status == OlympaSanctionStatus.ACTIVE && sanction.getExpires() != 0 && Utils.getCurrentTimeInSeconds() >= sanction.getExpires())
			return changeStatus(sanction, new OlympaSanctionHistory(OlympaSanctionStatus.EXPIRE));
		return sanction;
	}

	public static OlympaSanction endIfNeeded(OlympaSanction sanction) {
		OlympaSanctionStatus status = sanction.getStatus();
		if ((status == OlympaSanctionStatus.ACTIVE || status == OlympaSanctionStatus.EXPIRE) && sanction.getExpires() != 0 && Utils.getCurrentTimeInSeconds() >= sanction.getExpires())
			return changeStatus(sanction, new OlympaSanctionHistory(OlympaSanctionStatus.END));
		return sanction;
	}

	public static boolean isExpired(OlympaSanction sanction) {
		OlympaSanctionStatus status = sanction.getStatus();
		if (status == OlympaSanctionStatus.EXPIRE || status == OlympaSanctionStatus.END)
			return true;
		if (status != OlympaSanctionStatus.ACTIVE)
			return false;
		if (sanction.getExpires() != 0 && Utils.getCurrentTimeInSeconds() > sanction.getExpires()) {
			//			removeMute(sanction); // only for mute
			changeStatus(sanction, new OlympaSanctionHistory(OlympaSanctionStatus.EXPIRE));
			return true;
		}
		return false;
	}

	public static OlympaSanction changeStatus(OlympaSanction sanction, OlympaSanctionHistory banhistory) {
		sanction.addHistory(banhistory);
		sanction.setStatus(banhistory.getStatus());
		if (!BanMySQL.changeStatus(banhistory, sanction.getId()))
			return null;
		return sanction;
	}

	public static List<OlympaSanction> getMute() {
		return mutes;
	}

	//	@SuppressWarnings("deprecation")
	//	public static OlympaSanction getMute(ProxiedPlayer player) {
	//		return getMute(AccountProvider.getPlayerInformations(player.getUniqueId()).getId(), player.getAddress().getHostName());
	//	}

	public static OlympaSanction getMute(long olympaId, String ip) {
		return mutes.stream().filter(olympaMute -> olympaMute.getTargetId() != null && olympaMute.getTargetId() == olympaId || olympaMute.getTargetIp() != null && olympaMute.getTargetIp().equals(ip)).findFirst().orElse(null);
	}

	// ID or IP
	public static OlympaSanction getMute(String target) {
		return mutes.stream().filter(olympaMute -> olympaMute.getTarget().equals(target)).findFirst().orElse(null);
	}

	public static List<OlympaSanction> getMutes(UUID uuid) {
		return mutes.stream().filter(olympaMute -> olympaMute.getPlayersInfos().stream().anyMatch(opi -> opi.getUUID().equals(uuid))).collect(Collectors.toList());
	}

	public static OlympaSanction getMute(OlympaPlayer olympaPlayer) {
		return mutes.stream().filter(olympaMute -> olympaMute.isTarget(olympaPlayer)).findFirst().orElse(null);
	}

	public static OlympaSanction getMute(long olympaPlayerId) {
		return mutes.stream().filter(olympaMute -> olympaMute.getTargetId() != null && olympaMute.getTargetId() == olympaPlayerId).findFirst().orElse(null);
	}

	public static void removeMute(OlympaSanction mute) {
		mutes.remove(mute);
	}

	//	public static void removeMute(OlympaPlayer olympaPlayer) {
	//		removeMute(getMute(olympaPlayer));
	//	}
	public static boolean removeMute(String target) {
		OlympaSanction mute = getMute(target);
		if (mute.getPlayersInfos().size() > 1)
			return false;
		removeMute(mute);
		return true;
	}

	//	public static void removeMute(ProxiedPlayer player) {
	//		removeMute(getMute(player.getUniqueId()));
	//	}

}
