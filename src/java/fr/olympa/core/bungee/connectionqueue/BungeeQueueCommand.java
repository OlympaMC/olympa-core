package fr.olympa.core.bungee.connectionqueue;

import java.text.DecimalFormat;

import fr.olympa.api.bungee.command.BungeeCommand;
import fr.olympa.api.common.match.RegexMatcher;
import fr.olympa.api.common.permission.list.OlympaCorePermissionsBungee;
import fr.olympa.api.utils.Prefix;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeeQueueCommand extends BungeeCommand {

	public BungeeQueueCommand(Plugin plugin) {
		super(plugin, "bungeequeue", OlympaCorePermissionsBungee.BUNGEE_QUEUE_COMMAND, "bqueue");
		minArg = 1;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onCommand(CommandSender sender, String[] args) {
		int number;
		try {
			number = (int) RegexMatcher.NUMBER.parse(args[0]);
		} catch (IllegalArgumentException e) {
			sendError(args[0] + " doit être un nombre valide.");
			return;
		}
		int oldNumber = Integer.valueOf(QueueHandler.TIME_BETWEEN_2);
		QueueHandler.TIME_BETWEEN_2 = number;
		sender.sendMessage(Prefix.DEFAULT_GOOD.formatMessage(
				"Le temps de queue par joueur est passé de &2%s&a à &e%s&a. C'est à dire %s joueurs par secondes.", oldNumber, number, new DecimalFormat("0.#").format(number * 60)));
	}

}