package fr.olympa.core.bungee.ban.commands.methods;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.UUID;

import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.ban.MuteUtils;
import fr.olympa.core.bungee.ban.SanctionUtils;
import fr.olympa.core.bungee.ban.objects.OlympaSanction;
import fr.olympa.core.bungee.ban.objects.OlympaSanctionType;
import fr.olympa.core.bungee.ban.objects.SanctionExecuteTarget;
import fr.olympa.core.bungee.utils.BungeeUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;

public class MutePlayer {

	@SuppressWarnings("deprecation")
	public static void addMute(long authorId, CommandSender sender, String targetname, UUID targetUUID, String[] args, OlympaPlayer olymaPlayer) {
		ProxiedPlayer target = null;
		OlympaPlayer olympaTarget = null;
		if (targetUUID != null)
			target = ProxyServer.getInstance().getPlayer(targetUUID);
		else if (targetname != null)
			target = ProxyServer.getInstance().getPlayer(targetname);
		else
			throw new NullPointerException("The uuid or name must be specified");

		Configuration config = OlympaBungee.getInstance().getConfig();
		try {
			if (target != null) {
				olympaTarget = new AccountProvider(target.getUniqueId()).get();
				if (olympaTarget == null) {
					sender.sendMessage(Prefix.DEFAULT_BAD + BungeeUtils.color(config.getString("ban.playerneverjoin").replace("%player%", args[0])));
					return;
				}
			} else if (targetUUID != null)
				olympaTarget = AccountProvider.getFromDatabase(targetUUID);
			else if (targetname != null)
				olympaTarget = AccountProvider.getFromDatabase(targetname);

			// Si le joueur n'est pas mute
			OlympaSanction alreadymute = MuteUtils.getMute(olympaTarget.getUniqueId());
			if (alreadymute != null && !MuteUtils.chechExpireBan(alreadymute)) {
				// Sinon annuler le ban
				TextComponent msg = BungeeUtils.formatStringToJSON(config.getString("bungee.ban.messages.alreadymute").replaceAll("%player%", olympaTarget.getName()));
				msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, alreadymute.toBaseComplement()));
				msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/banhist " + alreadymute.getId()));
				sender.sendMessage(msg);
				return;
			}
		} catch (SQLException e) {
			sender.sendMessage(Prefix.DEFAULT_BAD + BungeeUtils.color(config.getString("ban.messages.errordb")));
			e.printStackTrace();
			return;
		}
		java.util.regex.Matcher matcher1 = SanctionUtils.matchDuration(args[1]);
		//		java.util.regex.Matcher matcher2 = SanctionUtils.matchUnit(args[1]);
		java.util.regex.Matcher matcher2 = null;
		// Si la command contient un temps et une unité valide
		if (matcher1.find() && matcher2.find()) {
			// Si la command contient un motif
			if (args.length > 2) {
				String reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
				String time = matcher1.group();
				String unit = matcher2.group();
				long timestamp = SanctionUtils.toTimeStamp(Integer.parseInt(time), unit);
				long seconds = timestamp - Utils.getCurrentTimeInSeconds();

				if (olymaPlayer != null && OlympaCorePermissions.STAFF.hasPermission(olympaTarget) && OlympaCorePermissions.BAN_BYPASS_SANCTION_STAFF.hasPermission(olymaPlayer)) {
					sender.sendMessage(config.getString("ban.messages.cantmutestaffmembers"));
					return;
				}

				if (seconds <= config.getInt("ban.settings.minmutetime")) {
					sender.sendMessage(config.getString("ban.messages.cantbypassmaxmutetime"));
					return;
				}

				if (seconds >= config.getInt("ban.settings.maxmutetime")) {
					sender.sendMessage(config.getString("ban.messages.cantbypassmminmutetime"));
					return;
				}

				String Stimestamp = Utils.timestampToDuration(timestamp);
				OlympaSanction mute;
				try {
					mute = SanctionExecuteTarget.add(OlympaSanctionType.MUTE, authorId, olympaTarget.getUniqueId(), reason, timestamp);
				} catch (SQLException e) {
					e.printStackTrace();
					sender.sendMessage(config.getString("bungee.ban.messages.errordb"));
					return;
				}
				MuteUtils.addMute(mute);
				// Si Target est connecté
				if (target != null) {
					// Envoyer un message à tous les joueurs du même serveur spigot
					for (ProxiedPlayer players : target.getServer().getInfo().getPlayers())
						players.sendMessage(config.getString("bungee.ban.messages.tempmuteannounce")
								.replaceAll("%player%", olympaTarget.getName())
								.replaceAll("%time%", Stimestamp)
								.replaceAll("%reason%", reason));
					target.sendMessage(config.getString("bungee.ban.messages.tempmuteannouncetotarget").replaceAll("%time%", Stimestamp).replaceAll("%reason%", reason));

				}
				// Envoye un message à l'auteur
				TextComponent msg = BungeeUtils.formatStringToJSON(config.getString("bungee.ban.messages.tempmuteannouncetoauthor")
						.replaceAll("%player%", olympaTarget.getName())
						.replaceAll("%time%", Stimestamp)
						.replaceAll("%reason%", reason)
						.replaceAll("%author%", BungeeUtils.getName(authorId)));
				msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, mute.toBaseComplement()));
				msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/banhist " + mute.getId()));
				OlympaCorePermissions.BAN_SEEBANMSG.sendMessage(msg);
				ProxyServer.getInstance().getConsole().sendMessage(msg);
			} else
				sender.sendMessage(config.getString("bungee.ban.messages.usagemute"));
		} else {
			sender.sendMessage(config.getString("bungee.ban.messages.usagemute"));
			String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

			OlympaSanction mute;
			try {
				mute = SanctionExecuteTarget.add(OlympaSanctionType.MUTE, authorId, olympaTarget.getUniqueId(), reason, 0);
			} catch (SQLException e) {
				e.printStackTrace();
				sender.sendMessage(config.getString("bungee.ban.messages.errordb"));
				return;
			}
			if (target != null)
				for (ProxiedPlayer players : target.getServer().getInfo().getPlayers())
					players.sendMessage(config.getString("bungee.ban.messages.muteannounce").replaceAll("%player%", olympaTarget.getName()).replaceAll("%reason%", reason));

			TextComponent msg = BungeeUtils
					.formatStringToJSON(config.getString("bungee.ban.messages.muteannouncetoauthor").replaceAll("%player%", olympaTarget.getName()).replaceAll("%reason%", reason));
			msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, mute.toBaseComplement()));
			msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/banhist " + mute.getId()));
			OlympaCorePermissions.BAN_SEEBANMSG.sendMessage(msg);
			ProxyServer.getInstance().getConsole().sendMessage(msg);
		}
	}
}
