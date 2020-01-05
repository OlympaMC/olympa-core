package fr.olympa.core.ban.commands.methods;

import java.util.Arrays;
import java.util.UUID;

import org.bukkit.command.CommandSender;

import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.sql.MySQL;
import fr.olympa.spigot.core.ban.BanMySQL;
import fr.olympa.spigot.core.ban.objects.OlympaSanction;
import fr.olympa.spigot.core.ban.objects.OlympaSanctionHistory;
import fr.olympa.spigot.core.ban.objects.OlympaSanctionStatus;
import fr.olympa.spigot.core.ban.objects.OlympaSanctionType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class UnbanPlayer {

	/**
	 * Ajoute un ban
	 * targetUUID ou targetname ne doit pas être null.
	 *
	 * @param author is a UUID of author of ban or String (If the author is Console, author = "Console")
	 * @param targetname Name of player to ban. case insensitive
	 */
	@SuppressWarnings("deprecation")
	public static void unBan(UUID author, CommandSender sender, UUID targetUUID, String targetname, String[] args) {

		ProxiedPlayer target = null;
		EmeraldPlayer emeraldTarget = null;
		if (targetUUID != null) {
			target = ProxyServer.getInstance().getPlayer(targetUUID);

		} else if (targetname != null) {
			target = ProxyServer.getInstance().getPlayer(targetname);

		} else {
			throw new NullPointerException("The uuid or name must be specified");
		}

		if (target != null) {
			emeraldTarget = new AccountProvider(target.getUniqueId()).getEmeraldPlayer();
		} else {
			emeraldTarget = MySQL.getPlayer(targetUUID);
			if (emeraldTarget == null) {
				sender.sendMessage(BungeeConfigUtils.getString("bungee.ban.messages.playerneverjoin").replaceAll("%player%", args[0]));
				return;
			}
		}

		// Si le joueur n'est pas banni
		if (!BanMySQL.isBanned(emeraldTarget.getUniqueId())) {
			sender.sendMessage(BungeeConfigUtils.getString("bungee.ban.messages.notbanned").replaceAll("%player%", targetname));
			return;
		}
		String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

		OlympaSanction ban = BanMySQL.getSanctionActive(emeraldTarget.getUniqueId(), OlympaSanctionType.BAN);
		ban.setStatus(OlympaSanctionStatus.CANCEL);
		if (!BanMySQL.changeCurrentSanction(new OlympaSanctionHistory(author, OlympaSanctionStatus.CANCEL, reason), ban.getId())) {
			sender.sendMessage(BungeeConfigUtils.getString("bungee.ban.messages.errordb"));
			return;
		}
		// Envoye un message à l'auteur
		TextComponent msg = BungeeUtils.formatStringToJSON(BungeeConfigUtils.getString("bungee.ban.messages.unbanannouncetoauthor")
				.replaceAll("%player%", targetname)
				.replaceAll("%reason%", reason)
				.replaceAll("%author%", BungeeUtils.getName(author)));
		msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, ban.toBaseComplement()));
		msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/banhist " + ban.getId()));
		BungeeUtils.sendMessageToStaff(msg);
		ProxyServer.getInstance().getConsole().sendMessage(msg);
	}
}
