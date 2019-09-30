package fr.tristiisch.olympa.core.ban.commands.methods;

import java.util.Arrays;
import java.util.UUID;

import fr.tristiisch.emeraldmc.api.bungee.ban.BanMySQL;
import fr.tristiisch.emeraldmc.api.bungee.ban.objects.EmeraldBan;
import fr.tristiisch.emeraldmc.api.bungee.ban.objects.EmeraldBanHistory;
import fr.tristiisch.emeraldmc.api.bungee.ban.objects.EmeraldBanStatus;
import fr.tristiisch.emeraldmc.api.bungee.ban.objects.EmeraldBanType;
import fr.tristiisch.emeraldmc.api.bungee.utils.BungeeConfigUtils;
import fr.tristiisch.emeraldmc.api.bungee.utils.BungeeUtils;
import fr.tristiisch.emeraldmc.api.commons.datamanagment.redis.AccountProvider;
import fr.tristiisch.emeraldmc.api.commons.datamanagment.sql.MySQL;
import fr.tristiisch.emeraldmc.api.commons.object.EmeraldPlayer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class UnbanPlayer {

	/**
	 * Ajoute un ban
	 * targetUUID ou targetname ne doit pas être null.
	 *
	 * @param author is a UUID of author of ban or String (If the author is Console, author = "Console")
	 * @param targetname Name of player to ban. case insensitive
	 */
	@SuppressWarnings("deprecation")
	public static void unBan(final UUID author, final CommandSender sender, final UUID targetUUID, final String targetname, final String[] args) {

		ProxiedPlayer target = null;
		EmeraldPlayer emeraldTarget = null;
		if(targetUUID != null) {
			target = ProxyServer.getInstance().getPlayer(targetUUID);

		} else if(targetname != null) {
			target = ProxyServer.getInstance().getPlayer(targetname);

		} else {
			throw new NullPointerException("The uuid or name must be specified");
		}

		if(target != null) {
			emeraldTarget = new AccountProvider(target.getUniqueId()).getEmeraldPlayer();
		} else {
			emeraldTarget = MySQL.getPlayer(targetUUID);
			if(emeraldTarget == null) {
				sender.sendMessage(BungeeConfigUtils.getString("bungee.ban.messages.playerneverjoin").replaceAll("%player%", args[0]));
				return;
			}
		}

		// Si le joueur n'est pas banni
		if(!BanMySQL.isBanned(emeraldTarget.getUniqueId())) {
			sender.sendMessage(BungeeConfigUtils.getString("bungee.ban.messages.notbanned").replaceAll("%player%", targetname));
			return;
		}
		final String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

		final EmeraldBan ban = BanMySQL.getActiveSanction(emeraldTarget.getUniqueId(), EmeraldBanType.BAN);
		ban.setStatus(EmeraldBanStatus.CANCEL);
		if(!BanMySQL.changeCurrentSanction(new EmeraldBanHistory(author, EmeraldBanStatus.CANCEL, reason), ban.getId())) {
			sender.sendMessage(BungeeConfigUtils.getString("bungee.ban.messages.errordb"));
			return;
		}
		// Envoye un message à l'auteur
		final TextComponent msg = BungeeUtils.formatStringToJSON(BungeeConfigUtils.getString("bungee.ban.messages.unbanannouncetoauthor")
				.replaceAll("%player%", targetname)
				.replaceAll("%reason%", reason)
				.replaceAll("%author%", BungeeUtils.getName(author)));
		msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, ban.toBaseComplement()));
		msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/banhist " + ban.getId()));
		BungeeUtils.sendMessageToStaff(msg);
		ProxyServer.getInstance().getConsole().sendMessage(msg);
	}
}
