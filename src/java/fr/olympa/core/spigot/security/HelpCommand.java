package fr.olympa.core.spigot.security;

import java.util.List;
import java.util.StringJoiner;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.command.OlympaCommand;

public class HelpCommand extends OlympaCommand {

	public HelpCommand(Plugin plugin) {
		super(plugin, "?", "help");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		/*for (HelpTopic cmdLabel : Bukkit.getServer().getHelpMap().getHelpTopics()) {
			sendMessage(Prefix.NONE, cmdLabel.getName() + " | " + cmdLabel.getShortText() + " | " + cmdLabel.getFullText(sender));
		}*/
		if (args.length == 0) {
			// TODO components click help
			StringJoiner joiner = new StringJoiner("\n");
			joiner.add("§e---------- §6Aide §lOlympa§e ----------");
			for (OlympaCommand command : OlympaCommand.commands) {
				joiner.add("§6/" + command.getCommand() + ": §e" + command.getDescription());
			}
			joiner.add("§e--------------------------------------------");
			sender.sendMessage(joiner.toString());
		}else {
			// TODO command specific help
		}
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}

}
