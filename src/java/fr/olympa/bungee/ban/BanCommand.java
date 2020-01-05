package fr.olympa.bungee.ban;

import java.util.List;

import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.bungee.api.command.BungeeCommand;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Plugin;

public abstract class BanCommand extends BungeeCommand {

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
