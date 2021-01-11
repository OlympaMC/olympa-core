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
import fr.olympa.core.bungee.connectionqueue.QueueHandler;
import fr.olympa.core.bungee.security.SecurityHandler;
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

	@Cmd(permissionName = "BUNGEE_COMMAND_SETTINGS", args = "BOOLEAN")
	public void pingBeforeJoin(CommandContext cmd) {
		boolean b = SecurityHandler.PING_BEFORE_JOIN;
		if (cmd.getArgumentsLength() == 0)
			sendMessage(Prefix.DEFAULT, "Le paramètre &e%&7 du bungee est actuellement à &e%s&7.", "PING_BEFORE_JOIN", b);
		else {
			SecurityHandler.PING_BEFORE_JOIN = cmd.getArgument(0);
			sendMessage(Prefix.DEFAULT_GOOD, "Le paramètre &2%&a du bungee est passé de &2%s&a à &2%s&a.", "PING_BEFORE_JOIN", b, SecurityHandler.PING_BEFORE_JOIN);
		}
	}

	@Cmd(permissionName = "BUNGEE_COMMAND_SETTINGS", args = "BOOLEAN")
	public void allowCrack(CommandContext cmd) {
		boolean b = SecurityHandler.ALLOW_CRACK;
		if (cmd.getArgumentsLength() == 0)
			sendMessage(Prefix.DEFAULT, "Le paramètre &e%&7 du bungee est actuellement à &e%s&7.", "ALLOW_CRACK", b);
		else {
			SecurityHandler.ALLOW_CRACK = cmd.getArgument(0);
			sendMessage(Prefix.DEFAULT_GOOD, "Le paramètre &2%&a du bungee est passé de &2%s&a à &2%s&a.", "ALLOW_CRACK", b, SecurityHandler.ALLOW_CRACK);
		}
	}

	@Cmd(permissionName = "BUNGEE_COMMAND_SETTINGS", args = "BOOLEAN")
	public void allowPremium(CommandContext cmd) {
		boolean b = SecurityHandler.ALLOW_PREMIUM;
		if (cmd.getArgumentsLength() == 0)
			sendMessage(Prefix.DEFAULT, "Le paramètre &e%&7 du bungee est actuellement à &e%s&7.", "ALLOW_PREMIUM", b);
		else {
			SecurityHandler.ALLOW_PREMIUM = cmd.getArgument(0);
			sendMessage(Prefix.DEFAULT_GOOD, "Le paramètre &2%&a du bungee est passé de &2%s&a à &2%s&a.", "ALLOW_PREMIUM", b, SecurityHandler.ALLOW_PREMIUM);
		}
	}

	@Cmd(permissionName = "BUNGEE_COMMAND_SETTINGS", args = "BOOLEAN")
	public void checkCorrectEntredIP(CommandContext cmd) {
		boolean b = SecurityHandler.CHECK_CORRECT_ENTRED_IP;
		if (cmd.getArgumentsLength() == 0)
			sendMessage(Prefix.DEFAULT, "Le paramètre &e%&7 du bungee est actuellement à &e%s&7.", "CHECK_CORRECT_ENTRED_IP", b);
		else {
			SecurityHandler.CHECK_CORRECT_ENTRED_IP = cmd.getArgument(0);
			sendMessage(Prefix.DEFAULT_GOOD, "Le paramètre &2%&a du bungee est passé de &2%s&a à &2%s&a.", "CHECK_CORRECT_ENTRED_IP", b, SecurityHandler.CHECK_CORRECT_ENTRED_IP);
		}
	}

	@Cmd(permissionName = "BUNGEE_COMMAND_SETTINGS", args = "BOOLEAN")
	public void checkVPN(CommandContext cmd) {
		boolean b = SecurityHandler.CHECK_VPN;
		if (cmd.getArgumentsLength() == 0)
			sendMessage(Prefix.DEFAULT, "Le paramètre &e%&7 du bungee est actuellement à &e%s&7.", "CHECK_VPN", b);
		else {
			SecurityHandler.CHECK_VPN = cmd.getArgument(0);
			sendMessage(Prefix.DEFAULT_GOOD, "Le paramètre &2%&a du bungee est passé de &2%s&a à &2%s&a.", "CHECK_VPN", b, SecurityHandler.CHECK_VPN);
		}
	}

	@Cmd(permissionName = "BUNGEE_COMMAND_SETTINGS", args = "BOOLEAN")
	public void checkVPNOnMOTD(CommandContext cmd) {
		boolean b = SecurityHandler.CHECK_VPN_ON_MOTD;
		if (cmd.getArgumentsLength() == 0)
			sendMessage(Prefix.DEFAULT, "Le paramètre &e%&7 du bungee est actuellement à &e%s&7.", "CHECK_VPN_ON_MOTD", b);
		else {
			SecurityHandler.CHECK_VPN_ON_MOTD = cmd.getArgument(0);
			sendMessage(Prefix.DEFAULT_GOOD, "Le paramètre &2%&a du bungee est passé de &2%s&a à &2%s&a.", "CHECK_VPN_ON_MOTD", b, SecurityHandler.CHECK_VPN_ON_MOTD);
		}
	}

	@Cmd(permissionName = "BUNGEE_COMMAND_SETTINGS", args = "INTEGER")
	public void connectionsBeforeStartAntiBot(CommandContext cmd) {
		int i = QueueHandler.NUMBER_BEFORE_START_ANTIBOT;
		if (cmd.getArgumentsLength() == 0)
			sendMessage(Prefix.DEFAULT, "Le paramètre &e%&7 du bungee est actuellement à &e%s&7.", "NUMBER_BEFORE_START_ANTIBOT", i);
		else {
			QueueHandler.NUMBER_BEFORE_START_ANTIBOT = (Integer) cmd.getArgument(0);
			sendMessage(Prefix.DEFAULT_GOOD, "Le paramètre &2%&a du bungee est passé de &2%s&a à &2%s&a.", "NUMBER_BEFORE_START_ANTIBOT", i, QueueHandler.NUMBER_BEFORE_START_ANTIBOT);
		}
	}

	@Cmd(permissionName = "BUNGEE_COMMAND_SETTINGS", args = "INTEGER")
	public void connectionsBeforeCancel(CommandContext cmd) {
		int i = QueueHandler.NUMBER_BEFORE_CANCEL;
		if (cmd.getArgumentsLength() == 0)
			sendMessage(Prefix.DEFAULT, "Le paramètre &e%&7 du bungee est actuellement à &e%s&7.", "NUMBER_BEFORE_CANCEL", i);
		else {
			QueueHandler.NUMBER_BEFORE_CANCEL = (Integer) cmd.getArgument(0);
			sendMessage(Prefix.DEFAULT_GOOD, "Le paramètre &2%&a du bungee est passé de &2%s&a à &2%s&a.", "NUMBER_BEFORE_CANCEL", i, QueueHandler.NUMBER_BEFORE_CANCEL);
		}
	}

	@Cmd(permissionName = "BUNGEE_COMMAND_SETTINGS", args = "INTEGER")
	public void timeBetween2connections(CommandContext cmd) {
		int i = QueueHandler.TIME_BETWEEN_2;
		if (cmd.getArgumentsLength() == 0)
			sendMessage(Prefix.DEFAULT, "Le paramètre &e%&7 du bungee est actuellement à &e%s&7.", "TIME_BETWEEN_2", i);
		else {
			QueueHandler.TIME_BETWEEN_2 = (Integer) cmd.getArgument(0);
			sendMessage(Prefix.DEFAULT_GOOD, "Le paramètre &2%&a du bungee est passé de &2%s&a à &2%s&a.", "TIME_BETWEEN_2", i, QueueHandler.TIME_BETWEEN_2);
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
