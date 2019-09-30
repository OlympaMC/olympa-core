package fr.tristiisch.olympa.core.ban.commands;

import java.util.UUID;
import java.util.stream.Collectors;

import fr.tristiisch.emeraldmc.api.bungee.commands.BungeeCommand;
import fr.tristiisch.emeraldmc.api.bungee.utils.BungeeUtils;
import fr.tristiisch.emeraldmc.api.commons.Matcher;
import fr.tristiisch.emeraldmc.api.commons.Prefix;
import fr.tristiisch.emeraldmc.api.commons.Utils;
import fr.tristiisch.emeraldmc.api.commons.datamanagment.redis.AccountProvider;
import fr.tristiisch.emeraldmc.api.commons.object.EmeraldConsole;
import fr.tristiisch.emeraldmc.api.commons.object.EmeraldGroup;
import fr.tristiisch.olympa.core.ban.BanMySQL;
import fr.tristiisch.olympa.core.ban.objects.EmeraldBan;
import fr.tristiisch.olympa.core.ban.objects.EmeraldBanHistory;
import fr.tristiisch.olympa.core.ban.objects.EmeraldBanStatus;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

@SuppressWarnings("deprecation")
public class DelbanCommand extends BungeeCommand {

	public DelbanCommand(final Plugin plugin) {
		super(plugin, "delban", EmeraldGroup.RESPMODO, "bandel", "dban", "delmute", "mutedel", "delmute", "delkick", "kickdel", "dkick");
		this.minArg = 1;
		this.usageString = "&cUsage &7» &c/delban [id]";
		this.register();
	}

	@Override
	public void onCommand(final CommandSender sender, final String[] args) {
		UUID author;
		if(sender instanceof ProxiedPlayer) {
			author = this.proxiedPlayer.getUniqueId();
		} else {
			author = OlympaConsole.getUniqueId();
		}

		if(Matcher.isInt(args[0])) {
			final int id = Integer.parseInt(args[0]);
			final EmeraldBan ban = BanMySQL.getBanByID(id);
			if(ban != null) {
				if(ban.getStatus().isStatus(EmeraldBanStatus.DELETE)) {
					final TextComponent msg = BungeeUtils.formatStringToJSON(Prefix.DEFAULT_BAD + "Le ban n°&4" + ban.getId() + " &ca déjà été supprimé.");
					msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, ban.toBaseComplement()));
					sender.sendMessage(msg);
					return;
				}
				ban.setStatus(EmeraldBanStatus.DELETE);
				if(BanMySQL.changeCurrentSanction(new EmeraldBanHistory(author, EmeraldBanStatus.DELETE), ban.getId())) {
					final TextComponent msg = BungeeUtils.formatStringToJSON(Prefix.DEFAULT_GOOD + "Le ban n°" + ban.getId() + " a été supprimé.");
					msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, ban.toBaseComplement()));
					for(final ProxiedPlayer player : ProxyServer.getInstance()
							.getPlayers()
							.stream()
							.filter(p -> new AccountProvider(p.getUniqueId()).getEmeraldPlayer().getGroup().isStaffMember())
							.collect(Collectors.toList())) {
						player.sendMessage(msg);
					}
					ProxyServer.getInstance().getConsole().sendMessage(msg);
				} else {
					sender.sendMessage(Utils.color(Prefix.DEFAULT_BAD + "Une erreur avec la base de donnés est survenu."));
				}
			} else {
				sender.sendMessage(Utils.color(Prefix.DEFAULT_BAD + "Le ban n°" + args[0] + " n'existe pas"));
			}

		} else {
			this.sendUsage();
		}
	}
}
