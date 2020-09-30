package fr.olympa.core.spigot.afk;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.core.spigot.OlympaCore;

public class AfkCommand extends OlympaCommand {

	public AfkCommand(Plugin plugin) {
		super(plugin, "afk", "Permet de se mettre AFK", null, new String[] {});
		allowConsole = false;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		AfkPlayer afkPlayer = AfkHandler.get(player);
		AfkPlayerToggleEvent event = new AfkPlayerToggleEvent(player, afkPlayer);
		OlympaCore.getInstance().getServer().getPluginManager().callEvent(event);
		AfkHandler.updateLastAction(player, !event.getAfkPlayer().isAfk(), "command");
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}
}
