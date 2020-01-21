package fr.olympa.core.bungee.privatemessage;

import java.util.Arrays;

import fr.olympa.core.bungee.api.command.BungeeCommand;
import fr.olympa.core.bungee.utils.BungeeConfigUtils;
import fr.olympa.core.bungee.utils.BungeeUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

@SuppressWarnings("deprecation")
public class PrivateMessageCommand extends BungeeCommand {

	public PrivateMessageCommand(Plugin plugin) {
		super(plugin, "msg", "m", "mp", "dm", "w", "whisper", "message", "email", "tell");
		this.minArg = 2;
		this.usageString = "<joueur> <message>";
		PrivateMessage.privateMessageCommand.add(this.command);
		PrivateMessage.privateMessageCommand.addAll(Arrays.asList(this.aliases));
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		if (!(sender instanceof ProxiedPlayer)) {
			sender.sendMessage(BungeeConfigUtils.getString("default.messages.cantconsole"));
			return;
		}
		ProxiedPlayer proxiedPlayer = (ProxiedPlayer) sender;
		ProxiedPlayer targetPlayer = ProxyServer.getInstance().getPlayer(args[0]);
		if (targetPlayer == null || PrivateMessageToggleCommand.players.contains(targetPlayer.getUniqueId())) {
			proxiedPlayer.sendMessage(BungeeUtils.color("&eOlympa &7» &cLe joueur &4%target &cest pas connecté ou pas disponible.".replaceAll("%target", args[0])));
			return;
		}

		if (targetPlayer == proxiedPlayer) {
			proxiedPlayer.sendMessage(BungeeUtils.color("&eOlympa &7» &cTu ne peux pas t'envoyer des messages."));
			return;
		}

		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 1; i < args.length; i++) {
			stringBuilder.append(args[i] + " ");
		}
		String message = stringBuilder.toString();

		PrivateMessage.send(proxiedPlayer, targetPlayer, message);

		PrivateMessage.setReply(proxiedPlayer, targetPlayer);
		PrivateMessage.setReply(targetPlayer, proxiedPlayer);
	}

}
