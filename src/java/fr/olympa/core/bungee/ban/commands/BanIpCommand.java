package fr.olympa.core.bungee.ban.commands;

import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.api.command.BungeeCommand;
import fr.olympa.core.bungee.ban.SanctionUtils;
import fr.olympa.core.bungee.ban.objects.BanExecute;
import fr.olympa.core.bungee.ban.objects.OlympaSanctionType;
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
		BanExecute banArg = SanctionUtils.formatArgs(args);
		banArg.setSanctionType(OlympaSanctionType.BANIP);
		if (sender instanceof ProxiedPlayer)
			banArg.setAuthor((ProxiedPlayer) sender);
		banArg.execute();
	}
}
