package fr.olympa.core.bungee.ban.commands.methods;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.UUID;

import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.sql.MySQL;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.ban.BanMySQL;
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

public class UnbanPlayer {

	/**
	 * Ajoute un ban targetUUID ou targetname ne doit pas être null.
	 *
	 * @param author     is a UUID of author of ban or String (If the author is
	 *                   Console, author = "Console")
	 * @param targetname Name of player to ban. case insensitive
	 */
	public static void unBan(UUID author, CommandSender sender, UUID targetUUID, String targetname, String[] args) {

		ProxiedPlayer target = null;
		OlympaPlayer emeraldTarget = null;
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
				emeraldTarget = new AccountProvider(target.getUniqueId()).get();
			} else {
				emeraldTarget = MySQL.getPlayer(targetUUID);
				if (emeraldTarget == null) {
					sender.sendMessage(config.getString("ban.messages.playerneverjoin").replace("%player%", args[0]));
					return;
				}
			}
		} catch (SQLException e) {
			sender.sendMessage(config.getString("ban.messages.errordb"));
			e.printStackTrace();
			return;
		}

		// Si le joueur n'est pas banni
		if (!BanMySQL.isBanned(emeraldTarget.getUniqueId())) {
			sender.sendMessage(config.getString("ban.messages.notbanned").replace("%player%", targetname));
			return;
		}
		String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

		OlympaSanction ban = BanMySQL.getSanctionActive(emeraldTarget.getUniqueId(), OlympaSanctionType.BAN);
		ban.setStatus(OlympaSanctionStatus.CANCEL);
		if (!BanMySQL.changeCurrentSanction(new OlympaSanctionHistory(author, OlympaSanctionStatus.CANCEL, reason), ban.getId())) {
			sender.sendMessage(config.getString("ban.messages.errordb"));
			return;
		}
		// Envoye un message à l'auteur
		TextComponent msg = BungeeUtils.formatStringToJSON(config.getString("ban.messages.unbanannouncetoauthor")
				.replace("%player%", targetname)
				.replace("%reason%", reason)
				.replace("%author%", BungeeUtils.getName(author)));
		msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, ban.toBaseComplement()));
		msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/banhist " + ban.getId()));
		OlympaCorePermissions.BAN_SEEBANMSG.sendMessage(msg);
		ProxyServer.getInstance().getConsole().sendMessage(msg);
	}
}
