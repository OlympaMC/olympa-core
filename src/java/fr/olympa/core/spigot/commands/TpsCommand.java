package fr.olympa.core.spigot.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.machine.MachineMessage;
import fr.olympa.api.permission.OlympaCorePermissions;

public class TpsCommand extends OlympaCommand {

	public TpsCommand(Plugin plugin) {
		super(plugin, "tps", "Affiche des informations sur l'état du serveur.", OlympaCorePermissions.SPIGOT_LAG_COMMAND, "lag");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		sendComponents(new MachineMessage(player == null).getInfoMessage().build());
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}

}
