package fr.olympa.core.bungee.ban.commands.methods;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.UUID;

import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.ban.objects.OlympaSanction;
import fr.olympa.core.bungee.ban.objects.OlympaSanctionStatus;
import fr.olympa.core.bungee.ban.objects.OlympaSanctionType;
import fr.olympa.core.bungee.ban.objects.SanctionExecuteTarget;
import fr.olympa.core.bungee.utils.BungeeUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;

public class KickPlayer {

	@SuppressWarnings("deprecation")
	public static void addKick(long authorId, CommandSender sender, String targetname, UUID targetUUID, String[] args, OlympaPlayer olympaPlayer) {
		ProxiedPlayer target = null;
		OlympaPlayer olympaTarget = null;

		if (targetUUID != null)
			target = ProxyServer.getInstance().getPlayer(targetUUID);
		else if (targetname != null)
			target = ProxyServer.getInstance().getPlayer(targetname);
		else
			throw new NullPointerException("The uuid or name must be specified");

		Configuration config = OlympaBungee.getInstance().getConfig();
		if (target != null)
			olympaTarget = AccountProvider.get(target.getUniqueId());
		else {
			sender.sendMessage(config.getString("ban.kicknotconnected").replaceAll("%player%", args[0]));
			return;
		}

		String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

		if (olympaPlayer != null && OlympaCorePermissions.BAN_BYPASS_SANCTION_STAFF.hasPermission(olympaPlayer)) {
			sender.sendMessage(config.getString("ban.cantkicktaffmembers"));
			return;
		}

		OlympaSanction kick;
		try {
			kick = SanctionExecuteTarget.add(OlympaSanctionType.KICK, authorId, olympaTarget.getUniqueId(), reason, 0, OlympaSanctionStatus.EXPIRE);
		} catch (SQLException e) {
			e.printStackTrace();
			sender.sendMessage(config.getString("ban.errordb"));
			return;
		}
		// Envoyer un message à tous les joueurs du même serveur spigot
		/*
		 * for(ProxiedPlayer players : target.getServer().getInfo().getPlayers()) {
		 * players.sendMessage(BungeeConfigUtils.getString(
		 * "bungee.ban.messages.kickannounce") .replaceAll("%player%",
		 * olympaTarget.getName()) .replaceAll("%reason%", reason) ); };
		 */

		target.disconnect(
				BungeeUtils.connectScreen(config.getString("bungee.ban.messages.kickdisconnect").replaceAll("%reason%", kick.getReason()).replace("%id%", String.valueOf(kick.getId()))));

		TextComponent msg = BungeeUtils.formatStringToJSON(config.getString("ban.kickannouncetoauthor")
				.replace("%player%", olympaTarget.getName())
				.replace("%reason%", reason)
				.replace("%author%", BungeeUtils.getName(authorId)));
		msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, kick.toBaseComplement()));
		msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/banhist " + kick.getId()));
		OlympaCorePermissions.BAN_SEEBANMSG.sendMessage(msg);

		ProxyServer.getInstance().getConsole().sendMessage(msg);
	}
}
