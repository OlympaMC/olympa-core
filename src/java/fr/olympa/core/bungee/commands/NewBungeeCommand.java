package fr.olympa.core.bungee.commands;

import java.io.IOException;
import java.text.DecimalFormat;

import fr.olympa.api.bungee.command.BungeeComplexCommand;
import fr.olympa.api.bungee.config.BungeeCustomConfig;
import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.machine.MachineUtils;
import fr.olympa.core.bungee.antibot.AntiBotHandler;
import fr.olympa.core.bungee.utils.BungeeUtils;
import net.md_5.bungee.api.plugin.Plugin;

public class NewBungeeCommand extends BungeeComplexCommand {

	public NewBungeeCommand(Plugin plugin) {
		super(plugin, "bungee", "Diverses gestion du serveur bungee.", OlympaCorePermissions.BUNGEE_COMMAND);
	}

	@Cmd(permissionName = "BUNGEE_COMMAND_TPS")
	public void tps(CommandContext cmd) {
		sender.sendMessage(MachineUtils.getInfos());
	}

	@Cmd(permissionName = "BUNGEE_COMMAND_SETTINGS", min = 1, args = "INTEGER")
	public void settings(CommandContext cmd) {
		try {
			BungeeUtils.changeSlots(cmd.getArgument(0));
		} catch (ReflectiveOperationException e) {
			e.printStackTrace();
			sendError(e);
		}
	}

	@Cmd(permissionName = "BUNGEE_COMMAND_MAXPLAYERS", min = 1, args = "INTEGER")
	public void maxplayers(CommandContext cmd) {
		try {
			BungeeUtils.changeSlots(cmd.getArgument(0));
		} catch (ReflectiveOperationException e) {
			e.printStackTrace();
			sendError(e);
		}
	}

	@Cmd(permissionName = "BUNGEE_COMMAND_ANTIBOT", args = "BOOLEAN|toggle")
	public void antibot(CommandContext cmd) {
		if (cmd.getArgumentsLength() == 0)
			sendMessage(Prefix.DEFAULT, "L'antibot est actuellement %s&7.", AntiBotHandler.isEnable() ? "&cActivé" : "&2Désactiver");
		else {
			if (cmd.getArgument(0) instanceof Boolean)
				AntiBotHandler.setEnable(cmd.getArgument(0), sender.getName());
			else
				AntiBotHandler.toggleEnable(sender.getName());
			sendMessage(Prefix.DEFAULT_GOOD, "L'antibot est désormais %s&7.", AntiBotHandler.isEnable() ? "&cActivé" : "&2Désactiver");
		}
	}

	@Cmd(permissionName = "BUNGEE_COMMAND_CONFIGS", min = 2, args = { "CONFIGS", "reload|save" })
	public void configs(CommandContext cmd) {
		BungeeCustomConfig config = BungeeCustomConfig.getConfig(cmd.getArgument(0));
		if (config == null) {
			this.sendMessage(Prefix.DEFAULT_BAD, "Config &4%s&c inconnu.", cmd.getArgument(0));
			return;
		}
		String arg1 = cmd.getArgument(1);
		switch (arg1) {
		case "reload":
			long time = System.nanoTime();
			try {
				config.reload();
				time = System.nanoTime() - time;
				sendMessage(Prefix.DEFAULT_GOOD, "Config &2%s&a chargé en &2%s secondes", config.getName(), new DecimalFormat("0.#").format(time / 1000000000d));
			} catch (IOException e) {
				sendMessage(Prefix.ERROR, "Impossible de charger la config &4%s&c : &4%s&c.", config.getName(), e.getMessage());
				e.printStackTrace();
			}
			break;
		case "save":
			time = System.nanoTime();
			try {
				config.save();
				time = System.nanoTime() - time;
				sendMessage(Prefix.DEFAULT_GOOD, "Config &2%s&a sauvegarder en &2%s secondes", config.getName(), new DecimalFormat("0.#").format(time / 1000000000d));
			} catch (IOException e) {
				sendMessage(Prefix.ERROR, "Impossible de sauvegarder la config &4%s&c sur le disque : &4%s&c.", config.getName(), e.getMessage());
				e.printStackTrace();
			}
			break;
		default:
			sendUsage(cmd.label);
			break;
		}
	}
}
