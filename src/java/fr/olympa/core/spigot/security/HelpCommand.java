package fr.olympa.core.spigot.security;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.common.chat.TxtComponentBuilder;
import fr.olympa.api.common.command.Paginator;
import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.spigot.command.OlympaCommand;
import fr.olympa.api.utils.Prefix;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class HelpCommand extends OlympaCommand {

	private Paginator<OlympaCommand> paginator;

	public HelpCommand(Plugin plugin) {
		super(plugin, "help", "?");
		super.description = "Affiche l'aide.";
		paginator = new Paginator<>(10, "Aide §lOlympa") {

			@Override
			protected List<OlympaCommand> getObjects() {
				return OlympaCommand.commands;
			}

			@Override
			protected BaseComponent getObjectDescription(OlympaCommand object) {
				return TxtComponentBuilder.of(Prefix.NONE, "§7➤ §6/" + object.getCommand() + (object.getDescription() == null ? "" : ": §e" + object.getDescription()),
						ClickEvent.Action.RUN_COMMAND, "/help " + object.getCommand(), HoverEvent.Action.SHOW_TEXT, new Text("§bClique pour afficher l'aide de cette commande !"));
			}

			@Override
			protected String getCommand(int page) {
				return "/help " + page;
			}
		};
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		OlympaPlayer olympaPlayer = null;
		if (player != null)
			olympaPlayer = getOlympaPlayer();
		int page = 1;
		if (args.length > 0)
			try {
				page = Integer.parseInt(args[0]);
				if (page < 1) {
					sendError("La page %d n'existe pas.", page);
					return false;
				}
			} catch (NumberFormatException e) {
				for (OlympaCommand command : OlympaCommand.commands)
					if (command.getAllCommands().contains(args[0])) {
						if (player == null || command.hasPermission(olympaPlayer))
							command.sendHelp(sender);
						else
							sendError("Tu n'as pas la permission pour afficher l'aide de cette commande.");
						return false;
					}
				sendError("La commande %s n'existe pas.", args[0]);
				return false;
			}

		sendComponents(paginator.getPage(page));
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		List<String> commands = new ArrayList<>();
		for (OlympaCommand command : OlympaCommand.commands) {
			if (!command.hasPermission(sender))
				continue;
			commands.addAll(command.getAllCommands());
		}
		return commands;
	}

}
