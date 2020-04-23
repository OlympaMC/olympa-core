package fr.olympa.core.bungee.ban.commands.methods;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.ban.BanMySQL;
import fr.olympa.core.bungee.ban.BanUtils;
import fr.olympa.core.bungee.ban.commands.BanCommand;
import fr.olympa.core.bungee.ban.objects.OlympaSanction;
import fr.olympa.core.bungee.ban.objects.OlympaSanctionType;
import fr.olympa.core.bungee.utils.BungeeUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;

public class BanIp {

	@SuppressWarnings("deprecation")
	public static void addBanIP(UUID author, CommandSender sender, String targetip, String[] args, OlympaPlayer olympaPlayer) {
		java.util.regex.Matcher matcher1 = BanUtils.matchDuration(args[1]);
		java.util.regex.Matcher matcher2 = BanUtils.matchUnit(args[1]);
		Configuration config = OlympaBungee.getInstance().getConfig();
		// Si la command contient un temps et une unité valide
		if (matcher1.find() && matcher2.find()) {
			// Si la command contient un motif
			if (args.length > 2) {
				String reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
				String time = matcher1.group();
				String unit = matcher2.group();
				String ip = args[0];
				long timestamp = BanUtils.toTimeStamp(Integer.parseInt(time), unit);
				long seconds = timestamp - Utils.getCurrentTimeInSeconds();

				if (seconds <= config.getInt("bungee.ban.settings.minbantime")) {
					sender.sendMessage(config.getString("bungee.ban.messages.cantbypassmaxbantime"));
					return;
				}
				if (seconds >= config.getInt("bungee.ban.settings.maxbantime")) {
					sender.sendMessage(config.getString("bungee.ban.messages.cantbypassmminbantime"));
					return;
				}
				String Stimestamp = Utils.timestampToDuration(timestamp);
				OlympaSanction ban = new OlympaSanction(OlympaSanction.getNextId(), OlympaSanctionType.BANIP, ip, author, reason, Utils.getCurrentTimeInSeconds(), timestamp);
				if (!BanMySQL.addSanction(ban)) {
					sender.sendMessage(config.getString("bungee.ban.messages.errordb"));
					return;
				}

				List<ProxiedPlayer> targets = ProxyServer.getInstance()
						.getPlayers()
						.stream()
						.filter(target -> target.getAddress().getAddress().getHostAddress().equals(ip))
						.collect(Collectors.toList());

				for (ProxiedPlayer target : targets) {
					target.disconnect(BungeeUtils.connectScreen(config.getString("bungee.ban.messages.tempbandisconnect")
							.replaceAll("%reason%", ban.getReason())
							.replaceAll("%time%", Stimestamp)
							.replaceAll("%id%", String.valueOf(ban.getId()))));
					for (ProxiedPlayer players : target.getServer().getInfo().getPlayers()) {
						players.sendMessage(config.getString("bungee.ban.messages.tempbanannounce")
								.replaceAll("%player%", target.getName())
								.replaceAll("%time%", Stimestamp)
								.replaceAll("%reason%", reason));
					}
				}

				// Envoye un message au staff
				TextComponent msg = BungeeUtils.formatStringToJSON(config.getString("bungee.ban.messages.tempbanipannouncetoauthor")
						.replaceAll("%ip%", BungeeUtils.getPlayersNamesByIp(ip))
						.replaceAll("%time%", Stimestamp)
						.replaceAll("%reason%", reason));
				msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, ban.toBaseComplement()));
				msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/banhist " + ban.getId()));

				OlympaCorePermissions.BAN_SEEBANMSG.sendMessage(msg);
			}
		} else {
			if (olympaPlayer != null && BanCommand.permToBandef.hasPermission(olympaPlayer)) {
				sender.sendMessage(Prefix.DEFAULT_BAD + "Tu as pas la permission de ban définitivement, tu peux ban maximum 1 an.");
				return;
			}
			String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
			String ip = args[0];
			OlympaSanction ban = new OlympaSanction(OlympaSanction.getNextId(), OlympaSanctionType.BANIP, ip, author, reason, Utils.getCurrentTimeInSeconds(), 0);

			if (!BanMySQL.addSanction(ban)) {
				sender.sendMessage(config.getString("bungee.ban.messages.errordb"));
				return;
			}

			for (ProxiedPlayer target : ProxyServer.getInstance()
					.getPlayers()
					.stream()
					.filter(player -> player.getAddress().getAddress().getHostAddress().equals(ip))
					.collect(Collectors.toList())) {
				target.disconnect(BungeeUtils
						.connectScreen(config.getString("bungee.ban.messages.bandisconnect").replaceAll("%reason%", ban.getReason()).replaceAll("%id%", String.valueOf(ban.getId()))));
				for (ProxiedPlayer players : target.getServer().getInfo().getPlayers()) {
					players.sendMessage(config.getString("bungee.ban.messages.banannounce").replaceAll("%player%", target.getName()).replaceAll("%reason%", reason));
				}
			}

			// Envoye un message au staff
			TextComponent msg = BungeeUtils.formatStringToJSON(
					config.getString("bungee.ban.messages.banipannouncetoauthor").replaceAll("%ip%", BungeeUtils.getPlayersNamesByIp(ip)).replaceAll("%reason%", reason));
			msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, ban.toBaseComplement()));
			msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/banhist " + ban.getId()));
			OlympaCorePermissions.BAN_SEEBANMSG.sendMessage(msg);
		}
	}
}
