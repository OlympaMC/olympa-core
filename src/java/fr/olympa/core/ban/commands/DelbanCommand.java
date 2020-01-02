package fr.olympa.core.ban.commands;

import java.util.UUID;
import java.util.stream.Collectors;

import fr.olympa.core.ban.BanMySQL;
import fr.olympa.core.ban.objects.OlympaSanction;
import fr.olympa.core.ban.objects.OlympaSanctionHistory;
import fr.olympa.core.ban.objects.OlympaSanctionStatus;
import fr.tristiisch.emeraldmc.api.bungee.commands.BungeeCommand;
import fr.tristiisch.emeraldmc.api.bungee.utils.BungeeUtils;
import fr.tristiisch.emeraldmc.api.commons.Matcher;
import fr.tristiisch.emeraldmc.api.commons.Prefix;
import fr.tristiisch.emeraldmc.api.commons.Utils;
import fr.tristiisch.emeraldmc.api.commons.datamanagment.redis.AccountProvider;
import fr.tristiisch.emeraldmc.api.commons.object.EmeraldConsole;
import fr.tristiisch.emeraldmc.api.commons.object.EmeraldGroup;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

@SuppressWarnings("deprecation")
public class DelbanCommand extends BungeeCommand {

	public DelbanCommand(Plugin plugin) {
		super(plugin, "delban", EmeraldGroup.RESPMODO, "bandel", "dban", "delmute", "mutedel", "delmute", "delkick", "kickdel", "dkick");
		this.minArg = 1;
		this.usageString = "&cUsage &7» &c/delban [id]";
		this.register();
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		UUID author;
		if(sender instanceof ProxiedPlayer) {
			author = this.proxiedPlayer.getUniqueId();
		} else {
			author = OlympaConsole.getUniqueId();
		}

		if(Matcher.isInt(args[0])) {
			int id = Integer.parseInt(args[0]);
			OlympaSanction ban = BanMySQL.getSanction(id);
			if(ban != null) {
				if(ban.getStatus().isStatus(OlympaSanctionStatus.DELETE)) {
					TextComponent msg = BungeeUtils.formatStringToJSON(Prefix.DEFAULT_BAD + "Le ban n°&4" + ban.getId() + " &ca déjà été supprimé.");
					msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, ban.toBaseComplement()));
					sender.sendMessage(msg);
					return;
				}
				ban.setStatus(OlympaSanctionStatus.DELETE);
				if(BanMySQL.changeCurrentSanction(new OlympaSanctionHistory(author, OlympaSanctionStatus.DELETE), ban.getId())) {
					TextComponent msg = BungeeUtils.formatStringToJSON(Prefix.DEFAULT_GOOD + "Le ban n°" + ban.getId() + " a été supprimé.");
					msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, ban.toBaseComplement()));
					for(ProxiedPlayer player : ProxyServer.getInstance()
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
