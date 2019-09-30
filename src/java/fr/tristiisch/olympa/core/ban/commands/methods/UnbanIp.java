package fr.tristiisch.olympa.core.ban.commands.methods;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import fr.tristiisch.emeraldmc.api.bungee.ban.BanMySQL;
import fr.tristiisch.emeraldmc.api.bungee.ban.objects.EmeraldBan;
import fr.tristiisch.emeraldmc.api.bungee.ban.objects.EmeraldBanHistory;
import fr.tristiisch.emeraldmc.api.bungee.ban.objects.EmeraldBanStatus;
import fr.tristiisch.emeraldmc.api.bungee.ban.objects.EmeraldBanType;
import fr.tristiisch.emeraldmc.api.bungee.utils.BungeeConfigUtils;
import fr.tristiisch.emeraldmc.api.bungee.utils.BungeeUtils;
import fr.tristiisch.emeraldmc.api.commons.datamanagment.sql.MySQL;
import fr.tristiisch.emeraldmc.api.commons.object.EmeraldPlayer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class UnbanIp {

	@SuppressWarnings("deprecation")
	public static void unBan(final UUID author, final CommandSender sender, final String ip, final String[] args) {
		final List<EmeraldPlayer> emeraldTargets = MySQL.getPlayersByIp(ip);
		final String emeraldTargetsName = emeraldTargets.stream().map(EmeraldPlayer::getName).collect(Collectors.joining(", "));

		// Si le joueur n'est pas banni
		if(!BanMySQL.isBanned(ip)) {
			sender.sendMessage(BungeeConfigUtils.getString("bungee.ban.messages.notbanned").replaceAll("%player%", emeraldTargetsName));
			return;
		}
		final String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

		final EmeraldBan ban = BanMySQL.getActiveSanction(ip, EmeraldBanType.BANIP);
		ban.setStatus(EmeraldBanStatus.CANCEL);
		if(!BanMySQL.changeCurrentSanction(new EmeraldBanHistory(author, EmeraldBanStatus.CANCEL, reason), ban.getId())) {
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
