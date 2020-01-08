package fr.olympa.core.bungee.ban.commands.methods;

import java.util.List;
import java.util.stream.Collectors;

import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.sql.MySQL;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.SpigotUtils;
import fr.olympa.core.bungee.ban.BanMySQL;
import fr.olympa.core.bungee.ban.objects.OlympaSanction;
import fr.olympa.core.bungee.ban.objects.OlympaSanctionStatus;
import fr.olympa.core.bungee.ban.objects.OlympaSanctionType;
import fr.olympa.core.bungee.utils.BungeeConfigUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class IpHistory {

	@SuppressWarnings("deprecation")
	public static void histBan(CommandSender sender, String ip) {
		List<OlympaSanction> bans = BanMySQL.getSanctions(ip);
		List<OlympaPlayer> players = MySQL.getPlayersByIp(ip);

		String playersShow = players.stream().map(OlympaPlayer::getName).collect(Collectors.joining(", "));

		if (bans == null) {
			sender.sendMessage(BungeeConfigUtils.getString("bungee.ban.messages.errordb"));
			return;
		}

		if (bans.size() == 0) {
			sender.sendMessage(SpigotUtils.color(Prefix.DEFAULT_BAD + "&cL'ip de &4%ip%&c n'a jamais été sanctionné.").replaceAll("%ip%", playersShow));
			return;
		}

		TextComponent msg = new TextComponent(SpigotUtils.color("&6Historique des sanctions de l'ip de &e" + playersShow + "&6:\n"));
		msg.addExtra(SpigotUtils.color("&6Bans: &e" + bans.stream().filter(b -> b.getType() == OlympaSanctionType.BAN).count() + " "));
		msg.addExtra(SpigotUtils.color("&6Mute: &e" + bans.stream().filter(b -> b.getType() == OlympaSanctionType.MUTE).count() + " "));
		msg.addExtra(SpigotUtils.color("&6Kick: &e" + bans.stream().filter(b -> b.getType() == OlympaSanctionType.KICK).count() + "\n"));

		String sanctions = bans.stream().filter(b -> b.getStatus() == OlympaSanctionStatus.ACTIVE).map(b -> "&c" + b.getType().getName()).collect(Collectors.joining("&7, "));
		msg.addExtra(SpigotUtils.color("&6Sanction Active: " + (sanctions.isEmpty() ? "&aAucune" : sanctions) + "\n"));

		msg.addExtra(SpigotUtils.color("&6Historique: "));

		bans.stream().forEach(b -> {
			BaseComponent[] comp = new ComponentBuilder(
					SpigotUtils.color(b.getStatus().getColor() + b.getType().getName().toUpperCase() + " " + b.getStatus().getName().toUpperCase() + " " + b.getReason()))
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
