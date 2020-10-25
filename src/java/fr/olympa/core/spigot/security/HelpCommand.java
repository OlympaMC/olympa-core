package fr.olympa.core.spigot.security;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.player.OlympaPlayer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class HelpCommand extends OlympaCommand {

	public HelpCommand(Plugin plugin) {
		super(plugin, "help", "?");
		super.description = "Affiche l'aide.";
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		OlympaPlayer olympaPlayer = getOlympaPlayer();
		if (args.length == 0) {
			TextComponent compo = new TextComponent(TextComponent.fromLegacyText("§e---------- §6Aide §lOlympa§e ----------"));
			for (OlympaCommand command : OlympaCommand.commands) {
				if (!command.hasPermission(olympaPlayer)) continue;
				TextComponent commandCompo = new TextComponent(TextComponent.fromLegacyText("\n§7➤ §6/" + command.getCommand() + (command.getDescription() == null ? "" : ": §e" + command.getDescription())));
				commandCompo.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§bClique pour afficher l'aide de cette commande !")));
				commandCompo.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/help " + command.getCommand()));
				compo.addExtra(commandCompo);
			}
			compo.addExtra("\n§e--------------------------------------------");
			sender.spigot().sendMessage(compo);
		}else {
			for (OlympaCommand command : OlympaCommand.commands) {
				if (command.getAllCommands().contains(args[0]) && command.hasPermission(olympaPlayer)) {
					command.sendHelp(sender);
					break;
				}
			}
		}
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		List<String> commands = new ArrayList<>();
		for (OlympaCommand command : OlympaCommand.commands) {
			if (!command.hasPermission(sender)) continue;
			commands.addAll(command.getAllCommands());
		}
		return commands;
	}

}
