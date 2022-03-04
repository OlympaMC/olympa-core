package fr.olympa.core.spigot.groups;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.common.permission.OlympaSpigotPermission;
import fr.olympa.api.spigot.command.OlympaCommand;
import fr.olympa.core.spigot.OlympaCore;

public class StaffCommand extends OlympaCommand {

	public StaffCommand(Plugin plugin) {
		super(plugin, "staff", "Affiche une liste des joueurs du staff.", (OlympaSpigotPermission) null);
		isAsynchronous = true;
		allowConsole = false;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		StaffGui staffGui;
		try {
			staffGui = new StaffGui(player);
		} catch (SQLException e) {
			e.printStackTrace();
			sendError(e);
			return false;
		}
		((OlympaCore) plugin).getTask().runTask(() -> staffGui.create(player));
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		return Collections.emptyList();
	}
}
