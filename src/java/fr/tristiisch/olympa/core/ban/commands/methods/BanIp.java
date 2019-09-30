package fr.tristiisch.olympa.core.ban.commands.methods;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import fr.tristiisch.emeraldmc.api.bungee.ban.BanMySQL;
import fr.tristiisch.emeraldmc.api.bungee.ban.BanUtils;
import fr.tristiisch.emeraldmc.api.bungee.ban.objects.EmeraldBan;
import fr.tristiisch.emeraldmc.api.bungee.ban.objects.EmeraldBanType;
import fr.tristiisch.emeraldmc.api.bungee.utils.BungeeConfigUtils;
import fr.tristiisch.emeraldmc.api.bungee.utils.BungeeUtils;
import fr.tristiisch.emeraldmc.api.commons.Utils;
import fr.tristiisch.emeraldmc.api.commons.object.EmeraldPlayer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class BanIp {

	@SuppressWarnings("deprecation")
	public static void addBanIP(final UUID author, final CommandSender sender, final String targetip, final String[] args, final EmeraldPlayer emeraldPlayer) {
		final java.util.regex.Matcher matcher1 = BanUtils.matchDuration(args[1]);
		final java.util.regex.Matcher matcher2 = BanUtils.matchUnit(args[1]);
		// Si la command contient un temps et une unité valide
		if(matcher1.find() && matcher2.find()) {
			// Si la command contient un motif
			if(args.length > 2) {
				final String reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
				final String time = matcher1.group();
				final String unit = matcher2.group();
				final String ip = args[0];
				final long timestamp = BanUtils.toTimeStamp(Integer.parseInt(time), unit);
				final long seconds = timestamp - Utils.getCurrentTimeinSeconds();

				if(seconds <= BungeeConfigUtils.getInt("bungee.ban.settings.minbantime")) {
					sender.sendMessage(BungeeConfigUtils.getString("bungee.ban.messages.cantbypassmaxbantime"));
					return;
				}
				if(seconds >= BungeeConfigUtils.getInt("bungee.ban.settings.maxbantime")) {
					sender.sendMessage(BungeeConfigUtils.getString("bungee.ban.messages.cantbypassmminbantime"));
					return;
				}
				final String Stimestamp = Utils.timestampToDuration(timestamp);
				final EmeraldBan ban = new EmeraldBan(EmeraldBan.getNextID(), EmeraldBanType.BANIP, ip, author, reason, Utils.getCurrentTimeinSeconds(), timestamp);
				if(!BanMySQL.addSanction(ban)) {
					sender.sendMessage(BungeeConfigUtils.getString("bungee.ban.messages.errordb"));
					return;
				}

				final List<ProxiedPlayer> targets = ProxyServer.getInstance()
						.getPlayers()
						.stream()
						.filter(target -> target.getAddress().getAddress().getHostAddress().equals(ip))
						.collect(Collectors.toList());

				for(final ProxiedPlayer target : targets) {
					target.disconnect(BungeeUtils.connectScreen(BungeeConfigUtils.getString("bungee.ban.messages.tempbandisconnect")
							.replaceAll("%reason%", ban.getReason())
							.replaceAll("%time%", Stimestamp)
							.replaceAll("%id%", String.valueOf(ban.getId()))));
					for(final ProxiedPlayer players : target.getServer().getInfo().getPlayers()) {
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
			final EmeraldBan ban = new EmeraldBan(EmeraldBan.getNextID(), EmeraldBanType.BANIP, ip, author, reason, Utils.getCurrentTimeinSeconds(), 0);

			if(!BanMySQL.addSanction(ban)) {
				sender.sendMessage(BungeeConfigUtils.getString("bungee.ban.messages.errordb"));
				return;
			}

			for(final ProxiedPlayer target : ProxyServer.getInstance()
					.getPlayers()
					.stream()
					.filter(player -> player.getAddress().getAddress().getHostAddress().equals(ip))
					.collect(Collectors.toList())) {
				target.disconnect(BungeeUtils
						.connectScreen(BungeeConfigUtils.getString("bungee.ban.messages.bandisconnect").replaceAll("%reason%", ban.getReason()).replaceAll("%id%", String.valueOf(ban.getId()))));
				for(final ProxiedPlayer players : target.getServer().getInfo().getPlayers()) {
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
