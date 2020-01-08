package fr.olympa.core.bungee.ban.commands.methods;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.UUID;

import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.ban.BanMySQL;
import fr.olympa.core.bungee.ban.BanUtils;
import fr.olympa.core.bungee.ban.MuteUtils;
import fr.olympa.core.bungee.ban.objects.OlympaSanction;
import fr.olympa.core.bungee.ban.objects.OlympaSanctionType;
import fr.olympa.core.bungee.utils.BungeeConfigUtils;
import fr.olympa.core.bungee.utils.BungeeUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class MutePlayer {

	@SuppressWarnings("deprecation")
	public static void addMute(UUID author, CommandSender sender, String targetname, UUID targetUUID, String[] args, OlympaPlayer olymaPlayer) {
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
				if (olympaTarget == null) {
					sender.sendMessage(BungeeConfigUtils.getString("ban.playerneverjoin").replace("%player%", args[0]));
					return;
				}
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

		// Si le joueur n'est pas mute
		OlympaSanction alreadymute = MuteUtils.getMute(olympaTarget.getUniqueId());
		if (alreadymute != null && !MuteUtils.chechExpireBan(alreadymute)) {
			// Sinon annuler le ban
			TextComponent msg = BungeeUtils.formatStringToJSON(BungeeConfigUtils.getString("bungee.ban.messages.alreadymute").replaceAll("%player%", olympaTarget.getName()));
			msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, alreadymute.toBaseComplement()));
			msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/banhist " + alreadymute.getId()));
			sender.sendMessage(msg);
			return;
		}
		java.util.regex.Matcher matcher1 = BanUtils.matchDuration(args[1]);
		java.util.regex.Matcher matcher2 = BanUtils.matchUnit(args[1]);
		// Si la command contient un temps et une unité valide
		if (matcher1.find() && matcher2.find()) {
			// Si la command contient un motif
			if (args.length > 2) {
				String reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
				String time = matcher1.group();
				String unit = matcher2.group();
				long timestamp = BanUtils.toTimeStamp(Integer.parseInt(time), unit);
				long seconds = timestamp - Utils.getCurrentTimeinSeconds();

				if (olymaPlayer != null && OlympaCorePermissions.STAFF.hasPermission(olympaTarget) && OlympaCorePermissions.BAN_BYPASS_SANCTION_STAFF.hasPermission(olymaPlayer)) {
					sender.sendMessage(BungeeConfigUtils.getString("ban.messages.cantmutestaffmembers"));
					return;
				}

				if (seconds <= BungeeConfigUtils.getInt("ban.settings.minmutetime")) {
					sender.sendMessage(BungeeConfigUtils.getString("ban.messages.cantbypassmaxmutetime"));
					return;
				}

				if (seconds >= BungeeConfigUtils.getInt("ban.settings.maxmutetime")) {
					sender.sendMessage(BungeeConfigUtils.getString("ban.messages.cantbypassmminmutetime"));
					return;
				}

				String Stimestamp = Utils.timestampToDuration(timestamp);
				OlympaSanction mute = new OlympaSanction(OlympaSanction.getNextId(), OlympaSanctionType.MUTE, olympaTarget.getUniqueId(), author, reason, Utils.getCurrentTimeinSeconds(), timestamp);
				if (!BanMySQL.addSanction(mute)) {
					sender.sendMessage(BungeeConfigUtils.getString("bungee.ban.messages.errordb"));
					return;
				}
				MuteUtils.addMute(mute);
				// Si Target est connecté
				if (target != null) {
					// Envoyer un message à tous les joueurs du même serveur spigot
					for (ProxiedPlayer players : target.getServer().getInfo().getPlayers()) {
						players.sendMessage(BungeeConfigUtils.getString("bungee.ban.messages.tempmuteannounce")
								.replaceAll("%player%", olympaTarget.getName())
								.replaceAll("%time%", Stimestamp)
								.replaceAll("%reason%", reason));
					}
					target.sendMessage(BungeeConfigUtils.getString("bungee.ban.messages.tempmuteannouncetotarget").replaceAll("%time%", Stimestamp).replaceAll("%reason%", reason));

				}
				// Envoye un message à l'auteur
				TextComponent msg = BungeeUtils.formatStringToJSON(BungeeConfigUtils.getString("bungee.ban.messages.tempmuteannouncetoauthor")
						.replaceAll("%player%", olympaTarget.getName())
						.replaceAll("%time%", Stimestamp)
						.replaceAll("%reason%", reason)
						.replaceAll("%author%", BungeeUtils.getName(author)));
				msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, mute.toBaseComplement()));
				msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/banhist " + mute.getId()));
				OlympaCorePermissions.BAN_SEEBANMSG.sendMessage(msg);
				ProxyServer.getInstance().getConsole().sendMessage(msg);
			} else {
				sender.sendMessage(BungeeConfigUtils.getString("bungee.ban.messages.usagemute"));
			}
			// Sinon: mute def
		} else {
			sender.sendMessage(BungeeConfigUtils.getString("bungee.ban.messages.usagemute"));
			String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

			OlympaSanction mute = new OlympaSanction(OlympaSanction.getNextId(), OlympaSanctionType.MUTE, olympaTarget.getUniqueId(), author, reason, Utils.getCurrentTimeinSeconds(), 0);
			if (!BanMySQL.addSanction(mute)) {
				sender.sendMessage(BungeeConfigUtils.getString("bungee.ban.messages.errordb"));
				return;
			}
			if (target != null) {
				for (ProxiedPlayer players : target.getServer().getInfo().getPlayers()) {
					players.sendMessage(BungeeConfigUtils.getString("bungee.ban.messages.muteannounce").replaceAll("%player%", olympaTarget.getName()).replaceAll("%reason%", reason));
				}
			}

			TextComponent msg = BungeeUtils
					.formatStringToJSON(BungeeConfigUtils.getString("bungee.ban.messages.muteannouncetoauthor").replaceAll("%player%", olympaTarget.getName()).replaceAll("%reason%", reason));
			msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, mute.toBaseComplement()));
			msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/banhist " + mute.getId()));
			OlympaCorePermissions.BAN_SEEBANMSG.sendMessage(msg);
			ProxyServer.getInstance().getConsole().sendMessage(msg);
		}
	}
}
