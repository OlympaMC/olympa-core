package fr.tristiisch.olympa.core.ban.commands.methods;

import java.util.Arrays;
import java.util.UUID;
import java.util.regex.Matcher;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.tristiisch.olympa.api.objects.OlympaPlayer;
import fr.tristiisch.olympa.api.permission.OlympaPermission;
import fr.tristiisch.olympa.api.plugin.OlympaPlugin;
import fr.tristiisch.olympa.api.utils.SpigotUtils;
import fr.tristiisch.olympa.api.utils.Utils;
import fr.tristiisch.olympa.core.ban.BanMySQL;
import fr.tristiisch.olympa.core.ban.BanUtils;
import fr.tristiisch.olympa.core.ban.objects.OlympaSanction;
import fr.tristiisch.olympa.core.ban.objects.OlympaSanctionType;
import fr.tristiisch.olympa.core.datamanagment.redis.access.OlympaAccountProvider;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class BanPlayer {

	/**
	 * Ajoute un ban
	 * targetUUID ou targetname ne doit pas être null.
	 *
	 * @param author is a UUID of author of ban or String (If the author is Console, author = "Console")
	 * @param targetname Name of player to ban. case insensitive
	 */
	@SuppressWarnings("deprecation")
	public static void addBanPlayer(UUID author, CommandSender sender, String targetname, UUID targetUUID, String[] args, OlympaPlayer olympaPlayer) {
		// /ban <pseudo> <time unit> <reason>
		// args[0] = target
		// args[1] = time + unit
		// args[2] & + = reason

		long currentTime = Utils.getCurrentTimeinSeconds();
		Player player;
		if (sender instanceof Player) {
			player = (Player) sender;
		}
		Player target = null;
		OlympaPlayer olympaTarget = null;
		if (targetUUID != null) {
			target = Bukkit.getPlayer(targetUUID);

		} else if (targetname != null) {
			target = Bukkit.getPlayer(targetname);

		} else {
			throw new NullPointerException("The uuid or name must be specified");
		}

		if (target != null) {
			olympaTarget = OlympaAccountProvider.get(target);

		} else {
			olympaTarget = OlympaAccountProvider.getFromDatabase(targetUUID);
			if (olympaTarget == null) {
				sender.sendMessage(OlympaPlugin.getInstance().getConfig().getString("ban.playerneverjoin").replace("%player%", args[0]));
				return;
			}
		}

		// Si le joueur n'est pas banni
		OlympaSanction alreadyban = BanMySQL.getSanctionActive(olympaTarget.getUniqueId(), OlympaSanctionType.BAN);
		if (alreadyban != null) {
			// Sinon annuler le ban
			TextComponent msg = SpigotUtils.formatStringToJSON(OlympaPlugin.getInstance().getConfig().getString("ban.alreadyban").replace("%player%", olympaTarget.getName()));
			msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, alreadyban.toBaseComplement()));
			msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/banhist " + alreadyban.getId()));
			if (player != null) {
				player.spigot().sendMessage(msg);
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
				sender.sendMessage(OlympaPlugin.getInstance().getConfig().getString("ban.usageban"));
				return;
			}
			// Si la command contient un motif
			String reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
			String time = matcher1.group();
			String unit = matcher2.group();
			long expire = BanUtils.toTimeStamp(Integer.parseInt(time), unit);
			long seconds = expire - currentTime;

			if (olympaTarget.hasPermission(OlympaPermission.BAN_BYPASS_BAN)) {
				sender.sendMessage(OlympaPlugin.getInstance().getConfig().getString("ban.cantbanstaffmembers"));
				return;
			}
			// 600 = 10 mins
			if (seconds < 600) {
				sender.sendMessage(OlympaPlugin.getInstance().getConfig().getString("ban.cantbypassmaxbantime"));
				return;
			}
			// 527040 = 1 an en année bisextille
			if (seconds > 527040) {
				sender.sendMessage(OlympaPlugin.getInstance().getConfig().getString("ban.cantbypassmminbantime"));
				return;
			}
			String expireString = Utils.timestampToDuration(expire);
			OlympaSanction ban = new OlympaSanction(OlympaSanction.getNextId(), OlympaSanctionType.BAN, olympaTarget.getUniqueId(), author, reason, currentTime, expire);
			if (!BanMySQL.addSanction(ban)) {
				sender.sendMessage(OlympaPlugin.getInstance().getConfig().getString("ban.errordb"));
				return;
			}
			// Si Target est connecté
			if (target != null) {
				// Envoyer un message à Target lors de la déconnexion
				target.kickPlayer(SpigotUtils.connectScreen(OlympaPlugin.getInstance().getConfig().getString("ban.tempbandisconnect")
						.replace("%reason%", ban.getReason())
						.replace("%time%", expireString)
						.replace("%id%", String.valueOf(ban.getId()))));

				// Envoyer un message à tous les joueurs du même serveur spigot
				Bukkit.broadcastMessage(OlympaPlugin.getInstance().getConfig().getString("ban.tempbanannounce")
						.replace("%player%", olympaTarget.getName())
						.replace("%time%", expireString)
						.replace("%reason%", reason));

			}
			// Envoye un message à l'auteur (+ staff)
			TextComponent msg = SpigotUtils.formatStringToJSON(OlympaPlugin.getInstance().getConfig().getString("ban.tempbanannouncetoauthor")
					.replace("%player%", olympaTarget.getName())
					.replace("%time%", expireString)
					.replace("%reason%", reason)
					.replace("%author%", SpigotUtils.getName(author)));
			msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, ban.toBaseComplement()));
			msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/banhist " + ban.getId()));

			OlympaPermission.BAN_SEEBANMSG.sendMessage(msg);
			Bukkit.getConsoleSender().sendMessage(msg.toPlainText());

			// Sinon: ban def
		} else {
			if (OlympaPermission.BAN_DEF.hasPermission(olympaPlayer)) {
				sender.sendMessage(OlympaPlugin.getInstance().getConfig().getString("ban.usageban"));
				return;
			}
			String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

			OlympaSanction ban = new OlympaSanction(OlympaSanction.getNextId(), OlympaSanctionType.BAN, olympaTarget.getUniqueId(), author, reason, Utils.getCurrentTimeinSeconds(), 0);
			if (!BanMySQL.addSanction(ban)) {
				sender.sendMessage(OlympaPlugin.getInstance().getConfig().getString("ban.errordb"));
				return;
			}

			// Si Target est connecté
			if (target != null) {
				// Envoyer un message à Target lors de la déconnexion
				target.kickPlayer(SpigotUtils.connectScreen(OlympaPlugin.getInstance().getConfig().getString("ban.bandisconnect").replace("%reason%", reason).replace("%id%", String.valueOf(ban.getId()))));

				// Envoyer un message à tous les joueurs du même serveur spigot
				Bukkit.broadcastMessage(OlympaPlugin.getInstance().getConfig().getString("ban.banannounce").replace("%player%", olympaTarget.getName()).replace("%reason%", reason));

			}
			// Envoye un message à l'auteur (+ staff)
			TextComponent msg = SpigotUtils.formatStringToJSON(OlympaPlugin.getInstance().getConfig().getString("ban.banannouncetoauthor")
					.replace("%player%", olympaTarget.getName())
					.replace("%reason%", reason)
					.replace("%author%", SpigotUtils.getName(author)));
			msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, ban.toBaseComplement()));
			msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/banhist " + ban.getId()));

			OlympaPermission.BAN_SEEBANMSG.sendMessage(msg);
			Bukkit.getConsoleSender().sendMessage(msg.toPlainText());
		}
	}
}
