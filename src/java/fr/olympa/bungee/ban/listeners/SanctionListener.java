package fr.olympa.bungee.ban.listeners;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerLoginEvent;

import fr.olympa.OlympaCore;
import fr.olympa.api.utils.SpigotUtils;
import fr.olympa.api.utils.Utils;
import fr.olympa.bungee.ban.BanMySQL;
import fr.olympa.bungee.ban.MuteUtils;
import fr.olympa.bungee.ban.objects.OlympaSanction;
import fr.olympa.bungee.ban.objects.OlympaSanctionType;

// TODO mute + ban = 1 request (at this moment this = 2)
public class SanctionListener implements Listener {

	private List<String> commandDisableWhenMuted = new ArrayList<>();

	/**public MuteListener() {
		this.commandDisableWhenMuted.addAll(PrivateMessage.privateMessageCommand);
		this.commandDisableWhenMuted.addAll(PrivateMessage.replyCommand);
	}*/

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		String command = event.getMessage().substring(1);
		if (event.getMessage().startsWith("/") && !this.commandDisableWhenMuted.contains(command)) {
			return;
		}
		Player player = event.getPlayer();

		OlympaSanction mute = MuteUtils.getMute(player.getUniqueId());
		if (mute != null) {
			if (!MuteUtils.chechExpireBan(mute)) {
				player.sendMessage(
						OlympaCore.getInstance().getConfig().getString("ban.youaremuted").replace("%reason%", mute.getReason()).replace("%expire%", Utils.timestampToDuration(mute.getExpires())));
				event.setCancelled(true);
				return;
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onPlayerLogin(PlayerLoginEvent event) {
		OlympaCore.getInstance().getTask().runTaskAsynchronously(() -> {
			Player player = event.getPlayer();
			OlympaSanction mute = MuteUtils.getMute(player.getUniqueId());
			if (mute == null) {
				mute = BanMySQL.getSanctionActive(player.getUniqueId(), OlympaSanctionType.MUTE);
				if (mute != null) {
					MuteUtils.addMute(mute);
				}
			}
		});
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
		UUID playerUUID = event.getUniqueId();
		String playerIp = event.getAddress().getHostAddress();

		OlympaSanction ban;
		try {
			ban = BanMySQL.getBanActive(playerUUID, playerIp);
		} catch (SQLException e) {
			event.disallow(Result.KICK_OTHER, SpigotUtils.connectScreen("&cImpossible de se connecter pour le moment, réessaye plus tard ..."));
			e.printStackTrace();
			return;
		}
		if (ban == null) {
			return;
		}

		if (ban.isPermanent()) {
			event.disallow(Result.KICK_BANNED, SpigotUtils.connectScreen(OlympaCore.getInstance().getConfig().getString("ban.bandisconnect")
					.replaceAll("%reason%", ban.getReason())
					.replaceAll("%id%", String.valueOf(ban.getId()))));
		} else {
			event.disallow(Result.KICK_BANNED, SpigotUtils.connectScreen(OlympaCore.getInstance().getConfig().getString("ban.tempbandisconnect")
					.replaceAll("%reason%", ban.getReason())
					.replaceAll("%time%", Utils.timestampToDuration(ban.getExpires()))
					.replaceAll("%id%", String.valueOf(ban.getId()))));
		}
	}
}
