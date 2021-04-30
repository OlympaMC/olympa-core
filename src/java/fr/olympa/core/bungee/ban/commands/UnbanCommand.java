package fr.olympa.core.bungee.ban.commands;

import java.util.Arrays;
import java.util.List;

import fr.olympa.api.bungee.command.BungeeCommand;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.sql.MySQL;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.ban.execute.SanctionExecute;
import fr.olympa.core.bungee.ban.objects.OlympaSanctionStatus;
import fr.olympa.core.bungee.ban.objects.OlympaSanctionType;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Plugin;

public class UnbanCommand extends BungeeCommand {

	public UnbanCommand(Plugin plugin) {
		super(plugin, "unban", OlympaCorePermissions.BAN_UNBAN_COMMAND, "pardon", "unbann");
		minArg = 2;
		usageString = "<joueur|uuid|ip> <motif>";
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		SanctionExecute banArg = SanctionExecute.formatArgs(this, args);
		banArg.setSanctionType(OlympaSanctionType.BAN);
		banArg.launchSanction(OlympaSanctionStatus.CANCEL);
	}

	//	@Override
	//	public void onCommand(CommandSender sender, String[] args) {
	//		UUID author;
	//		if (sender instanceof ProxiedPlayer)
	//			author = ((Player) sender).getUniqueId();
	//		else
	//			author = OlympaConsole.getUniqueId();
	//
	//		Configuration config = OlympaBungee.getInstance().getConfig();
	//		if (Matcher.isFakeIP(args[0])) {
	//			if (Matcher.isIP(args[0]))
	//				UnbanIp.unBan(author, sender, args[0], args);
	//			else {
	//				sendMessage(Prefix.DEFAULT_BAD, config.getString("default.ipinvalid").replace("%ip%", args[0]));
	//				return;
	//			}
	//
	//		} else if (Matcher.isUsername(args[0]))
	//			UnbanPlayer.unBan(author, sender, null, args[0], args);
	//		else if (Matcher.isFakeUUID(args[0])) {
	//
	//			if (Matcher.isUUID(args[0]))
	//				UnbanPlayer.unBan(author, sender, UUID.fromString(args[0]), null, args);
	//			else {
	//				sendMessage(Prefix.DEFAULT_BAD, config.getString("default.uuidinvalid").replace("%uuid%", args[0]));
	//				return;
	//			}
	//		} else {
	//			sendMessage(Prefix.DEFAULT_BAD, config.getString("default.typeunknown").replace("%type%", args[0]));
	//			return;
	//		}
	//		return;
	//	}

	@Override
	public List<String> onTabComplete(CommandSender sender, BungeeCommand command, String[] args) {
		if (args.length == 1)
			// -> usless code, change to banned players
			return Utils.startWords(args[0], MySQL.getNamesBySimilarName(args[0]));
		else if (args.length == 2) {
			List<String> reasons = Arrays.asList("Demande de déban acceptée", "Erreur", "Tromper de Joueur", "Augmentation de peine", "Réduction de peine");
			return Utils.startWords(args[1], reasons);
		}
		return null;
	}
}
