package fr.olympa.core.bungee.privatemessage;

import java.util.Arrays;
import java.util.UUID;

import fr.olympa.api.bungee.command.BungeeCommand;
import fr.olympa.api.chat.ColorUtils;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.bungee.utils.BungeeUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

@SuppressWarnings("deprecation")
public class ReplyCommand extends BungeeCommand {

	public ReplyCommand(Plugin plugin) {
		super(plugin, "r", "reply");
		PrivateMessage.replyCommand.add(command);
		PrivateMessage.replyCommand.addAll(Arrays.asList(aliases));
		allowConsole = false;
		description = "Répondre au dernier message privé.";
		minArg = 1;
		usageString = "<message>";
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {

		ProxiedPlayer proxiedPlayer = (ProxiedPlayer) sender;

		UUID targetUUID = PrivateMessage.getReply(proxiedPlayer);
		if (targetUUID == null) {
			sender.sendMessage(Prefix.DEFAULT_GOOD + ColorUtils.color("&cTu n'as personne à qui répondre."));
			return;
		}
		final ProxiedPlayer targetPlayer = ProxyServer.getInstance().getPlayer(targetUUID);
		if (targetPlayer == null) {
			sender.sendMessage(Prefix.DEFAULT_BAD + ColorUtils.color("&4" + BungeeUtils.getName(targetUUID) + "&c n'est plus connecté."));
			return;
		}

		String message = String.join(" ", Arrays.copyOfRange(args, 0, args.length));

		PrivateMessage.send(proxiedPlayer, targetPlayer, message);

	}
}
