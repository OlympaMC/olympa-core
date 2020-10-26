package fr.olympa.core.bungee.ban.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import fr.olympa.api.bungee.command.BungeeCommand;
import fr.olympa.api.match.RegexMatcher;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.player.OlympaConsole;
import fr.olympa.api.sql.MySQL;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.ban.SanctionUtils;
import fr.olympa.core.bungee.ban.commands.methods.MuteIp;
import fr.olympa.core.bungee.ban.commands.methods.MutePlayer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;

public class MuteCommand extends BungeeCommand {

	public MuteCommand(Plugin plugin) {
		super(plugin, "mute", OlympaCorePermissions.BAN_MUTE_COMMAND, "tempmute");
		minArg = 2;
		usageString = "<joueur|uuid|ip> [temps] <motif>";
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		UUID author;
		if (sender instanceof ProxiedPlayer)
			author = proxiedPlayer.getUniqueId();
		else
			author = OlympaConsole.getUniqueId();
		Configuration config = OlympaBungee.getInstance().getConfig();
		if (RegexMatcher.USERNAME.is(args[0]))
			MutePlayer.addMute(author, sender, args[0], null, args, olympaPlayer);
		else if (RegexMatcher.FAKE_IP.is(args[0])) {
			if (RegexMatcher.IP.is(args[0]))
				MuteIp.addMute(author, sender, args[0], args, olympaPlayer);
			else {
				this.sendMessage(Prefix.DEFAULT_BAD, config.getString("default.ipinvalid").replace("%ip%", args[0]));
				return;
			}
		} else if (RegexMatcher.UUID.is(args[0])) {
			if (RegexMatcher.UUID.is(args[0]))
				MutePlayer.addMute(author, sender, null, UUID.fromString(args[0]), args, olympaPlayer);
			else {
				this.sendMessage(Prefix.DEFAULT_BAD, config.getString("default.uuidinvalid").replace("%uuid%", args[0]));
				return;
			}
		} else {
			this.sendMessage(Prefix.DEFAULT_BAD, config.getString("default.typeunknown").replace("%type%", args[0]));
			return;
		}
	}

	@Override
	public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
		switch (args.length) {
		case 1:
			List<String> postentielNames = Utils.startWords(args[0], MySQL.getNamesBySimilarName(args[0]));
			return postentielNames;
		case 2:
			List<String> units = new ArrayList<>();
			for (List<String> unit : SanctionUtils.units)
				for (String u : unit)
					for (int i = 1; i < 20; i++)
						units.add(i + u);
			return Utils.startWords(args[1], units);
		case 3:
			List<String> reasons = Arrays.asList("Insulte", "Provocation", "Spam", "Harcèlement", "Publicité");
			return Utils.startWords(args[2], reasons);
		default:
			return new ArrayList<>();
		}
	}
}
