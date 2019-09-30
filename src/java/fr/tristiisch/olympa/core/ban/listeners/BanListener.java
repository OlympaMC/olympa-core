package fr.tristiisch.olympa.core.ban.listeners;

import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;

import fr.tristiisch.olympa.core.ban.BanMySQL;
import fr.tristiisch.olympa.core.ban.objects.EmeraldBan;
import fr.tristiisch.olympa.core.ban.objects.EmeraldBanType;

public class BanListener implements Listener {

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerPreLogin(final AsyncPlayerPreLoginEvent event) {
		final UUID playerUUID = event.getUniqueId();
		final String playerIp = event.getAddress().getHostAddress();

		EmeraldBan ban = BanMySQL.getActiveSanction(playerUUID, EmeraldBanType.BAN);

		if (ban == null) {
			ban = BanMySQL.getActiveSanction(playerIp, EmeraldBanType.BANIP);
		}

		if (ban != null) {
			if (ban.isPermanent()) {
				event.disallow(Result.KICK_BANNED, "");
				event.setCancelReason(BungeeUtils.connectScreen(BungeeConfigUtils.getString("bungee.ban.messages.bandisconnect")
						.replaceAll("%reason%", ban.getReason())
						.replaceAll("%id%", String.valueOf(ban.getId()))));
				/*
				 * } else if(ban.getExpires() < Utils.getCurrentTimeinSeconds()){
				 * event.setCancelReason(BungeeUtils.
				 * connectScreen("\n&6&lVous avez été récemment banni pour &e&l" +
				 * ban.getReason() +
				 * "\n\n&6Sachez que si vous recommencez, vous serez doublement banni.\n\n&2&n&lVous pouvez maintenant vous reconnectez"
				 * )); BanMySQL.expireBan(ban);
				 */
			} else {
				event.disallow(Result.KICK_BANNED, "");
				event.setCancelReason(BungeeUtils.connectScreen(BungeeConfigUtils.getString("bungee.ban.messages.tempbandisconnect")
						.replaceAll("%reason%", ban.getReason())
						.replaceAll("%time%", Utils.timestampToDuration(ban.getExpires()))
						.replaceAll("%id%", String.valueOf(ban.getId()))));
			}
			return;
		}
	}
}
