package fr.olympa.core.bungee.ban.commands;

import fr.olympa.api.bungee.command.BungeeCommand;
import fr.olympa.api.commun.permission.list.OlympaCorePermissionsBungee;
import fr.olympa.api.utils.Prefix;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Plugin;

public class DelbanCommand extends BungeeCommand {

	public DelbanCommand(Plugin plugin) {
		super(plugin, "delban", OlympaCorePermissionsBungee.BAN_DELBAN_COMMAND, "bandel", "dban", "delmute", "mutedel", "delmute", "delkick", "kickdel", "dkick");
		minArg = 1;
		usageString = "&cUsage &7» &c/delban [id]";
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		//		UUID author;
		//		if (sender instanceof ProxiedPlayer)
		//			author = proxiedPlayer.getUniqueId();
		//		else
		//			author = OlympaConsole.getUniqueId();

		sendMessage(Prefix.DEFAULT_BAD, "En dev.");
		//		if (RegexMatcher.INT.is(args[0])) {
		//			int id = Integer.parseInt(args[0]);
		//			OlympaSanction ban = BanMySQL.getSanction(id);
		//			if (ban != null) {
		//				if (ban.getStatus().isStatus(OlympaSanctionStatus.DELETE)) {
		//					TextComponent msg = BungeeUtils.stringToTextConponent(Prefix.DEFAULT_BAD + "Le ban n°&4" + ban.getId() + " &ca déjà été supprimé.");
		//					msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, ban.toBaseComplement()));
		//					sendMessage(msg);
		//					return;
		//				}
		//				ban.setStatus(OlympaSanctionStatus.DELETE);
		//								if (BanMySQL.changeStatus(new OlympaSanctionHistory(author, OlympaSanctionStatus.DELETE), ban.getId())) {
		//									TextComponent msg = BungeeUtils.formatStringToJSON(Prefix.DEFAULT_GOOD + "Le ban n°" + ban.getId() + " a été supprimé.");
		//									msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, ban.toBaseComplement()));
		//									OlympaCorePermissions.BAN_SEEBANMSG.sendMessage(msg);
		//									ProxyServer.getInstance().getConsole().sendMessage(msg);
		//								} else
		//				sendMessage(Prefix.DEFAULT_BAD, "Une erreur avec la base de donnés est survenu.");
		//			} else
		//				sendMessage(Prefix.DEFAULT_BAD, "Le ban n°" + args[0] + " n'existe pas");
		//
		//		} else
		//			sendUsage();
	}
}
