package fr.tristiisch.olympa.core.ban.commands.methods;

import java.util.Arrays;
import java.util.UUID;

import fr.tristiisch.emeraldmc.api.bungee.ban.BanMySQL;
import fr.tristiisch.emeraldmc.api.bungee.ban.MuteUtils;
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

public class UnmutePlayer {

	@SuppressWarnings("deprecation")
	public static void unBan(final UUID author, final CommandSender sender, final UUID targetUUID, final String targetname, final String[] args) {
		// /ban <pseudo> <time unit> <reason>
		// args[0] = target
		// args[1] = time + unit
		// args[2] & + = reason

		ProxiedPlayer target = null;
		EmeraldPlayer emeraldTarget = null;
		// Si l'uuid est défini & le nom du joueur n'est pas défini, le récupéré via son uuid
		if(targetUUID != null) {
			target = ProxyServer.getInstance().getPlayer(targetUUID);
			// Si Target est connecté, prendre son nom exacte
			if(target != null) {
				emeraldTarget = new AccountProvider(target.getUniqueId()).getEmeraldPlayer();
				// Sinon prendre son nom exacte dans la base de donnés
			} else {
				emeraldTarget = MySQL.getPlayer(targetUUID);
				// Si le joueur n'est pas dans la base de donnés, annuler le ban.
				if(emeraldTarget == null) {
					sender.sendMessage(BungeeConfigUtils.getString("commun.messages.playerneverjoin").replaceAll("%player%", args[0]));
					return;
				}
			}
			// Si le nom du joueur est défini & l'uuid n'est pas défini, le récupéré via son nom
		} else if(targetname != null) {
			target = ProxyServer.getInstance().getPlayer(targetname);
			// Si Target est connecté, prendre son nom exacte et son uuid
			if(target != null) {
				emeraldTarget = new AccountProvider(target.getUniqueId()).getEmeraldPlayer();
				// Sinon, récupérer son nom exacte et son uuid dans la base de donnés
			} else {
				emeraldTarget = MySQL.getPlayer(targetname);
				// Si le joueur n'est pas dans la base de donnés, annuler le ban.
				if(emeraldTarget == null) {
					sender.sendMessage(BungeeConfigUtils.getString("commun.messages.playerneverjoin").replaceAll("%player%", args[0]));
					return;
				}
			}
			// Si l'uuid & le nom ne sont pas défini, annuler le ban
		} else {
			throw new NullPointerException("The uuid or name must be specified");
		}
		OlympaSanction mute;
		if(target != null) {
			mute = MuteUtils.getMute(target.getUniqueId());
			// Si le joueur n'est pas mute
			if(mute == null) {
				sender.sendMessage(BungeeConfigUtils.getString("bungee.ban.messages.notmuted").replaceAll("%player%", targetname));
				return;
			}
		} else {
			mute = BanMySQL.getSanctionActive(emeraldTarget.getUniqueId(), OlympaSanctionType.MUTE);
			// Si le joueur n'est pas mute
			if(mute == null) {
				sender.sendMessage(BungeeConfigUtils.getString("bungee.ban.messages.notmuted").replaceAll("%player%", targetname));
				return;
			}
		}

		final String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

		MuteUtils.getMute(emeraldTarget.getUniqueId());
		mute.setStatus(OlympaSanctionStatus.CANCEL);
		if(!BanMySQL.changeCurrentSanction(new OlympaSanctionHistory(author, OlympaSanctionStatus.CANCEL), mute.getId())) {
			sender.sendMessage(BungeeConfigUtils.getString("bungee.ban.messages.errordb"));
			return;
		}

		MuteUtils.removeMute(emeraldTarget.getUniqueId());

		// Envoye un message au staff
		final TextComponent msg = BungeeUtils.formatStringToJSON(BungeeConfigUtils.getString("bungee.ban.messages.unmuteannouncetoauthor")
				.replaceAll("%player%", emeraldTarget.getName())
				.replaceAll("%reason%", reason)
				.replaceAll("%author%", BungeeUtils.getName(author)));
		msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, mute.toBaseComplement()));
		msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/banhist " + mute.getId()));
		BungeeUtils.sendMessageToStaff(msg);
		ProxyServer.getInstance().getConsole().sendMessage(msg);
	}
}
