package fr.olympa.core.bungee.ban.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fr.olympa.api.bungee.command.BungeeCommand;
import fr.olympa.api.match.RegexMatcher;
import fr.olympa.api.permission.list.OlympaCorePermissionsBungee;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.ban.SanctionUtils;
import fr.olympa.core.bungee.ban.execute.SanctionExecute;
import fr.olympa.core.bungee.ban.objects.OlympaSanctionStatus;
import fr.olympa.core.bungee.ban.objects.OlympaSanctionType;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Plugin;

public class MuteCommand extends BungeeCommand {

	public MuteCommand(Plugin plugin) {
		super(plugin, "mute", OlympaCorePermissionsBungee.BAN_MUTE_COMMAND, "tempmute");
		minArg = 2;
		usageString = "<joueur|uuid|ip> [temps] <motif>";
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		SanctionExecute banArg = SanctionExecute.formatArgs(this, args);
		banArg.setSanctionType(OlympaSanctionType.MUTE);
		banArg.launchSanction(OlympaSanctionStatus.ACTIVE);
	}

	//	@Override
	//	public void onCommand(CommandSender sender, String[] args) {
	//		long authorId;
	//		if (sender instanceof ProxiedPlayer)
	//			authorId = getOlympaPlayer().getId();
	//		else
	//			authorId = OlympaConsole.getId();
	//		Configuration config = OlympaBungee.getInstance().getConfig();
	//		if (RegexMatcher.USERNAME.is(args[0]))
	//			MutePlayer.addMute(authorId, sender, args[0], null, args, olympaPlayer);
	//		else if (RegexMatcher.FAKE_IP.is(args[0])) {
	//			if (RegexMatcher.IP.is(args[0]))
	//				MuteIp.addMute(authorId, sender, args[0], args, olympaPlayer);
	//			else {
	//				this.sendMessage(Prefix.DEFAULT_BAD, config.getString("default.ipinvalid").replace("%ip%", args[0]));
	//				return;
	//			}
	//		} else if (RegexMatcher.UUID.is(args[0])) {
	//			if (RegexMatcher.UUID.is(args[0]))
	//				MutePlayer.addMute(authorId, sender, null, UUID.fromString(args[0]), args, olympaPlayer);
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
			List<String> postentielNames = Utils.startWords(args[0], AccountProvider.getSQL().getNamesBySimilarName(args[0]));
			return postentielNames;
		case 2:
			List<String> units = new ArrayList<>();
			if (args[1].isBlank())
				return Arrays.asList("15min", "30min", "1h", "2h", "3h", "6h", "12h", "1j");
			int time = RegexMatcher.INT.extractAndParse(args[1]);
			for (List<String> unit : SanctionUtils.units)
				for (String u : unit)
					units.add(time + u);
			return Utils.startWords(args[1], units);
		case 3:
			List<String> reasons = Arrays.asList("Insulte", "Provocation", "Spam", "Harcèlement", "Publicité");
			return Utils.startWords(args[2], reasons);
		default:
			return new ArrayList<>();
		}
	}
}
