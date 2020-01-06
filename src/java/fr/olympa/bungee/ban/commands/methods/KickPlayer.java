package fr.olympa.bungee.ban.commands.methods;

import java.util.Arrays;
import java.util.UUID;

import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.Utils;
import fr.olympa.bungee.ban.BanMySQL;
import fr.olympa.bungee.ban.objects.OlympaSanction;
import fr.olympa.bungee.ban.objects.OlympaSanctionStatus;
import fr.olympa.bungee.ban.objects.OlympaSanctionType;
import fr.olympa.bungee.utils.BungeeConfigUtils;
import fr.olympa.bungee.utils.BungeeUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class KickPlayer {

	@SuppressWarnings("deprecation")
	public static void addKick(UUID author, CommandSender sender, String targetname, UUID targetUUID, String[] args, OlympaPlayer olympaPlayer) {
		ProxiedPlayer target = null;
		OlympaPlayer olympaTarget = null;

		if (targetUUID != null) {
			target = ProxyServer.getInstance().getPlayer(targetUUID);

		} else if (targetname != null) {
			target = ProxyServer.getInstance().getPlayer(targetname);

		} else {
			throw new NullPointerException("The uuid or name must be specified");
		}

		if (target != null) {
			olympaTarget = AccountProvider.get(target.getUniqueId());

		} else {
			sender.sendMessage(BungeeConfigUtils.getString("bungee.ban.messages.kicknotconnected").replaceAll("%player%", args[0]));
			return;
		}

		String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

		if (olympaPlayer != null && OlympaCorePermissions.BAN_BYPASS_SANCTION_STAFF.hasPermission(olympaPlayer)) {
			sender.sendMessage(BungeeConfigUtils.getString("bungee.ban.messages.cantkicktaffmembers"));
			return;
		}

		OlympaSanction kick = new OlympaSanction(OlympaSanction.getNextId(), OlympaSanctionType.KICK, olympaTarget.getUniqueId(), author, reason, Utils.getCurrentTimeinSeconds(), 0, OlympaSanctionStatus.EXPIRE);
		if (!BanMySQL.addSanction(kick)) {
			sender.sendMessage(BungeeConfigUtils.getString("bungee.ban.messages.errordb"));
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
				BungeeUtils.connectScreen(BungeeConfigUtils.getString("bungee.ban.messages.kickdisconnect").replaceAll("%reason%", kick.getReason()).replaceAll("%id%", String.valueOf(kick.getId()))));

		TextComponent msg = BungeeUtils.formatStringToJSON(BungeeConfigUtils.getString("bungee.ban.messages.kickannouncetoauthor")
				.replaceAll("%player%", olympaTarget.getName())
				.replaceAll("%reason%", reason)
				.replaceAll("%author%", BungeeUtils.getName(author)));
		msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, kick.toBaseComplement()));
		msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/banhist " + kick.getId()));
		OlympaCorePermissions.BAN_SEEBANMSG.sendMessage(msg);

		ProxyServer.getInstance().getConsole().sendMessage(msg);
	}
}
