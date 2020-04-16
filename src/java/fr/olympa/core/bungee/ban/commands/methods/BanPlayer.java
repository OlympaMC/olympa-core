package fr.olympa.core.bungee.ban.commands.methods;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.UUID;
import java.util.regex.Matcher;

import org.bukkit.Bukkit;

import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.SpigotUtils;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.ban.BanMySQL;
import fr.olympa.core.bungee.ban.BanUtils;
import fr.olympa.core.bungee.ban.commands.BanIpCommand;
import fr.olympa.core.bungee.ban.objects.OlympaSanction;
import fr.olympa.core.bungee.ban.objects.OlympaSanctionType;
import fr.olympa.core.bungee.utils.BungeeConfigUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;

public class BanPlayer {

	/**
	 * Ajoute un ban targetUUID ou targetname ne doit pas être null.
	 *
	 * @param author     is a UUID of author of ban or String (If the author is
	 *                   Console, author = "Console")
	 * @param targetname Name of player to ban. case insensitive
	 */
	@SuppressWarnings("deprecation")
	public static void addBanPlayer(UUID author, CommandSender sender, String targetname, UUID targetUUID, String[] args, OlympaPlayer olympaPlayer) {
		// /ban <pseudo> <time unit> <reason>
		// args[0] = target
		// args[1] = time + unit
		// args[2] & + = reason

		Configuration config = BungeeConfigUtils.getDefaultConfig();
		long currentTime = Utils.getCurrentTimeInSeconds();
		ProxiedPlayer player = null;
		if (sender instanceof ProxiedPlayer) {
			player = (ProxiedPlayer) sender;
		}
		ProxiedPlayer target = null;
		OlympaPlayer olympaTarget = null;
		if (targetUUID != null) {
			target = ProxyServer.getInstance().getPlayer(targetUUID);

		} else if (targetname != null) {
			target = ProxyServer.getInstance().getPlayer(targetname);

		} else {
			throw new NullPointerException("The uuid or name must be specified");
		}

		try {
			if (target != null) {
				olympaTarget = AccountProvider.get(target.getUniqueId());
			} else if (targetUUID != null) {
				olympaTarget = AccountProvider.getFromDatabase(targetUUID);
			} else if (targetname != null) {
				olympaTarget = AccountProvider.getFromDatabase(targetname);
			}
		} catch (SQLException e) {
			sender.sendMessage(BungeeConfigUtils.getString("ban.messages.errordb"));
			e.printStackTrace();
			return;
		}

		// Si le joueur n'est pas banni
		OlympaSanction alreadyban = BanMySQL.getSanctionActive(olympaTarget.getUniqueId(), OlympaSanctionType.BAN);
		if (alreadyban != null) {
			// Sinon annuler le ban
			TextComponent msg = new TextComponent(config.getString("ban.alreadyban").replace("%player%", olympaTarget.getName()));
			msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, alreadyban.toBaseComplement()));
			msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/banhist " + alreadyban.getId()));
			if (player != null) {
				player.sendMessage(msg);
			} else {
				sender.sendMessage(msg.toLegacyText());
			}
			return;
		}
		Matcher matcher1 = BanUtils.matchDuration(args[1]);
		Matcher matcher2 = BanUtils.matchUnit(args[1]);
		// Si la command contient un temps et une unité valide
		if (matcher1.find() && matcher2.find()) {
			if (args.length <= 2) {
				sender.sendMessage(config.getString("ban.usageban"));
				return;
			}
			// Si la command contient un motif
			String reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
			String time = matcher1.group();
			String unit = matcher2.group();
			long expire = BanUtils.toTimeStamp(Integer.parseInt(time), unit);
			long seconds = expire - currentTime;

			if (OlympaCorePermissions.BAN_BYPASS_BAN.hasPermission(olympaTarget)) {
				sender.sendMessage(config.getString("ban.cantbanstaffmembers"));
				return;
			}
			// 600 = 10 mins
			if (seconds < 600) {
				sender.sendMessage(config.getString("ban.cantbypassmaxbantime"));
				return;
			}
			// 527040 = 1 an en année bisextille
			if (seconds > 527040) {
				sender.sendMessage(config.getString("ban.cantbypassmminbantime"));
				return;
			}
			String expireString = Utils.timestampToDuration(expire);
			OlympaSanction ban = new OlympaSanction(OlympaSanction.getNextId(), OlympaSanctionType.BAN, olympaTarget.getUniqueId(), author, reason, currentTime, expire);
			if (!BanMySQL.addSanction(ban)) {
				sender.sendMessage(config.getString("ban.errordb"));
				return;
			}
			// Si Target est connecté
			if (target != null) {
				// Envoyer un message à Target lors de la déconnexion
				target.disconnect(SpigotUtils.connectScreen(config.getString("ban.tempbandisconnect"))
						.replace("%reason%", ban.getReason())
						.replace("%time%", expireString)
						.replace("%id%", String.valueOf(ban.getId())));

				// Envoyer un message à tous les joueurs du même serveur spigot
				ProxyServer.getInstance().broadcast(config.getString("ban.tempbanannounce")
						.replace("%player%", olympaTarget.getName())
						.replace("%time%", expireString)
						.replace("%reason%", reason));

			}
			// Envoye un message à l'auteur (+ staff)
			TextComponent msg = new TextComponent(config.getString("ban.tempbanannouncetostaff")
					.replace("%player%", olympaTarget.getName())
					.replace("%time%", expireString)
					.replace("%reason%", reason)
					.replace("%author%", SpigotUtils.getName(author)));
			msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, ban.toBaseComplement()));
			msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/banhist " + ban.getId()));

			OlympaCorePermissions.BAN_SEEBANMSG.sendMessage(msg);
			ProxyServer.getInstance().getConsole().sendMessage(msg.toPlainText());

			// Sinon: ban def
		} else {
			if (olympaPlayer != null && BanIpCommand.permToBandef.hasPermission(olympaPlayer)) {
				sender.sendMessage(Prefix.DEFAULT_BAD + "Tu as pas la permission de ban définitivement, tu peux ban maximum 1 an.");
				return;
			}
			String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

			OlympaSanction ban = new OlympaSanction(OlympaSanction.getNextId(), OlympaSanctionType.BAN, olympaTarget.getUniqueId(), author, reason, Utils.getCurrentTimeInSeconds(), 0);
			if (!BanMySQL.addSanction(ban)) {
				sender.sendMessage(config.getString("ban.errordb"));
				return;
			}

			// Si Target est connecté
			if (target != null) {
				// Envoyer un message à Target lors de la déconnexion
				target.disconnect(SpigotUtils.connectScreen(config.getString("ban.bandisconnect").replace("%reason%", reason).replace("%id%", String.valueOf(ban.getId()))));

				// Envoyer un message à tous les joueurs du même serveur spigot
				Bukkit.broadcastMessage(config.getString("ban.banannounce").replace("%player%", olympaTarget.getName()).replace("%reason%", reason));

			}
			// Envoye un message à l'auteur (+ staff)
			TextComponent msg = new TextComponent(config.getString("ban.banannouncetostaff")
					.replace("%player%", olympaTarget.getName())
					.replace("%reason%", reason)
					.replace("%author%", SpigotUtils.getName(author)));
			msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, ban.toBaseComplement()));
			msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/banhist " + ban.getId()));

			OlympaCorePermissions.BAN_SEEBANMSG.sendMessage(msg);
			ProxyServer.getInstance().getConsole().sendMessage(msg);
		}
	}
}
