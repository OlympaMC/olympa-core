package fr.olympa.core.bungee.ban.commands.methods;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.UUID;

import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.ban.BanMySQL;
import fr.olympa.core.bungee.ban.MuteUtils;
import fr.olympa.core.bungee.ban.objects.OlympaSanction;
import fr.olympa.core.bungee.ban.objects.OlympaSanctionHistory;
import fr.olympa.core.bungee.ban.objects.OlympaSanctionStatus;
import fr.olympa.core.bungee.ban.objects.OlympaSanctionType;
import fr.olympa.core.bungee.utils.BungeeUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;

public class UnmutePlayer {

	@SuppressWarnings("deprecation")
	public static void unBan(UUID author, CommandSender sender, UUID targetUUID, String targetname, String[] args) {
		// /ban <pseudo> <time unit> <reason>
		// args[0] = target
		// args[1] = time + unit
		// args[2] & + = reason

		ProxiedPlayer target = null;
		OlympaPlayer olympaTarget = null;
		if (targetUUID != null) {
			target = ProxyServer.getInstance().getPlayer(targetUUID);

		} else if (targetname != null) {
			target = ProxyServer.getInstance().getPlayer(targetname);

		} else {
			throw new NullPointerException("The uuid or name must be specified");
		}

		Configuration config = OlympaBungee.getInstance().getConfig();
		try {
			if (target != null) {
				olympaTarget = AccountProvider.get(target.getUniqueId());
			} else if (targetUUID != null) {
				olympaTarget = AccountProvider.getFromDatabase(targetUUID);
			} else if (targetname != null) {
				olympaTarget = AccountProvider.getFromDatabase(targetname);
			}
		} catch (SQLException e) {
			sender.sendMessage(config.getString("ban.messages.errordb"));
			e.printStackTrace();
			return;
		}
		OlympaSanction mute;
		if (target != null) {
			mute = MuteUtils.getMute(target.getUniqueId());
			// Si le joueur n'est pas mute
			if (mute == null) {
				sender.sendMessage(config.getString("bungee.ban.messages.notmuted").replaceAll("%player%", targetname));
				return;
			}
		} else {
			mute = BanMySQL.getSanctionActive(olympaTarget.getUniqueId(), OlympaSanctionType.MUTE);
			// Si le joueur n'est pas mute
			if (mute == null) {
				sender.sendMessage(config.getString("bungee.ban.messages.notmuted").replaceAll("%player%", targetname));
				return;
			}
		}

		String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

		MuteUtils.getMute(olympaTarget.getUniqueId());
		mute.setStatus(OlympaSanctionStatus.CANCEL);
		if (!BanMySQL.changeCurrentSanction(new OlympaSanctionHistory(author, OlympaSanctionStatus.CANCEL), mute.getId())) {
			sender.sendMessage(config.getString("bungee.ban.messages.errordb"));
			return;
		}

		MuteUtils.removeMute(olympaTarget.getUniqueId());

		// Envoye un message au staff
		TextComponent msg = BungeeUtils.formatStringToJSON(config.getString("bungee.ban.messages.unmuteannouncetoauthor")
				.replaceAll("%player%", olympaTarget.getName())
				.replaceAll("%reason%", reason)
				.replaceAll("%author%", BungeeUtils.getName(author)));
		msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, mute.toBaseComplement()));
		msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/banhist " + mute.getId()));
		OlympaCorePermissions.BAN_SEEBANMSG.sendMessage(msg);
		ProxyServer.getInstance().getConsole().sendMessage(msg);
	}
}
