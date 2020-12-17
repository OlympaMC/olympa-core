package fr.olympa.core.bungee.ban.commands;

import java.util.ArrayList;
import java.util.stream.Collectors;

import fr.olympa.api.bungee.command.BungeeCommand;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.ban.execute.SanctionExecute;
import fr.olympa.core.bungee.ban.objects.OlympaSanctionStatus;
import fr.olympa.core.bungee.ban.objects.OlympaSanctionType;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class UnmuteCommand extends BungeeCommand {

	public UnmuteCommand(OlympaBungee plugin) {
		super(plugin, "unmute", OlympaCorePermissions.BAN_UNMUTE_COMMAND, "umute");
		usageString = "<joueur|uuid|id|ip> <motif>";
		minArg = 2;
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		SanctionExecute banArg = SanctionExecute.formatArgs(args);
		banArg.setSanctionType(OlympaSanctionType.MUTE);
		if (sender instanceof ProxiedPlayer)
			banArg.setAuthor(getOlympaPlayer());
		banArg.launchSanction(this, OlympaSanctionStatus.CANCEL);
	}

	//	@Override
	//	public void onCommand(CommandSender sender, String[] args) {
	//		UUID author;
	//		if (sender instanceof ProxiedPlayer)
	//			author = proxiedPlayer.getUniqueId();
	//		else
	//			author = OlympaConsole.getUniqueId();
	//		Configuration config = OlympaBungee.getInstance().getConfig();
	//
	//		if (RegexMatcher.USERNAME.is(args[0]))
	//			UnmutePlayer.unBan(author, sender, null, args[0], args);
	//		else if (RegexMatcher.FAKE_UUID.is(args[0])) {
	//			if (RegexMatcher.UUID.is(args[0]))
	//				UnmutePlayer.unBan(author, sender, UUID.fromString(args[0]), null, args);
	//			else {
	//				this.sendMessage(Prefix.DEFAULT_BAD, config.getString("default.uuidinvalid").replace("%uuid%", args[0]));
	//				return;
	//			}
	//		} else {
	//			this.sendMessage(Prefix.DEFAULT_BAD, config.getString("default.typeunknown").replace("%type%", args[0]));
	//			return;
	//		}
	//	}

	@Override
	public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
		switch (args.length) {
		case 1:
			return Utils.startWords(args[0], ProxyServer.getInstance().getPlayers().stream().map(ProxiedPlayer::getName).collect(Collectors.toList()));
		default:
			return new ArrayList<>();
		}
	}
}
