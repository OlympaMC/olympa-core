package fr.olympa.core.bungee.ban.commands;

import java.util.ArrayList;
import java.util.List;

import fr.olympa.api.bungee.command.BungeeCommand;
import fr.olympa.api.common.permission.OlympaPermission;
import fr.olympa.api.common.sanction.OlympaSanctionType;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.ban.execute.SanctionExecute;
import fr.olympa.core.bungee.ban.objects.OlympaSanctionStatus;
import fr.olympa.core.common.permission.list.OlympaCorePermissionsBungee;
import fr.olympa.core.common.provider.AccountProvider;
import net.md_5.bungee.api.CommandSender;

public class BanIpCommand extends BungeeCommand {

	public static OlympaPermission permToBandef;

	public BanIpCommand(OlympaBungee plugin) {
		super(plugin, "banip", "Permet de bannir ou tempbannir une IP, en utilisant le pseudo", OlympaCorePermissionsBungee.BAN_BANIP_COMMAND, "tempbanip");
		minArg = 2;
		usageString = plugin.getConfig().getString("ban.usageban");
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		SanctionExecute banArg = SanctionExecute.formatArgs(this, args);
		banArg.setSanctionType(OlympaSanctionType.BANIP);
		banArg.launchSanction(OlympaSanctionStatus.ACTIVE);
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, BungeeCommand command, String[] args) {
		switch (args.length) {
		case 1:
			return Utils.startWords(args[0], AccountProvider.getter().getSQL().getNamesBySimilarName(args[0]));
		default:
			return new ArrayList<>();
		}
	}
}
