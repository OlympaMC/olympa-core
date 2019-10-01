package fr.tristiisch.olympa.core.ban.commands.methods;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import fr.tristiisch.emeraldmc.api.bungee.ban.BanMySQL;
import fr.tristiisch.emeraldmc.api.bungee.ban.objects.EmeraldBan;
import fr.tristiisch.emeraldmc.api.bungee.ban.objects.EmeraldBanStatus;
import fr.tristiisch.emeraldmc.api.bungee.ban.objects.EmeraldBanType;
import fr.tristiisch.emeraldmc.api.bungee.utils.BungeeConfigUtils;
import fr.tristiisch.emeraldmc.api.commons.Prefix;
import fr.tristiisch.emeraldmc.api.commons.Utils;
import fr.tristiisch.emeraldmc.api.commons.datamanagment.redis.AccountProvider;
import fr.tristiisch.emeraldmc.api.commons.datamanagment.sql.MySQL;
import fr.tristiisch.emeraldmc.api.commons.object.EmeraldPlayer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class PlayerHistory {

	@SuppressWarnings("deprecation")
	public static void histBan(final CommandSender sender, final String name, final UUID uuid) {
		ProxiedPlayer target = null;
		EmeraldPlayer emeraldTarget;
		if(uuid != null) {
			target = ProxyServer.getInstance().getPlayer(uuid);

		} else if(name != null) {
			target = ProxyServer.getInstance().getPlayer(name);

		} else {
			throw new NullPointerException("The uuid or name must be specified");
		}

		if(target != null) {
			emeraldTarget = new AccountProvider(target.getUniqueId()).getEmeraldPlayer();

		} else {
			if(uuid != null) {
				emeraldTarget = MySQL.getPlayer(uuid);

			} else if(name != null) {
				emeraldTarget = MySQL.getPlayer(name);
			} else {
				throw new NullPointerException("The uuid or name must be specified");
			}

			if(emeraldTarget == null) {
				sender.sendMessage(BungeeConfigUtils.getString("bungee.ban.messages.playerneverjoin").replaceAll("%player%", name));
				return;
			}
		}

		final List<OlympaSanction> bans = BanMySQL.getSanctions(emeraldTarget.getUniqueId());

		if(bans == null) {
			sender.sendMessage(BungeeConfigUtils.getString("bungee.ban.messages.errordb"));
			return;
		}

		if(bans.size() == 0) {
			sender.sendMessage(Utils.color(Prefix.DEFAULT_BAD + "&4%player%&c n'a jamais été sanctionné.").replaceAll("%player%", emeraldTarget.getName()));
			return;
		}

		final TextComponent msg = new TextComponent(Utils.color("&6Historique des sanctions de " + emeraldTarget.getGroup().getPrefix() + emeraldTarget.getName() + "&6:\n"));
		msg.addExtra(Utils.color("&6Bans: &e" + bans.stream().filter(b -> b.getType() == OlympaSanctionType.BAN).count() + " "));
		msg.addExtra(Utils.color("&6Mute: &e" + bans.stream().filter(b -> b.getType() == OlympaSanctionType.MUTE).count() + " "));
		msg.addExtra(Utils.color("&6Kick: &e" + bans.stream().filter(b -> b.getType() == OlympaSanctionType.KICK).count() + "\n"));

		final String sanctions = bans.stream().filter(b -> b.getStatus() == OlympaSanctionStatus.ACTIVE).map(b -> "&c" + b.getType().getName()).collect(Collectors.joining("&7, "));
		msg.addExtra(Utils.color("&6Sanction Active: " + (sanctions.isEmpty() ? "&aAucune" : sanctions) + "\n"));

		msg.addExtra(Utils.color("&6Historique: "));

		bans.stream().forEach(b -> {
			final BaseComponent[] comp = new ComponentBuilder(
				Utils.colorFix(b.getStatus().getColor() + b.getType().getName().toUpperCase() + " " + b.getStatus().getName().toUpperCase() + " " + b.getReason() + "\n"))
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
