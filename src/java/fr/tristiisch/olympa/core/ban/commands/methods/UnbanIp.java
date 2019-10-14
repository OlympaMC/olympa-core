package fr.tristiisch.olympa.core.ban.commands.methods;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;

import fr.tristiisch.olympa.api.objects.OlympaPlayer;
import fr.tristiisch.olympa.core.ban.BanMySQL;
import fr.tristiisch.olympa.core.ban.objects.OlympaSanction;
import fr.tristiisch.olympa.core.ban.objects.OlympaSanctionHistory;
import fr.tristiisch.olympa.core.ban.objects.OlympaSanctionStatus;
import fr.tristiisch.olympa.core.ban.objects.OlympaSanctionType;
import fr.tristiisch.olympa.core.datamanagment.sql.MySQL;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class UnbanIp {

	@SuppressWarnings("deprecation")
	public static void unBan(final UUID author, final CommandSender sender, final String ip, final String[] args) {
		final List<OlympaPlayer> emeraldTargets = MySQL.getPlayersByIp(ip);
		final String emeraldTargetsName = emeraldTargets.stream().map(OlympaPlayer::getName).collect(Collectors.joining(", "));

		// Si le joueur n'est pas banni
		if (!BanMySQL.isBanned(ip)) {
			sender.sendMessage(BungeeConfigUtils.getString("bungee.ban.messages.notbanned").replaceAll("%player%", emeraldTargetsName));
			return;
		}
		final String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

		final OlympaSanction ban = BanMySQL.getSanctionActive(ip, OlympaSanctionType.BANIP);
		ban.setStatus(OlympaSanctionStatus.CANCEL);
		if (!BanMySQL.changeCurrentSanction(new OlympaSanctionHistory(author, OlympaSanctionStatus.CANCEL, reason), ban.getId())) {
			sender.sendMessage(BungeeConfigUtils.getString("bungee.ban.messages.errordb"));
			return;
		}

		final TextComponent msg = BungeeUtils.formatStringToJSON(BungeeConfigUtils.getString("bungee.ban.messages.unbanannouncetoauthor")
				.replaceAll("%player%", emeraldTargetsName)
				.replaceAll("%reason%", reason)
				.replaceAll("%author%", BungeeUtils.getName(author)));
		msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, ban.toBaseComplement()));
		msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/banhist " + ban.getId()));
		BungeeUtils.sendMessageToStaff(msg);
		ProxyServer.getInstance().getConsole().sendMessage(msg);
	}

}
