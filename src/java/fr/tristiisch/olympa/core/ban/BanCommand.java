package fr.tristiisch.olympa.core.ban;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import fr.tristiisch.olympa.api.command.OlympaCommand;
import fr.tristiisch.olympa.api.permission.OlympaPermission;

public abstract class BanCommand extends OlympaCommand {

	public BanCommand(Plugin plugin, String command, OlympaPermission permission, String[] alias) {
		super(plugin, command, permission, alias);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		// TODO Auto-generated method stub
		return null;
	}

}
