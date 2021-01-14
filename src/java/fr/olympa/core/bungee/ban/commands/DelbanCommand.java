package fr.olympa.core.bungee.ban.commands;

import java.util.UUID;

import fr.olympa.api.bungee.command.BungeeCommand;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.player.OlympaConsole;
import fr.olympa.api.utils.Matcher;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.bungee.ban.BanMySQL;
import fr.olympa.core.bungee.ban.objects.OlympaSanction;
import fr.olympa.core.bungee.ban.objects.OlympaSanctionStatus;
import fr.olympa.core.bungee.utils.BungeeUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

public class DelbanCommand extends BungeeCommand {

	public DelbanCommand(Plugin plugin) {
		super(plugin, "delban", OlympaCorePermissions.BAN_DELBAN_COMMAND, "bandel", "dban", "delmute", "mutedel", "delmute", "delkick", "kickdel", "dkick");
		minArg = 1;
		usageString = "&cUsage &7» &c/delban [id]";
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		UUID author;
		if (sender instanceof ProxiedPlayer)
			author = proxiedPlayer.getUniqueId();
		else
			author = OlympaConsole.getUniqueId();

		if (Matcher.isInt(args[0])) {
			int id = Integer.parseInt(args[0]);
			OlympaSanction ban = BanMySQL.getSanction(id);
			if (ban != null) {
				if (ban.getStatus().isStatus(OlympaSanctionStatus.DELETE)) {
					TextComponent msg = BungeeUtils.stringToTextConponent(Prefix.DEFAULT_BAD + "Le ban n°&4" + ban.getId() + " &ca déjà été supprimé.");
					msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, ban.toBaseComplement()));
					sendMessage(msg);
					return;
				}
				ban.setStatus(OlympaSanctionStatus.DELETE);
				//				if (BanMySQL.changeCurrentSanction(new OlympaSanctionHistory(author, OlympaSanctionStatus.DELETE), ban.getId())) {
				//					TextComponent msg = BungeeUtils.formatStringToJSON(Prefix.DEFAULT_GOOD + "Le ban n°" + ban.getId() + " a été supprimé.");
				//					msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, ban.toBaseComplement()));
				//					OlympaCorePermissions.BAN_SEEBANMSG.sendMessage(msg);
				//					ProxyServer.getInstance().getConsole().sendMessage(msg);
				//				} else
				//					sendMessage(Prefix.DEFAULT_BAD, "Une erreur avec la base de donnés est survenu.");
			} else
				sendMessage(Prefix.DEFAULT_BAD, "Le ban n°" + args[0] + " n'existe pas");

		} else
			sendUsage();
	}
}
