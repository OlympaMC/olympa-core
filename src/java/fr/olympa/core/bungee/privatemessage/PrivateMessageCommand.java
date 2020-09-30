package fr.olympa.core.bungee.privatemessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import fr.olympa.api.bungee.command.BungeeCommand;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.OlympaBungee;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.TabExecutor;

public class PrivateMessageCommand extends BungeeCommand implements TabExecutor {
	
	public PrivateMessageCommand(Plugin plugin) {
		super(plugin, "msg", "m", "mp", "dm", "w", "whisper", "message", "email", "tell");
		minArg = 2;
		usageString = "<joueur> <message>";
		PrivateMessage.privateMessageCommand.add(command);
		PrivateMessage.privateMessageCommand.addAll(Arrays.asList(aliases));
	}
	
	@Override
	public void onCommand(CommandSender sender, String[] args) {
		if (!(sender instanceof ProxiedPlayer)) {
			sendImpossibleWithConsole();
			return;
		}
		ProxiedPlayer proxiedPlayer = (ProxiedPlayer) sender;
		ProxiedPlayer targetPlayer = ProxyServer.getInstance().getPlayer(args[0]);
		if (targetPlayer == null || PrivateMessageToggleCommand.players.contains(targetPlayer.getUniqueId())) {
			sendMessage(Prefix.DEFAULT_BAD, "Le joueur &4%s&c n'est pas connecté ou pas disponible.".replace("%s", args[0]));
			return;
		}
		
		if (targetPlayer == proxiedPlayer) {
			sendMessage(Prefix.DEFAULT_BAD, "Tu ne peux pas t'envoyer des messages.");
			return;
		}
		
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 1; i < args.length; i++)
			stringBuilder.append(args[i] + " ");
		String message = stringBuilder.toString();
		
		PrivateMessage.send(proxiedPlayer, targetPlayer, message);
		
		PrivateMessage.setReply(proxiedPlayer, targetPlayer);
		PrivateMessage.setReply(targetPlayer, proxiedPlayer);
	}
	
	@Override
	public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
		Set<String> players = OlympaBungee.getInstance().getProxy().getPlayers().stream().map(ProxiedPlayer::getName).collect(Collectors.toSet());
		if (args.length == 0)
			return players;
		else if (args.length == 1)
			return Utils.startWords(args[0], players);
		return new ArrayList<>();
	}
}
