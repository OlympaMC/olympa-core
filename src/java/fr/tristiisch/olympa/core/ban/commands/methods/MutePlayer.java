package fr.tristiisch.olympa.core.ban.commands.methods;

import java.util.Arrays;
import java.util.UUID;

import fr.tristiisch.emeraldmc.api.bungee.ban.BanMySQL;
import fr.tristiisch.emeraldmc.api.bungee.ban.BanUtils;
import fr.tristiisch.emeraldmc.api.bungee.ban.MuteUtils;
import fr.tristiisch.emeraldmc.api.bungee.ban.objects.EmeraldBan;
import fr.tristiisch.emeraldmc.api.bungee.ban.objects.EmeraldBanType;
import fr.tristiisch.emeraldmc.api.bungee.utils.BungeeConfigUtils;
import fr.tristiisch.emeraldmc.api.bungee.utils.BungeeUtils;
import fr.tristiisch.emeraldmc.api.commons.Utils;
import fr.tristiisch.emeraldmc.api.commons.datamanagment.redis.AccountProvider;
import fr.tristiisch.emeraldmc.api.commons.datamanagment.sql.MySQL;
import fr.tristiisch.emeraldmc.api.commons.object.EmeraldGroup;
import fr.tristiisch.emeraldmc.api.commons.object.EmeraldPlayer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class MutePlayer {

	@SuppressWarnings("deprecation")
	public static void addMute(final UUID author, final CommandSender sender, final String targetname, final UUID targetUUID, final String[] args, final EmeraldPlayer emeraldPlayer) {
		ProxiedPlayer target = null;
		EmeraldPlayer emeraldTarget = null;
		if(targetUUID != null) {
			target = ProxyServer.getInstance().getPlayer(targetUUID);

		} else if(targetname != null) {
			target = ProxyServer.getInstance().getPlayer(targetname);

		} else {
			throw new NullPointerException("The uuid or name must be specified");
		}

		if(target != null) {
			emeraldTarget = new AccountProvider(target.getUniqueId()).getEmeraldPlayer();
		} else {
			emeraldTarget = MySQL.getPlayer(targetUUID);
			if(emeraldTarget == null) {
				sender.sendMessage(BungeeConfigUtils.getString("bungee.ban.messages.playerneverjoin").replaceAll("%player%", args[0]));
				return;
			}
		}

		// Si le joueur n'est pas mute
		final EmeraldBan alreadymute = MuteUtils.getMute(emeraldTarget.getUniqueId());
		if(alreadymute != null && !MuteUtils.chechExpireBan(alreadymute)) {
			// Sinon annuler le ban
			final TextComponent msg = BungeeUtils.formatStringToJSON(BungeeConfigUtils.getString("bungee.ban.messages.alreadymute").replaceAll("%player%", emeraldTarget.getName()));
			msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, alreadymute.toBaseComplement()));
			msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/banhist " + alreadymute.getId()));
			sender.sendMessage(msg);
			return;
		}
		final java.util.regex.Matcher matcher1 = BanUtils.matchDuration(args[1]);
		final java.util.regex.Matcher matcher2 = BanUtils.matchUnit(args[1]);
		// Si la command contient un temps et une unité valide
		if(matcher1.find() && matcher2.find()) {
			// Si la command contient un motif
			if(args.length > 2) {
				final String reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
				final String time = matcher1.group();
				final String unit = matcher2.group();
				final long timestamp = BanUtils.toTimeStamp(Integer.parseInt(time), unit);
				final long seconds = timestamp - Utils.getCurrentTimeinSeconds();

				if(emeraldPlayer != null && emeraldTarget.getGroup().isStaffMember() && emeraldPlayer.hasPowerLessThan(EmeraldGroup.RESPMODO)) {
					sender.sendMessage(BungeeConfigUtils.getString("bungee.ban.messages.cantmutestaffmembers"));
					return;
				}

				if(seconds <= BungeeConfigUtils.getInt("bungee.ban.settings.minmutetime")) {
					sender.sendMessage(BungeeConfigUtils.getString("bungee.ban.messages.cantbypassmaxmutetime"));
					return;
				}

				if(seconds >= BungeeConfigUtils.getInt("bungee.ban.settings.maxmutetime")) {
					sender.sendMessage(BungeeConfigUtils.getString("bungee.ban.messages.cantbypassmminmutetime"));
					return;
				}

				final String Stimestamp = Utils.timestampToDuration(timestamp);
				final EmeraldBan mute = new EmeraldBan(EmeraldBan.getNextID(), EmeraldBanType.MUTE, emeraldTarget.getUniqueId(), author, reason, Utils.getCurrentTimeinSeconds(), timestamp);
				if(!BanMySQL.addSanction(mute)) {
					sender.sendMessage(BungeeConfigUtils.getString("bungee.ban.messages.errordb"));
					return;
				}
				MuteUtils.addMute(mute);
				// Si Target est connecté
				if(target != null) {
					// Envoyer un message à tous les joueurs du même serveur spigot
					for(final ProxiedPlayer players : target.getServer().getInfo().getPlayers()) {
						players.sendMessage(BungeeConfigUtils.getString("bungee.ban.messages.tempmuteannounce")
								.replaceAll("%player%", emeraldTarget.getName())
								.replaceAll("%time%", Stimestamp)
								.replaceAll("%reason%", reason));
					}
					target.sendMessage(BungeeConfigUtils.getString("bungee.ban.messages.tempmuteannouncetotarget").replaceAll("%time%", Stimestamp).replaceAll("%reason%", reason));

				}
				// Envoye un message à l'auteur
				final TextComponent msg = BungeeUtils.formatStringToJSON(BungeeConfigUtils.getString("bungee.ban.messages.tempmuteannouncetoauthor")
						.replaceAll("%player%", emeraldTarget.getName())
						.replaceAll("%time%", Stimestamp)
						.replaceAll("%reason%", reason)
						.replaceAll("%author%", BungeeUtils.getName(author)));
				msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, mute.toBaseComplement()));
				msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/banhist " + mute.getId()));
				BungeeUtils.sendMessageToStaff(msg);
				ProxyServer.getInstance().getConsole().sendMessage(msg);
			} else {
				sender.sendMessage(BungeeConfigUtils.getString("bungee.ban.messages.usagemute"));
			}
			// Sinon: mute def
		} else {
			sender.sendMessage(BungeeConfigUtils.getString("bungee.ban.messages.usagemute"));
			final String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

			final EmeraldBan mute = new EmeraldBan(EmeraldBan.getNextID(), EmeraldBanType.MUTE, emeraldTarget.getUniqueId(), author, reason, Utils.getCurrentTimeinSeconds(), 0);
			if(!BanMySQL.addSanction(mute)) {
				sender.sendMessage(BungeeConfigUtils.getString("bungee.ban.messages.errordb"));
				return;
			}
			if(target != null) {
				for(final ProxiedPlayer players : target.getServer().getInfo().getPlayers()) {
					players.sendMessage(BungeeConfigUtils.getString("bungee.ban.messages.muteannounce").replaceAll("%player%", emeraldTarget.getName()).replaceAll("%reason%", reason));
				}
			}

			final TextComponent msg = BungeeUtils
					.formatStringToJSON(BungeeConfigUtils.getString("bungee.ban.messages.muteannouncetoauthor").replaceAll("%player%", emeraldTarget.getName()).replaceAll("%reason%", reason));
			msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, mute.toBaseComplement()));
			msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/banhist " + mute.getId()));
			BungeeUtils.sendMessageToStaff(msg);
			ProxyServer.getInstance().getConsole().sendMessage(msg);
		}
	}
}
