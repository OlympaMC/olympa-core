package fr.olympa.core.bungee.ban.commands.methods;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import fr.olympa.api.common.chat.ColorUtils;
import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.common.sanction.OlympaSanctionType;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.ban.BanMySQL;
import fr.olympa.core.bungee.ban.objects.OlympaSanction;
import fr.olympa.core.bungee.ban.objects.OlympaSanctionStatus;
import fr.olympa.core.common.provider.AccountProvider;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.config.Configuration;

public class IpHistory {

	@SuppressWarnings("deprecation")
	public static void histBan(CommandSender sender, String ip) {
		List<OlympaSanction> bans = BanMySQL.getSanctions(ip);
		List<OlympaPlayer> players = null;
		try {
			players = AccountProvider.getter().getSQL().getPlayersByIp(ip);
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}

		String playersShow = players.stream().map(OlympaPlayer::getName).collect(Collectors.joining(", "));

		if (bans == null) {
			Configuration config = OlympaBungee.getInstance().getConfig();
			sender.sendMessage(config.getString("bungee.ban.messages.errordb"));
			return;
		}

		if (bans.size() == 0) {
			sender.sendMessage(ColorUtils.color(Prefix.DEFAULT_BAD + "&cL'ip de &4%ip%&c n'a jamais été sanctionné.").replaceAll("%ip%", playersShow));
			return;
		}

		TextComponent msg = new TextComponent(ColorUtils.color("&6Historique des sanctions de l'ip de &e" + playersShow + "&6:\n"));
		msg.addExtra(ColorUtils.color("&6Bans: &e" + bans.stream().filter(b -> b.getType() == OlympaSanctionType.BAN).count() + " "));
		msg.addExtra(ColorUtils.color("&6Mute: &e" + bans.stream().filter(b -> b.getType() == OlympaSanctionType.MUTE).count() + " "));
		msg.addExtra(ColorUtils.color("&6Kick: &e" + bans.stream().filter(b -> b.getType() == OlympaSanctionType.KICK).count() + "\n"));

		String sanctions = bans.stream().filter(b -> b.getStatus() == OlympaSanctionStatus.ACTIVE).map(b -> "&c" + b.getType().getName(!b.isPermanent())).collect(Collectors.joining("&7, "));
		msg.addExtra(ColorUtils.color("&6Sanction Active: " + (sanctions.isEmpty() ? "&aAucune" : sanctions) + "\n"));

		msg.addExtra(ColorUtils.color("&6Historique: "));

		bans.stream().forEach(b -> {
			BaseComponent[] comp = new ComponentBuilder(
					ColorUtils.color(b.getStatus().getColor() + b.getType().getName(!b.isPermanent()).toUpperCase() + " " + b.getStatus().getName().toUpperCase() + " " + b.getReason()))
							.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, b.toBaseComplement()))
							.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/banhist " + b.getId()))
							.create();

			for (BaseComponent s : comp)
				msg.addExtra(s);
		});
		sender.sendMessage(msg);
	}
}
