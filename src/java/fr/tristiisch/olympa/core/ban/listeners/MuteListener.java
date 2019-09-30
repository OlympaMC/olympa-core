package fr.tristiisch.olympa.core.ban.listeners;

import java.util.ArrayList;
import java.util.List;

import fr.tristiisch.emeraldmc.api.bungee.privatemessage.PrivateMessage;
import fr.tristiisch.emeraldmc.api.bungee.utils.BungeeConfigUtils;
import fr.tristiisch.emeraldmc.api.commons.Utils;
import fr.tristiisch.olympa.core.ban.BanMySQL;
import fr.tristiisch.olympa.core.ban.MuteUtils;
import fr.tristiisch.olympa.core.ban.objects.EmeraldBan;
import fr.tristiisch.olympa.core.ban.objects.EmeraldBanType;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class MuteListener implements Listener {

	private final List<String> commandDisableWhenMuted = new ArrayList<>();

	public MuteListener() {
		this.commandDisableWhenMuted.addAll(PrivateMessage.privateMessageCommand);
		this.commandDisableWhenMuted.addAll(PrivateMessage.replyCommand);
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onChat(final ChatEvent event) {
		final String command = event.getMessage().substring(1);
		if(event.isCancelled() || event.getMessage().startsWith("/") && !this.commandDisableWhenMuted.contains(command)) {
			return;
		}
		final ProxiedPlayer player = (ProxiedPlayer) event.getSender();

		final EmeraldBan mute = MuteUtils.getMute(player.getUniqueId());
		if(mute != null) {
			if(!MuteUtils.chechExpireBan(mute)) {
				player.sendMessage(
						BungeeConfigUtils.getString("bungee.ban.messages.youaremuted").replaceAll("%reason%", mute.getReason()).replaceAll("%expire%", Utils.timestampToDuration(mute.getExpires())));
				event.setCancelled(true);
				return;
			}
		}
	}

	@EventHandler
	public void PostLoginEvent(final PostLoginEvent event) {
		final ProxiedPlayer player = event.getPlayer();
		EmeraldBan mute = MuteUtils.getMute(player.getUniqueId());
		if(mute == null) {
			mute = BanMySQL.getActiveSanction(player.getUniqueId(), EmeraldBanType.MUTE);
			if(mute != null) {
				MuteUtils.addMute(mute);
			}
		}
	}
}
