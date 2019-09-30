package fr.tristiisch.olympa.core.ban.commands.methods;

import java.util.Arrays;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.tristiisch.olympa.api.objects.OlympaPlayer;
import fr.tristiisch.olympa.api.permission.OlympaPermission;
import fr.tristiisch.olympa.api.utils.Utils;
import fr.tristiisch.olympa.core.ban.BanMySQL;
import fr.tristiisch.olympa.core.ban.BanUtils;
import fr.tristiisch.olympa.core.ban.objects.EmeraldBan;
import fr.tristiisch.olympa.core.ban.objects.EmeraldBanType;
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
	public static void addBanUsername(UUID author, CommandSender sender, String targetname, UUID targetUUID, String[] args, OlympaPlayer emeraldPlayer) {
		// /ban <pseudo> <time unit> <reason>
		// args[0] = target
		// args[1] = time + unit
		// args[2] & + = reason

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
				sender.sendMessage(BungeeConfigUtils.getString("commun.messages.playerneverjoin").replaceAll("%player%", args[0]));
				return;
			}
		}

		// Si le joueur n'est pas banni
		EmeraldBan alreadyban = BanMySQL.getActiveSanction(olympaTarget.getUniqueId(), EmeraldBanType.BAN);
		if (alreadyban != null) {
			// Sinon annuler le ban
			TextComponent msg = BungeeUtils.formatStringToJSON(BungeeConfigUtils.getString("bungee.ban.messages.alreadyban").replaceAll("%player%", olympaTarget.getName()));
			msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, alreadyban.toBaseComplement()));
			msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/banhist " + alreadyban.getId()));
			sender.spigot().sendMessage(msg);
			target.spigot().sendMessage(new TextComponent("Hello world"));
			return;
		}
		Matcher matcher1 = BanUtils.matchDuration(args[1]);
		Matcher matcher2 = BanUtils.matchUnit(args[1]);
		// Si la command contient un temps et une unité valide
		if (matcher1.find() && matcher2.find()) {
			// Si la command contient un motif
			if (args.length > 2) {
				String reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
				String time = matcher1.group();
				String unit = matcher2.group();
				long timestamp = BanUtils.toTimeStamp(Integer.parseInt(time), unit);
				long seconds = timestamp - Utils.getCurrentTimeinSeconds();

				if (olympaTarget.hasPermission(OlympaPermission.BAN_BYPASS_BAN)) {
					sender.sendMessage(BungeeConfigUtils.getString("bungee.ban.messages.cantbanstaffmembers"));
					return;
				}
				if (seconds <= BungeeConfigUtils.getInt("bungee.ban.settings.minbantime")) {
					sender.sendMessage(BungeeConfigUtils.getString("bungee.ban.messages.cantbypassmaxbantime"));
					return;
				}
				if (seconds >= BungeeConfigUtils.getInt("bungee.ban.settings.maxbantime")) {
					sender.sendMessage(BungeeConfigUtils.getString("bungee.ban.messages.cantbypassmminbantime"));
					return;
				}
				String Stimestamp = Utils.timestampToDuration(timestamp);
				EmeraldBan ban = new EmeraldBan(EmeraldBan.getNextID(), EmeraldBanType.BAN, olympaTarget.getUniqueId(), author, reason, Utils.getCurrentTimeinSeconds(), timestamp);
				if (!BanMySQL.addSanction(ban)) {
					sender.sendMessage(BungeeConfigUtils.getString("bungee.ban.messages.errordb"));
					return;
				}
				// Si Target est connecté
				if (target != null) {
					// Envoyer un message à tous les joueurs du même serveur spigot
					for (ProxiedPlayer players : target.getServer().getInfo().getPlayers()) {
						players.sendMessage(BungeeConfigUtils.getString("bungee.ban.messages.tempbanannounce")
								.replaceAll("%player%", olympaTarget.getName())
								.replaceAll("%time%", Stimestamp)
								.replaceAll("%reason%", reason));
					}
					// Envoyer un message à Target lors de la déconnexion
					target.disconnect(BungeeUtils.connectScreen(BungeeConfigUtils.getString("bungee.ban.messages.tempbandisconnect")
							.replaceAll("%reason%", ban.getReason())
							.replaceAll("%time%", Stimestamp)
							.replaceAll("%id%", String.valueOf(ban.getId()))));
				}
				// Envoye un message à l'auteur
				TextComponent msg = BungeeUtils.formatStringToJSON(BungeeConfigUtils.getString("bungee.ban.messages.tempbanannouncetoauthor")
						.replaceAll("%player%", olympaTarget.getName())
						.replaceAll("%time%", Stimestamp)
						.replaceAll("%reason%", reason)
						.replaceAll("%author%", BungeeUtils.getName(author)));
				msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, ban.toBaseComplement()));
				msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/banhist " + ban.getId()));
				for (ProxiedPlayer player : ProxyServer.getInstance()
						.getPlayers()
						.stream()
						.filter(p -> new AccountProvider(p.getUniqueId()).getEmeraldPlayer().getGroup().isStaffMember())
						.collect(Collectors.toList())) {
					player.sendMessage(msg);
				}
				ProxyServer.getInstance().getConsole().sendMessage(msg);
			} else {
				sender.sendMessage(BungeeConfigUtils.getString("bungee.ban.messages.usageban"));
			}
			// Sinon: ban def
		} else {
			if (emeraldPlayer != null && !emeraldPlayer.hasPowerMoreThan(BungeeConfigUtils.getInt("bungee.ban.settings.powerbandef"))) {
				sender.sendMessage(BungeeConfigUtils.getString("bungee.ban.messages.usageban"));
				return;
			}
			String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

			EmeraldBan ban = new EmeraldBan(EmeraldBan.getNextID(), EmeraldBanType.BAN, olympaTarget.getUniqueId(), author, reason, Utils.getCurrentTimeinSeconds(), 0);
			if (!BanMySQL.addSanction(ban)) {
				sender.sendMessage(BungeeConfigUtils.getString("bungee.ban.messages.errordb"));
				return;
			}

			// Si Target est connecté
			if (target != null) {
				// Envoyer un message à tous les joueurs du même serveur spigot
				for (ProxiedPlayer players : target.getServer().getInfo().getPlayers()) {
					players.sendMessage(BungeeConfigUtils.getString("bungee.ban.messages.banannounce").replaceAll("%player%", olympaTarget.getName()).replaceAll("%reason%", reason));
				}
				// Envoyer un message à Target lors de la déconnexion
				target.disconnect(BungeeUtils
						.connectScreen(BungeeConfigUtils.getString("bungee.ban.messages.bandisconnect").replaceAll("%reason%", ban.getReason()).replaceAll("%id%", String.valueOf(ban.getId()))));
			}
			// Envoye un message à l'auteur
			TextComponent msg = BungeeUtils.formatStringToJSON(BungeeConfigUtils.getString("bungee.ban.messages.banannouncetoauthor")
					.replaceAll("%player%", olympaTarget.getName())
					.replaceAll("%reason%", reason)
					.replaceAll("%author%", BungeeUtils.getName(author)));
			msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, ban.toBaseComplement()));
			msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/banhist " + ban.getId()));
			BungeeUtils.sendMessageToStaff(msg);
			ProxyServer.getInstance().getConsole().sendMessage(msg);
		}
	}
}
