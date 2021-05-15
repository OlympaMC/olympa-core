package fr.olympa.core.bungee.ban.commands.methods;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import fr.olympa.api.config.CustomConfig;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.spigot.SpigotUtils;
import fr.olympa.core.bungee.ban.BanMySQL;
import fr.olympa.core.bungee.ban.objects.OlympaSanction;
import fr.olympa.core.bungee.ban.objects.OlympaSanctionStatus;
import fr.olympa.core.bungee.ban.objects.OlympaSanctionType;
import fr.olympa.core.spigot.OlympaCore;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

@Deprecated(forRemoval = true)
public class UnbanIp {

	public static void unBan(UUID author, CommandSender sender, String ip, String[] args) {
		List<OlympaPlayer> olympaTargets;
		try {
			olympaTargets = AccountProvider.getSQL().getPlayersByIp(ip);
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
		String olympaTargetsName = olympaTargets.stream().map(OlympaPlayer::getName).collect(Collectors.joining(", "));

		CustomConfig config = OlympaCore.getInstance().getConfig();
		// Si le joueur n'est pas banni
		if (!BanMySQL.isBanned(ip)) {
			sender.sendMessage(config.getString("ban.notbanned").replaceAll("%player%", olympaTargetsName));
			return;
		}
		String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

		OlympaSanction ban = BanMySQL.getSanctionActive(ip, OlympaSanctionType.BANIP);
		ban.setStatus(OlympaSanctionStatus.CANCEL);
		//		if (!BanMySQL.changeCurrentSanction(new OlympaSanctionHistory(author, OlympaSanctionStatus.CANCEL, reason), ban.getId())) {
		//			sender.sendMessage(config.getString("ban.errordb"));
		//			return;
		//		}

		TextComponent msg = new TextComponent(config.getString("ban.unbanannouncetostaff")
				.replaceAll("%player%", olympaTargetsName)
				.replaceAll("%reason%", reason)
				.replaceAll("%author%", SpigotUtils.getName(author)));
		msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, ban.toBaseComplement()));
		msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/banhist " + ban.getId()));

		OlympaCorePermissions.BAN_SEEBANMSG.sendMessage(msg);
		ProxyServer.getInstance().getConsole().sendMessage(msg);
	}

}
