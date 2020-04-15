package fr.olympa.core.bungee.ban.commands.methods;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.ColorUtils;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.bungee.ban.BanMySQL;
import fr.olympa.core.bungee.ban.objects.OlympaSanction;
import fr.olympa.core.bungee.ban.objects.OlympaSanctionStatus;
import fr.olympa.core.bungee.ban.objects.OlympaSanctionType;
import fr.olympa.core.bungee.utils.BungeeConfigUtils;
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
	public static void histBan(CommandSender sender, String name, UUID uuid) {
		ProxiedPlayer target = null;
		OlympaPlayer olympaTarget = null;
		if (uuid != null) {
			target = ProxyServer.getInstance().getPlayer(uuid);

		} else if (name != null) {
			target = ProxyServer.getInstance().getPlayer(name);

		} else {
			throw new NullPointerException("The uuid or name must be specified");
		}

		try {
			if (target != null) {
				olympaTarget = AccountProvider.get(target.getUniqueId());
			} else if (uuid != null) {
				olympaTarget = AccountProvider.getFromDatabase(uuid);
			} else if (name != null) {
				olympaTarget = AccountProvider.getFromDatabase(name);
			}
		} catch (SQLException e) {
			sender.sendMessage(BungeeConfigUtils.getString("ban.messages.errordb"));
			e.printStackTrace();
			return;
		}

		List<OlympaSanction> bans = BanMySQL.getSanctions(olympaTarget.getUniqueId());

		if (bans == null) {
			sender.sendMessage(BungeeConfigUtils.getString("bungee.ban.messages.errordb"));
			return;
		}

		if (bans.size() == 0) {
			sender.sendMessage(ColorUtils.color(Prefix.DEFAULT_BAD + "&4%player%&c n'a jamais été sanctionné.").replaceAll("%player%", olympaTarget.getName()));
			return;
		}

		TextComponent msg = new TextComponent(ColorUtils.color("&6Historique des sanctions de " + olympaTarget.getGroup().getPrefix() + olympaTarget.getName() + "&6:\n"));
		msg.addExtra(ColorUtils.color("&6Bans: &e" + bans.stream().filter(b -> b.getType() == OlympaSanctionType.BAN).count() + " "));
		msg.addExtra(ColorUtils.color("&6Mute: &e" + bans.stream().filter(b -> b.getType() == OlympaSanctionType.MUTE).count() + " "));
		msg.addExtra(ColorUtils.color("&6Kick: &e" + bans.stream().filter(b -> b.getType() == OlympaSanctionType.KICK).count() + "\n"));

		String sanctions = bans.stream().filter(b -> b.getStatus() == OlympaSanctionStatus.ACTIVE).map(b -> "&c" + b.getType().getName()).collect(Collectors.joining("&7, "));
		msg.addExtra(ColorUtils.color("&6Sanction Active: " + (sanctions.isEmpty() ? "&aAucune" : sanctions) + "\n"));

		msg.addExtra(ColorUtils.color("&6Historique: "));

		bans.stream().forEach(b -> {
			BaseComponent[] comp = new ComponentBuilder(
					ColorUtils.color(b.getStatus().getColor() + b.getType().getName().toUpperCase() + " " + b.getStatus().getName().toUpperCase() + " " + b.getReason() + "\n"))
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
