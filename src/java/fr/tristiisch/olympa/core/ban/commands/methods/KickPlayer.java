package fr.tristiisch.olympa.core.ban.commands.methods;

import java.util.Arrays;
import java.util.UUID;

import fr.tristiisch.emeraldmc.api.bungee.ban.BanMySQL;
import fr.tristiisch.emeraldmc.api.bungee.ban.objects.EmeraldBan;
import fr.tristiisch.emeraldmc.api.bungee.ban.objects.EmeraldBanStatus;
import fr.tristiisch.emeraldmc.api.bungee.ban.objects.EmeraldBanType;
import fr.tristiisch.emeraldmc.api.bungee.utils.BungeeConfigUtils;
import fr.tristiisch.emeraldmc.api.bungee.utils.BungeeUtils;
import fr.tristiisch.emeraldmc.api.commons.Utils;
import fr.tristiisch.emeraldmc.api.commons.datamanagment.redis.AccountProvider;
import fr.tristiisch.emeraldmc.api.commons.object.EmeraldGroup;
import fr.tristiisch.emeraldmc.api.commons.object.EmeraldPlayer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class KickPlayer {

	@SuppressWarnings("deprecation")
	public static void addKick(final UUID author, final CommandSender sender, final String targetname, final UUID targetUUID, final String[] args, final EmeraldPlayer emeraldPlayer) {
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
			sender.sendMessage(BungeeConfigUtils.getString("bungee.ban.messages.kicknotconnected").replaceAll("%player%", args[0]));
			return;
		}

		final String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

		if(emeraldPlayer != null && emeraldTarget.getGroup().isStaffMember() && emeraldPlayer.hasPowerLessThan(EmeraldGroup.RESPMODO)) {
			sender.sendMessage(BungeeConfigUtils.getString("bungee.ban.messages.cantkicktaffmembers"));
			return;
		}

		final EmeraldBan kick = new EmeraldBan(EmeraldBan.getNextID(), EmeraldBanType.KICK, emeraldTarget.getUniqueId(), author, reason, Utils.getCurrentTimeinSeconds(), 0, EmeraldBanStatus.EXPIRE);
		if(!BanMySQL.addSanction(kick)) {
			sender.sendMessage(BungeeConfigUtils.getString("bungee.ban.messages.errordb"));
			return;
		}
		// Envoyer un message à tous les joueurs du même serveur spigot
		/*		for(ProxiedPlayer players : target.getServer().getInfo().getPlayers()) {
			players.sendMessage(BungeeConfigUtils.getString("bungee.ban.messages.kickannounce")
					.replaceAll("%player%", emeraldTarget.getName())
					.replaceAll("%reason%", reason)
			);
		};*/

		target.disconnect(
			BungeeUtils.connectScreen(BungeeConfigUtils.getString("bungee.ban.messages.kickdisconnect").replaceAll("%reason%", kick.getReason()).replaceAll("%id%", String.valueOf(kick.getId()))));

		final TextComponent msg = BungeeUtils.formatStringToJSON(BungeeConfigUtils.getString("bungee.ban.messages.kickannouncetoauthor")
				.replaceAll("%player%", emeraldTarget.getName())
				.replaceAll("%reason%", reason)
				.replaceAll("%author%", BungeeUtils.getName(author)));
		msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, kick.toBaseComplement()));
		msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/banhist " + kick.getId()));
		BungeeUtils.sendMessageToStaff(msg);

		ProxyServer.getInstance().getConsole().sendMessage(msg);
	}
}
