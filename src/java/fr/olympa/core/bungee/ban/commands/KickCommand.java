package fr.olympa.core.bungee.ban.commands;

import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Collectors;

import fr.olympa.api.bungee.command.BungeeCommand;
import fr.olympa.api.match.RegexMatcher;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.player.OlympaConsole;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.ban.commands.methods.KickIp;
import fr.olympa.core.bungee.ban.commands.methods.KickPlayer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;

public class KickCommand extends BungeeCommand {

	public KickCommand(OlympaBungee plugin) {
		super(plugin, "kick", OlympaCorePermissions.BAN_KICK_COMMAND, "eject");
		usageString = plugin.getConfig().getString("ban.messages.usagekick");
		minArg = 1;
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		long authorId;
		if (sender instanceof ProxiedPlayer)
			authorId = getOlympaPlayer().getId();
		else
			authorId = OlympaConsole.getId();
		Configuration config = OlympaBungee.getInstance().getConfig();
		if (RegexMatcher.USERNAME.is(args[0]))
			KickPlayer.addKick(authorId, sender, args[0], null, args, olympaPlayer);
		else if (RegexMatcher.FAKE_IP.is(args[0])) {
			if (RegexMatcher.IP.is(args[0]))
				KickIp.addKick(authorId, sender, args[0], args, olympaPlayer);
			else {
				sendMessage(Prefix.DEFAULT_BAD, config.getString("default.ipinvalid").replaceAll("%ip%", args[0]));
				return;
			}
		} else if (RegexMatcher.FAKE_UUID.is(args[0])) {
			if (RegexMatcher.UUID.is(args[0]))
				KickPlayer.addKick(authorId, sender, null, UUID.fromString(args[0]), args, olympaPlayer);
			else {
				sendMessage(Prefix.DEFAULT_BAD, config.getString("default.uuidinvalid").replaceAll("%uuid%", args[0]));
				return;
			}
		} else {
			sendMessage(Prefix.DEFAULT_BAD, config.getString("default.typeunknown").replaceAll("%type%", args[0]));
			return;
		}
	}

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
