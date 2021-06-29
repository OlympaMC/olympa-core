package fr.olympa.core.spigot.commands;

import java.util.List;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.common.bash.OlympaRuntime;
import fr.olympa.api.spigot.command.OlympaCommand;
import fr.olympa.core.common.permission.list.OlympaCorePermissionsSpigot;
import fr.olympa.core.spigot.OlympaCore;

public class RestartCommand extends OlympaCommand {

	public RestartCommand(Plugin plugin) {
		super(plugin, "restart", "Redémarre le serveur.", OlympaCorePermissionsSpigot.SERVER_RESTART_COMMAND);
		allowConsole = true;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Bukkit.getOnlinePlayers().forEach(p -> p.kickPlayer("Server is restarting"));
		Consumer<String> function = player != null ? out -> sender.sendMessage(out) : null;
		Runtime.getRuntime().addShutdownHook(OlympaRuntime.action("sh start.sh", function));
		((OlympaCore) plugin).getTask().runTaskLater(() -> plugin.getServer().shutdown(), 40);
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}
}
