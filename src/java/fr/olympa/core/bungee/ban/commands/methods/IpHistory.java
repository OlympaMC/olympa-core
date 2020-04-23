package fr.olympa.core.bungee.ban.commands.methods;

import java.util.List;
import java.util.stream.Collectors;

import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.sql.MySQL;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.ban.BanMySQL;
import fr.olympa.core.bungee.ban.objects.OlympaSanction;
import fr.olympa.core.bungee.ban.objects.OlympaSanctionStatus;
import fr.olympa.core.bungee.ban.objects.OlympaSanctionType;
import fr.olympa.core.bungee.utils.BungeeUtils;
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
		List<OlympaPlayer> players = MySQL.getPlayersByIp(ip);

		String playersShow = players.stream().map(OlympaPlayer::getName).collect(Collectors.joining(", "));

		if (bans == null) {
			Configuration config = OlympaBungee.getInstance().getConfig();
			sender.sendMessage(config.getString("bungee.ban.messages.errordb"));
			return;
		}

		if (bans.size() == 0) {
			sender.sendMessage(BungeeUtils.color(Prefix.DEFAULT_BAD + "&cL'ip de &4%ip%&c n'a jamais été sanctionné.").replaceAll("%ip%", playersShow));
			return;
		}

		TextComponent msg = new TextComponent(BungeeUtils.color("&6Historique des sanctions de l'ip de &e" + playersShow + "&6:\n"));
		msg.addExtra(BungeeUtils.color("&6Bans: &e" + bans.stream().filter(b -> b.getType() == OlympaSanctionType.BAN).count() + " "));
		msg.addExtra(BungeeUtils.color("&6Mute: &e" + bans.stream().filter(b -> b.getType() == OlympaSanctionType.MUTE).count() + " "));
		msg.addExtra(BungeeUtils.color("&6Kick: &e" + bans.stream().filter(b -> b.getType() == OlympaSanctionType.KICK).count() + "\n"));

		String sanctions = bans.stream().filter(b -> b.getStatus() == OlympaSanctionStatus.ACTIVE).map(b -> "&c" + b.getType().getName()).collect(Collectors.joining("&7, "));
		msg.addExtra(BungeeUtils.color("&6Sanction Active: " + (sanctions.isEmpty() ? "&aAucune" : sanctions) + "\n"));

		msg.addExtra(BungeeUtils.color("&6Historique: "));

		bans.stream().forEach(b -> {
			BaseComponent[] comp = new ComponentBuilder(
					BungeeUtils.color(b.getStatus().getColor() + b.getType().getName().toUpperCase() + " " + b.getStatus().getName().toUpperCase() + " " + b.getReason()))
							.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, b.toBaseComplement()))
							.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/banhist " + b.getId()))
							.create();

			for (BaseComponent s : comp) {
				msg.addExtra(s);
			}
		});
		sender.sendMessage(msg);
	}
}
