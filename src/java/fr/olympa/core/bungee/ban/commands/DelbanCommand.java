package fr.olympa.core.bungee.ban.commands;

import java.util.UUID;

import fr.olympa.api.objects.OlympaConsole;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.utils.Matcher;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.SpigotUtils;
import fr.olympa.core.bungee.api.command.BungeeCommand;
import fr.olympa.core.bungee.ban.BanMySQL;
import fr.olympa.core.bungee.ban.objects.OlympaSanction;
import fr.olympa.core.bungee.ban.objects.OlympaSanctionHistory;
import fr.olympa.core.bungee.ban.objects.OlympaSanctionStatus;
import fr.olympa.core.bungee.utils.BungeeUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

@SuppressWarnings("deprecation")
public class DelbanCommand extends BungeeCommand {

	public DelbanCommand(Plugin plugin) {
		super(plugin, "delban", OlympaCorePermissions.BAN_DELBAN_COMMAND, "bandel", "dban", "delmute", "mutedel", "delmute", "delkick", "kickdel", "dkick");
		this.minArg = 1;
		this.usageString = "&cUsage &7» &c/delban [id]";
		this.register();
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		UUID author;
		if (sender instanceof ProxiedPlayer) {
			author = this.proxiedPlayer.getUniqueId();
		} else {
			author = OlympaConsole.getUniqueId();
		}

		if (Matcher.isInt(args[0])) {
			int id = Integer.parseInt(args[0]);
			OlympaSanction ban = BanMySQL.getSanction(id);
			if (ban != null) {
				if (ban.getStatus().isStatus(OlympaSanctionStatus.DELETE)) {
					TextComponent msg = BungeeUtils.formatStringToJSON(Prefix.DEFAULT_BAD + "Le ban n°&4" + ban.getId() + " &ca déjà été supprimé.");
					msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, ban.toBaseComplement()));
					sender.sendMessage(msg);
					return;
				}
				ban.setStatus(OlympaSanctionStatus.DELETE);
				if (BanMySQL.changeCurrentSanction(new OlympaSanctionHistory(author, OlympaSanctionStatus.DELETE), ban.getId())) {
					TextComponent msg = BungeeUtils.formatStringToJSON(Prefix.DEFAULT_GOOD + "Le ban n°" + ban.getId() + " a été supprimé.");
					msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, ban.toBaseComplement()));
					OlympaCorePermissions.BAN_SEEBANMSG.sendMessage(msg);
					ProxyServer.getInstance().getConsole().sendMessage(msg);
				} else {
					sender.sendMessage(SpigotUtils.color(Prefix.DEFAULT_BAD + "Une erreur avec la base de donnés est survenu."));
				}
			} else {
				sender.sendMessage(SpigotUtils.color(Prefix.DEFAULT_BAD + "Le ban n°" + args[0] + " n'existe pas"));
			}

		} else {
			this.sendUsage();
		}
	}
}
