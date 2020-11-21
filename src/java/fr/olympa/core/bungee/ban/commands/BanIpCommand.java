package fr.olympa.core.bungee.ban.commands;

import java.util.ArrayList;

import fr.olympa.api.bungee.command.BungeeCommand;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.sql.MySQL;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.ban.SanctionUtils;
import fr.olympa.core.bungee.ban.objects.OlympaSanctionType;
import fr.olympa.core.bungee.ban.objects.SanctionExecute;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class BanIpCommand extends BungeeCommand {

	public static OlympaPermission permToBandef;

	public BanIpCommand(OlympaBungee plugin) {
		super(plugin, "banip", OlympaCorePermissions.BAN_BANIP_COMMAND, "tempbanip");
		permToBandef = OlympaCorePermissions.BAN_BANIPDEF_COMMAND;
		minArg = 2;
		usageString = plugin.getConfig().getString("ban.messages.usageban");
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		SanctionExecute banArg = SanctionUtils.formatArgs(sender, args);
		banArg.setSanctionType(OlympaSanctionType.BANIP);
		if (sender instanceof ProxiedPlayer)
			banArg.setAuthor(getOlympaPlayer());
		banArg.execute(this);
	}

	@Override
	public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
		switch (args.length) {
		case 1:
			return Utils.startWords(args[0], MySQL.getNamesBySimilarName(args[0]));
		default:
			return new ArrayList<>();
		}
	}
}
