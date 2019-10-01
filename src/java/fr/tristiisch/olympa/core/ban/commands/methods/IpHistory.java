package fr.tristiisch.olympa.core.ban.commands.methods;

import java.util.List;
import java.util.stream.Collectors;

import fr.tristiisch.emeraldmc.api.bungee.ban.BanMySQL;
import fr.tristiisch.emeraldmc.api.bungee.ban.objects.EmeraldBan;
import fr.tristiisch.emeraldmc.api.bungee.ban.objects.EmeraldBanStatus;
import fr.tristiisch.emeraldmc.api.bungee.ban.objects.EmeraldBanType;
import fr.tristiisch.emeraldmc.api.bungee.utils.BungeeConfigUtils;
import fr.tristiisch.emeraldmc.api.commons.Prefix;
import fr.tristiisch.emeraldmc.api.commons.Utils;
import fr.tristiisch.emeraldmc.api.commons.datamanagment.sql.MySQL;
import fr.tristiisch.emeraldmc.api.commons.object.EmeraldPlayer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class IpHistory {

	@SuppressWarnings("deprecation")
	public static void histBan(final CommandSender sender, final String ip) {
		final List<OlympaSanction> bans = BanMySQL.getSanctions(ip);
		final List<EmeraldPlayer> players = MySQL.getPlayersByIp(ip);

		final String playersShow = players.stream().map(EmeraldPlayer::getName).collect(Collectors.joining(", "));

		if(bans == null) {
			sender.sendMessage(BungeeConfigUtils.getString("bungee.ban.messages.errordb"));
			return;
		}

		if(bans.size() == 0) {
			sender.sendMessage(Utils.color(Prefix.DEFAULT_BAD + "&cL'ip de &4%ip%&c n'a jamais été sanctionné.").replaceAll("%ip%", playersShow));
			return;
		}

		final TextComponent msg = new TextComponent(Utils.color("&6Historique des sanctions de l'ip de &e" + playersShow + "&6:\n"));
		msg.addExtra(Utils.color("&6Bans: &e" + bans.stream().filter(b -> b.getType() == OlympaSanctionType.BAN).count() + " "));
		msg.addExtra(Utils.color("&6Mute: &e" + bans.stream().filter(b -> b.getType() == OlympaSanctionType.MUTE).count() + " "));
		msg.addExtra(Utils.color("&6Kick: &e" + bans.stream().filter(b -> b.getType() == OlympaSanctionType.KICK).count() + "\n"));

		final String sanctions = bans.stream().filter(b -> b.getStatus() == OlympaSanctionStatus.ACTIVE).map(b -> "&c" + b.getType().getName()).collect(Collectors.joining("&7, "));
		msg.addExtra(Utils.color("&6Sanction Active: " + (sanctions.isEmpty() ? "&aAucune" : sanctions) + "\n"));

		msg.addExtra(Utils.color("&6Historique: "));

		bans.stream().forEach(b -> {
			final BaseComponent[] comp = new ComponentBuilder(
				Utils.colorFix(b.getStatus().getColor() + b.getType().getName().toUpperCase() + " " + b.getStatus().getName().toUpperCase() + " " + b.getReason()))
						.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, b.toBaseComplement()))
						.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/banhist " + b.getId()))
						.create();

			for(final BaseComponent s : comp) {
				msg.addExtra(s);
			}
		});
		sender.sendMessage(msg);
	}
}
