package fr.olympa.core.spigot.vanish;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.common.permission.list.OlympaAPIPermissionsSpigot;
import fr.olympa.api.spigot.command.OlympaCommand;
import fr.olympa.api.spigot.vanish.IVanishApi;
import fr.olympa.api.utils.Prefix;

public class VanishCommand extends OlympaCommand {

	private IVanishApi vanishApi;

	public VanishCommand(Plugin plugin, IVanishApi vanishApi) {
		super(plugin, "vanish", "Permet de se mettre en Vanish.", OlympaAPIPermissionsSpigot.VANISH_COMMAND, "v");
		allowConsole = false;
		this.vanishApi = vanishApi;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (vanishApi == null)
			sendMessage(Prefix.DEFAULT_BAD, "Le module de Vanish est désactivé, commande impossible.");
		else if (vanishApi.isVanished(player))
			vanishApi.disable(getOlympaPlayer(), true);
		else
			vanishApi.enable(getOlympaPlayer(), true);
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}
}
