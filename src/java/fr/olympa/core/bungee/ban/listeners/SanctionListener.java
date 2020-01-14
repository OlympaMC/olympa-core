package fr.olympa.core.bungee.ban.listeners;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import fr.olympa.api.utils.SpigotUtils;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.ban.BanMySQL;
import fr.olympa.core.bungee.ban.MuteUtils;
import fr.olympa.core.bungee.ban.objects.OlympaSanction;
import fr.olympa.core.bungee.ban.objects.OlympaSanctionType;
import fr.olympa.core.bungee.utils.BungeeConfigUtils;
import fr.olympa.core.bungee.utils.BungeeUtils;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

// TODO mute + ban = 1 request (at this moment this = 2)
public class SanctionListener implements Listener {

	private List<String> commandDisableWhenMuted = new ArrayList<>();

	/**public MuteListener() {
		this.commandDisableWhenMuted.addAll(PrivateMessage.privateMessageCommand);
		this.commandDisableWhenMuted.addAll(PrivateMessage.replyCommand);
	}*/

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerChat(ChatEvent event) {
		if (event.isCancelled()) {
			return;
		}
		if (!(event.getSender() instanceof ProxiedPlayer)) {
			return;
		}
		String command = event.getMessage().substring(1);
		if (event.getMessage().startsWith("/") && !this.commandDisableWhenMuted.contains(command)) {
			return;
		}
		ProxiedPlayer player = (ProxiedPlayer) event.getSender();

		OlympaSanction mute = MuteUtils.getMute(player.getUniqueId());
		if (mute != null) {
			if (!MuteUtils.chechExpireBan(mute)) {
				player.sendMessage(
						BungeeConfigUtils.getString("ban.youaremuted").replace("%reason%", mute.getReason()).replace("%expire%", Utils.timestampToDuration(mute.getExpires())));
				event.setCancelled(true);
				return;
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerLogin(PostLoginEvent event) {
		OlympaBungee.getInstance().getTask().runAsync(OlympaBungee.getInstance(), () -> {
			ProxiedPlayer player = event.getPlayer();
			OlympaSanction mute = MuteUtils.getMute(player.getUniqueId());
			if (mute == null) {
				mute = BanMySQL.getSanctionActive(player.getUniqueId(), OlympaSanctionType.MUTE);
				if (mute != null) {
					MuteUtils.addMute(mute);
				}
			}
		});
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerPreLogin(LoginEvent event) {
		if (event.isCancelled()) {
			return;
		}
		PendingConnection connection = event.getConnection();
		UUID playerUUID = connection.getUniqueId();
		String playerIp = connection.getAddress().getAddress().getHostAddress();

		OlympaSanction ban;
		try {
			ban = BanMySQL.getBanActive(playerUUID, playerIp);
		} catch (SQLException e) {
			event.setCancelReason(BungeeUtils.connectScreen("§cUne erreur est survenue. \n\n§e§lMerci la signaler au staff.\n§eCode d'erreur: §l#SQLBungeeSanction"));
			e.printStackTrace();
			return;
		}
		if (ban == null) {
			return;
		}

		if (ban.isPermanent()) {
			event.setCancelReason(SpigotUtils.connectScreen(BungeeConfigUtils.getString("ban.bandisconnect")
					.replaceAll("%reason%", ban.getReason())
					.replaceAll("%id%", String.valueOf(ban.getId()))));
		} else {
			event.setCancelReason(SpigotUtils.connectScreen(BungeeConfigUtils.getString("ban.tempbandisconnect")
					.replaceAll("%reason%", ban.getReason())
					.replaceAll("%time%", Utils.timestampToDuration(ban.getExpires()))
					.replaceAll("%id%", String.valueOf(ban.getId()))));
		}
	}
}
