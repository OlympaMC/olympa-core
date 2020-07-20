package fr.olympa.core.spigot.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.utils.Prefix;

public class AfkCommand extends OlympaCommand {

	public AfkCommand(Plugin plugin) {
		super(plugin, "afk", "Permet de se mettre AFK");
		allowConsole = false;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		OlympaPlayer olympaPlayer = getOlympaPlayer();
		boolean toggle = !olympaPlayer.isAfk();
		player.setAllowFlight(toggle);
		olympaPlayer.setAfk(toggle);
		if (olympaPlayer.isAfk())
			sendMessage(Prefix.DEFAULT_GOOD, "Tu es désormais &2AFK&a.");
		else
			sendMessage(Prefix.DEFAULT_GOOD, "Tu n'es plus &2AFK&a.");
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}
}
