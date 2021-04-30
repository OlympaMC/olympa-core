package fr.olympa.core.bungee.ban.listeners;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.ban.BanMySQL;
import fr.olympa.core.bungee.ban.SanctionHandler;
import fr.olympa.core.bungee.ban.SanctionUtils;
import fr.olympa.core.bungee.ban.objects.OlympaSanction;
import fr.olympa.core.bungee.ban.objects.OlympaSanctionType;
import fr.olympa.core.bungee.datamanagment.CachePlayer;
import fr.olympa.core.bungee.datamanagment.DataHandler;
import fr.olympa.core.bungee.privatemessage.PrivateMessage;
import fr.olympa.core.bungee.utils.BungeeUtils;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

@SuppressWarnings("deprecation")
public class SanctionListener implements Listener {

	private List<String> commandDisableWhenMuted = new ArrayList<>();

	public SanctionListener() {
		commandDisableWhenMuted.addAll(PrivateMessage.privateMessageCommand);
		commandDisableWhenMuted.addAll(PrivateMessage.replyCommand);
	}

	@EventHandler
	public void on2Disconnect(PlayerDisconnectEvent event) {
		ProxiedPlayer player = event.getPlayer();
		List<OlympaSanction> playerMutes = SanctionHandler.getMutes(player.getUniqueId());
		playerMutes.forEach(mute -> SanctionHandler.removeMute(mute));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void on1Login(LoginEvent event) {
		if (event.isCancelled())
			return;
		PendingConnection connection = event.getConnection();
		CachePlayer cache = DataHandler.get(connection.getName());
		long playerId = cache.getOlympaPlayer().getId();
		String playerIp = connection.getAddress().getAddress().getHostAddress();

		List<OlympaSanction> sanctions;
		try {
			sanctions = BanMySQL.getSanctionsActive(playerId, playerIp);
		} catch (SQLException e) {
			event.setCancelReason(BungeeUtils.connectScreen("§cUne erreur est survenue. \n\n§e§lMerci la signaler au staff.\n§eCode d'erreur: §l#SQLBungeeSanction"));
			event.setCancelled(true);
			e.printStackTrace();
			return;
		}
		if (sanctions.isEmpty())
			return;
		List<OlympaSanction> bans = sanctions.stream().filter(sanction -> sanction.getType() == OlympaSanctionType.BAN || sanction.getType() == OlympaSanctionType.BANIP).collect(Collectors.toList());
		if (!bans.isEmpty()) {
			OlympaSanction ban = bans.stream().sorted((s1, s2) -> Boolean.compare(s2.isPermanent(), s1.isPermanent())).findFirst().orElse(null);
			event.setCancelReason(SanctionUtils.getDisconnectScreen(ban));
			event.setCancelled(true);
		}
		sanctions.stream().filter(sanction -> sanction.getType() == OlympaSanctionType.MUTE).forEach(mute -> SanctionHandler.addMute(mute));
	}
	//
	//	@EventHandler(priority = EventPriority.HIGH)
	//	public void on2PostLogin(PostLoginEvent event) {
	//		OlympaBungee.getInstance().getTask().runTaskAsynchronously(() -> {
	//			ProxiedPlayer player = event.getPlayer();
	//			OlympaSanction mute = SanctionHandler.getMute(player.getUniqueId());
	//			if (mute == null) {
	//				mute = BanMySQL.getSanctionActive(player.getUniqueId(), OlympaSanctionType.MUTE);
	//				if (mute != null)
	//					SanctionHandler.addMute(mute);
	//			}
	//		});
	//	}

	//	@EventHandler
	//	public void onBungeeNewPlayerEvent(BungeeNewPlayerEvent event) {
	//
	//	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerChat(ChatEvent event) {
		if (event.isCancelled())
			return;
		if (!(event.getSender() instanceof ProxiedPlayer))
			return;
		String command = event.getMessage().substring(1);
		if (event.getMessage().startsWith("/") && !commandDisableWhenMuted.contains(command))
			return;
		ProxiedPlayer player = (ProxiedPlayer) event.getSender();
		OlympaSanction mute = SanctionHandler.isMutedThenEnd(player);
		Configuration config = OlympaBungee.getInstance().getConfig();
		if (mute != null) {
			event.setCancelled(true);
			if (mute.isPermanent())
				player.sendMessage(Prefix.DEFAULT_BAD.formatMessageB("Tu es mute pour &4%s&c.", mute.getReason()));
			else
				player.sendMessage(Prefix.DEFAULT_BAD.formatMessageB(config.getString("ban.youaremuted"), mute.getReason(), Utils.timestampToDuration(mute.getExpires())));
		}
	}
}
