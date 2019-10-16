package fr.olympa.core.ban.commands.methods;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;

import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.ban.BanMySQL;
import fr.olympa.core.ban.BanUtils;
import fr.olympa.core.ban.objects.OlympaSanction;
import fr.olympa.core.ban.objects.OlympaSanctionType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class BanIp {

	@SuppressWarnings("deprecation")
	public static void addBanIP(final UUID author, final CommandSender sender, final String targetip, final String[] args, final OlympaPlayer olympaPlayer) {
		final java.util.regex.Matcher matcher1 = BanUtils.matchDuration(args[1]);
		final java.util.regex.Matcher matcher2 = BanUtils.matchUnit(args[1]);
		// Si la command contient un temps et une unité valide
		if (matcher1.find() && matcher2.find()) {
			// Si la command contient un motif
			if (args.length > 2) {
				final String reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
				final String time = matcher1.group();
				final String unit = matcher2.group();
				final String ip = args[0];
				final long timestamp = BanUtils.toTimeStamp(Integer.parseInt(time), unit);
				final long seconds = timestamp - Utils.getCurrentTimeinSeconds();

				if (seconds <= BungeeConfigUtils.getInt("bungee.ban.settings.minbantime")) {
					sender.sendMessage(BungeeConfigUtils.getString("bungee.ban.messages.cantbypassmaxbantime"));
					return;
				}
				if (seconds >= BungeeConfigUtils.getInt("bungee.ban.settings.maxbantime")) {
					sender.sendMessage(BungeeConfigUtils.getString("bungee.ban.messages.cantbypassmminbantime"));
					return;
				}
				final String Stimestamp = Utils.timestampToDuration(timestamp);
				final OlympaSanction ban = new OlympaSanction(OlympaSanction.getNextId(), OlympaSanctionType.BANIP, ip, author, reason, Utils.getCurrentTimeinSeconds(), timestamp);
				if (!BanMySQL.addSanction(ban)) {
					sender.sendMessage(BungeeConfigUtils.getString("bungee.ban.messages.errordb"));
					return;
				}

				final List<ProxiedPlayer> targets = ProxyServer.getInstance()
						.getPlayers()
						.stream()
						.filter(target -> target.getAddress().getAddress().getHostAddress().equals(ip))
						.collect(Collectors.toList());

				for (final ProxiedPlayer target : targets) {
					target.disconnect(BungeeUtils.connectScreen(BungeeConfigUtils.getString("bungee.ban.messages.tempbandisconnect")
							.replaceAll("%reason%", ban.getReason())
							.replaceAll("%time%", Stimestamp)
							.replaceAll("%id%", String.valueOf(ban.getId()))));
					for (final ProxiedPlayer players : target.getServer().getInfo().getPlayers()) {
						players.sendMessage(BungeeConfigUtils.getString("bungee.ban.messages.tempbanannounce")
								.replaceAll("%player%", target.getName())
								.replaceAll("%time%", Stimestamp)
								.replaceAll("%reason%", reason));
					}
				}

				// Envoye un message au staff
				final TextComponent msg = BungeeUtils.formatStringToJSON(BungeeConfigUtils.getString("bungee.ban.messages.tempbanipannouncetoauthor")
						.replaceAll("%ip%", BungeeUtils.getPlayersNamesByIp(ip))
						.replaceAll("%time%", Stimestamp)
						.replaceAll("%reason%", reason));
				msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, ban.toBaseComplement()));
				msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/banhist " + ban.getId()));

				BungeeUtils.sendMessageToStaff(msg);
			}
		} else {
			final String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
			final String ip = args[0];
			final OlympaSanction ban = new OlympaSanction(OlympaSanction.getNextId(), OlympaSanctionType.BANIP, ip, author, reason, Utils.getCurrentTimeinSeconds(), 0);

			if (!BanMySQL.addSanction(ban)) {
				sender.sendMessage(BungeeConfigUtils.getString("bungee.ban.messages.errordb"));
				return;
			}

			for (final ProxiedPlayer target : ProxyServer.getInstance()
					.getPlayers()
					.stream()
					.filter(player -> player.getAddress().getAddress().getHostAddress().equals(ip))
					.collect(Collectors.toList())) {
				target.disconnect(BungeeUtils
						.connectScreen(BungeeConfigUtils.getString("bungee.ban.messages.bandisconnect").replaceAll("%reason%", ban.getReason()).replaceAll("%id%", String.valueOf(ban.getId()))));
				for (final ProxiedPlayer players : target.getServer().getInfo().getPlayers()) {
					players.sendMessage(BungeeConfigUtils.getString("bungee.ban.messages.banannounce").replaceAll("%player%", target.getName()).replaceAll("%reason%", reason));
				}
			}

			// Envoye un message au staff
			final TextComponent msg = BungeeUtils.formatStringToJSON(
					BungeeConfigUtils.getString("bungee.ban.messages.banipannouncetoauthor").replaceAll("%ip%", BungeeUtils.getPlayersNamesByIp(ip)).replaceAll("%reason%", reason));
			msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, ban.toBaseComplement()));
			msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/banhist " + ban.getId()));
			BungeeUtils.sendMessageToStaff(msg);
		}
	}
}
