package fr.olympa.core.bungee.commands;

import java.io.IOException;
import java.sql.SQLException;
import java.text.DecimalFormat;

import fr.olympa.api.bungee.command.BungeeComplexCommand;
import fr.olympa.api.bungee.config.BungeeCustomConfig;
import fr.olympa.api.chat.TableGenerator;
import fr.olympa.api.chat.TableGenerator.Alignment;
import fr.olympa.api.chat.TableGenerator.Receiver;
import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.utils.CacheStats;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.machine.MachineUtils;
import fr.olympa.core.bungee.antibot.AntiBotHandler;
import fr.olympa.core.bungee.connectionqueue.QueueHandler;
import fr.olympa.core.bungee.security.SecurityHandler;
import fr.olympa.core.bungee.utils.BungeeUtils;
import fr.olympa.core.bungee.vpn.OlympaVpn;
import fr.olympa.core.bungee.vpn.VpnHandler;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

public class NewBungeeCommand extends BungeeComplexCommand {

	public NewBungeeCommand(Plugin plugin) {
		super(plugin, "bungee", "Diverses gestion du serveur bungee.", OlympaCorePermissions.BUNGEE_COMMAND, "bung");
		addArgumentParser("CACHE", (sender, arg) -> CacheStats.getCaches().keySet(), x -> CacheStats.getCache(x), x -> "&4%s&c doit être un id de cache qui existe.");
		addArgumentParser("DEBUG_LIST", (sender, arg) -> CacheStats.getDebugLists().keySet(), x -> CacheStats.getDebugList(x), x -> "&4%s&c doit être un id de debugList qui existe.");
		addArgumentParser("DEBUG_MAP", (sender, arg) -> CacheStats.getDebugMaps().keySet(), x -> CacheStats.getDebugMap(x), x -> "&4%s&c doit être un id de debugMap qui existe.");
	}

	@Cmd(permissionName = "BUNGEE_COMMAND_CACHE", args = { "CACHE", "clear|print" })
	public void cache(CommandContext cmd) {
		CacheStats.executeOnCache(this, cmd);
	}

	@Cmd(permissionName = "BUNGEE_COMMAND_CACHE", args = { "DEBUG_LIST", "clear|print" })
	public void list(CommandContext cmd) {
		CacheStats.executeOnList(this, cmd);
	}

	@Cmd(permissionName = "BUNGEE_COMMAND_CACHE", args = { "DEBUG_MAP", "clear|print" })
	public void map(CommandContext cmd) {
		CacheStats.executeOnMap(this, cmd);
	}

	@Cmd(permissionName = "BUNGEE_COMMAND_TPS")
	public void tps(CommandContext cmd) {
		sender.sendMessage(MachineUtils.getInfos(isConsole()));
	}

	@Cmd(permissionName = "BUNGEE_COMMAND_SETTINGS", args = "BOOLEAN")
	public void pingBeforeJoin(CommandContext cmd) {
		boolean b = SecurityHandler.PING_BEFORE_JOIN;
		if (cmd.getArgumentsLength() == 0)
			sendMessage(Prefix.DEFAULT, "Le paramètre &e%s&7 du bungee est actuellement à &e%s&7.", "PING_BEFORE_JOIN", b ? "true" : "false");
		else {
			SecurityHandler.PING_BEFORE_JOIN = cmd.getArgument(0);
			sendMessage(Prefix.DEFAULT_GOOD, "Le paramètre &2%s&a du bungee est passé de &2%s&a à &2%s&a.", "PING_BEFORE_JOIN", b ? "true" : "false", SecurityHandler.PING_BEFORE_JOIN);
		}
	}

	@Cmd(permissionName = "BUNGEE_COMMAND_SETTINGS", args = "BOOLEAN")
	public void allowCrack(CommandContext cmd) {
		boolean b = SecurityHandler.ALLOW_CRACK;
		if (cmd.getArgumentsLength() == 0)
			sendMessage(Prefix.DEFAULT, "Le paramètre &e%s&7 du bungee est actuellement à &e%s&7.", "ALLOW_CRACK", b ? "true" : "false");
		else {
			SecurityHandler.ALLOW_CRACK = cmd.getArgument(0);
			sendMessage(Prefix.DEFAULT_GOOD, "Le paramètre &2%&a du bungee est passé de &2%s&a à &2%s&a.", "ALLOW_CRACK", b ? "true" : "false", SecurityHandler.ALLOW_CRACK);
		}
	}

	@Cmd(permissionName = "BUNGEE_COMMAND_SETTINGS", args = "BOOLEAN")
	public void allowPremium(CommandContext cmd) {
		boolean b = SecurityHandler.ALLOW_PREMIUM;
		if (cmd.getArgumentsLength() == 0)
			sendMessage(Prefix.DEFAULT, "Le paramètre &e%s&7 du bungee est actuellement à &e%s&7.", "ALLOW_PREMIUM", b ? "true" : "false");
		else {
			SecurityHandler.ALLOW_PREMIUM = cmd.getArgument(0);
			sendMessage(Prefix.DEFAULT_GOOD, "Le paramètre &2%s&a du bungee est passé de &2%s&a à &2%s&a.", "ALLOW_PREMIUM", b ? "true" : "false", SecurityHandler.ALLOW_PREMIUM);
		}
	}

	@Cmd(permissionName = "BUNGEE_COMMAND_SETTINGS", args = "BOOLEAN")
	public void checkCorrectIP(CommandContext cmd) {
		boolean b = SecurityHandler.CHECK_CORRECT_ENTRED_IP;
		if (cmd.getArgumentsLength() == 0)
			sendMessage(Prefix.DEFAULT, "Le paramètre &e%s&7 du bungee est actuellement à &e%s&7.\nCela veux dire qu'un vérifie si l'ip est 'play.olympa.fr'.", "CHECK_CORRECT_ENTRED_IP",
					b ? "true" : "false");
		else {
			SecurityHandler.CHECK_CORRECT_ENTRED_IP = cmd.getArgument(0);
			sendMessage(Prefix.DEFAULT_GOOD, "Le paramètre &2%s&a du bungee est passé de &2%s&a à &2%s&a.", "CHECK_CORRECT_ENTRED_IP", b ? "true" : "false", SecurityHandler.CHECK_CORRECT_ENTRED_IP ? "true" : "false");
		}
	}

	@Cmd(permissionName = "BUNGEE_COMMAND_SETTINGS", args = "BOOLEAN")
	public void checkIPNumber(CommandContext cmd) {
		boolean b = SecurityHandler.CHECK_CORRECT_ENTRED_IP_NUMBER;
		if (cmd.getArgumentsLength() == 0)
			sendMessage(Prefix.DEFAULT, "Le paramètre &e%s&7 du bungee est actuellement à &e%s&7.\nCela veux dire qu'on autorise/interdit l'utilisation de l'ip en chiffre (cad 89.234.182.172).", "CHECK_CORRECT_ENTRED_IP_NUMBER",
					b ? "true" : "false");
		else {
			SecurityHandler.CHECK_CORRECT_ENTRED_IP_NUMBER = cmd.getArgument(0);
			sendMessage(Prefix.DEFAULT_GOOD, "Le paramètre &2%s&a du bungee est passé de &2%s&a à &2%s&a.", "CHECK_CORRECT_ENTRED_IP_NUMBER", b ? "true" : "false", SecurityHandler.CHECK_CORRECT_ENTRED_IP_NUMBER ? "true" : "false");
		}
	}

	@Cmd(permissionName = "BUNGEE_COMMAND_SETTINGS", args = "BOOLEAN")
	public void vpnCheck(CommandContext cmd) {
		boolean b = SecurityHandler.CHECK_VPN;
		if (cmd.getArgumentsLength() == 0)
			sendMessage(Prefix.DEFAULT, "Le paramètre &e%s&7 du bungee est actuellement à &e%s&7.\nCela veux dire que lorsqu'un joueur se connecte, on vérifie son IP.", "CHECK_VPN", b ? "true" : "false");
		else {
			SecurityHandler.CHECK_VPN = cmd.getArgument(0);
			sendMessage(Prefix.DEFAULT_GOOD, "Le paramètre &2%s&a du bungee est passé de &2%s&a à &2%s&a.", "CHECK_VPN", b ? "true" : "false", SecurityHandler.CHECK_VPN ? "true" : "false");
		}
	}

	@Cmd(permissionName = "BUNGEE_COMMAND_SETTINGS", args = "BOOLEAN")
	public void vpnCheckOnMOTD(CommandContext cmd) {
		boolean b = SecurityHandler.CHECK_VPN_ON_MOTD;
		if (cmd.getArgumentsLength() == 0)
			sendMessage(Prefix.DEFAULT, "Le paramètre &e%s&7 du bungee est actuellement à &e%s&7.\nCela veux dire que lorsqu'un joueur demande le motd, on vérifie son IP.", "CHECK_VPN_ON_MOTD", b ? "true" : "false");
		else {
			SecurityHandler.CHECK_VPN_ON_MOTD = cmd.getArgument(0);
			sendMessage(Prefix.DEFAULT_GOOD, "Le paramètre &2%s&a du bungee est passé de &2%s&a à &2%s&a.", "CHECK_VPN_ON_MOTD", b ? "true" : "false", SecurityHandler.CHECK_VPN_ON_MOTD ? "true" : "false");
		}
	}

	@Cmd(permissionName = "BUNGEE_COMMAND_SETTINGS", args = { "IP", "playerName" }, min = 2)
	public void vpnAddWhitelist(CommandContext cmd) {
		try {
			OlympaVpn vpnInfo = VpnHandler.get(cmd.getArgument(0));
			if (vpnInfo == null) {
				sendMessage(Prefix.DEFAULT_BAD, "L'IP &4%s&c n'est pas dans la base de données anti-vpn.");

				return;
			}
			vpnInfo.addUserWhitelist(cmd.getArgument(1));
			sendMessage(Prefix.DEFAULT_GOOD, "Le joueur &2%s&a est désormais autorisé à utiliser un VPN avec l'IP &2%s&a.", cmd.getArgument(0), cmd.getArgument(1));

		} catch (SQLException e) {
			sendError(e);
		}
	}

	@Cmd(permissionName = "BUNGEE_COMMAND_VPNINFO", args = { "IP" }, min = 1)
	public void vpnInfo(CommandContext cmd) {
		try {
			OlympaVpn vpnInfo = VpnHandler.get(cmd.getArgument(0));
			if (vpnInfo == null)
				vpnInfo = VpnHandler.createVpnInfo(cmd.getArgument(0));
			TableGenerator table = new TableGenerator(Alignment.LEFT, Alignment.LEFT);
			table.addRow("&ePays &6" + vpnInfo.getCountry(), "&eVille &6" + vpnInfo.getCity());
			table.addRow("&eOrganisation &6" + vpnInfo.getAs(), "&eNom court &6" + vpnInfo.getOrg());
			if (vpnInfo != null)
				table.addRow("&eUsers &6" + String.join(",", vpnInfo.getUsers()));
			if (vpnInfo != null)
				table.addRow("&eWhitelist &6" + String.join(",", vpnInfo.getWhitelistUsers()));
			sendMessage(table.toString(sender instanceof CommandSender ? Receiver.CONSOLE : Receiver.CLIENT));
		} catch (SQLException | IOException e) {
			sendError(e);
		}
	}

	@Cmd(permissionName = "BUNGEE_COMMAND_SETTINGS", args = "INTEGER")
	public void antibotStarter(CommandContext cmd) {
		int i = QueueHandler.NUMBER_BEFORE_START_ANTIBOT;
		if (cmd.getArgumentsLength() == 0)
			sendMessage(Prefix.DEFAULT, "Le paramètre &e%s&7 du bungee est actuellement à &e%d&7.\n" +
					"C'est à dire qu'il faut &4%d&c connections dans la file d'attente pour lancer l'antibot.\n" +
					"Et donc &4%s&7 de file d'attente.", "NUMBER_BEFORE_START_ANTIBOT", i, i, QueueHandler.getStartBotTimeString());
		else {
			QueueHandler.NUMBER_BEFORE_START_ANTIBOT = (Integer) cmd.getArgument(0);
			sendMessage(Prefix.DEFAULT_GOOD, "Le paramètre &2%s&a du bungee est passé de &2%d&a à &2%d&a.", "NUMBER_BEFORE_START_ANTIBOT", i, QueueHandler.NUMBER_BEFORE_START_ANTIBOT);
		}
	}

	@Cmd(permissionName = "BUNGEE_COMMAND_SETTINGS", args = "INTEGER")
	public void antibotCancelConnection(CommandContext cmd) {
		int i = QueueHandler.NUMBER_BEFORE_CANCEL;
		if (cmd.getArgumentsLength() == 0)
			sendMessage(Prefix.DEFAULT, "Le paramètre &e%s&7 du bungee est actuellement à &e%d&7.\n" +
					"C'est à dire qu'il peut y avoir &4%d&c connexions dans la file d'attente MAXIMUM. Les autres connexions seront cancel.\n" +
					"Et donc que le temps d'attente maximum est de &4%s&7.", "NUMBER_BEFORE_CANCEL", i, i, QueueHandler.getMaxQueueTimeString());
		else {
			QueueHandler.NUMBER_BEFORE_CANCEL = (Integer) cmd.getArgument(0);
			sendMessage(Prefix.DEFAULT_GOOD, "Le paramètre &2%s&a du bungee est passé de &2%d&a à &2%d&a.", "NUMBER_BEFORE_CANCEL", i, QueueHandler.NUMBER_BEFORE_CANCEL);
		}
	}

	@Cmd(permissionName = "BUNGEE_COMMAND_SETTINGS", args = "INTEGER")
	public void timeBetween2connections(CommandContext cmd) {
		int i = QueueHandler.TIME_BETWEEN_2;
		if (cmd.getArgumentsLength() == 0)
			sendMessage(Prefix.DEFAULT, "Le paramètre &e%s&7 du bungee est actuellement à &e%d&7.\n" +
					"C'est à dire qu'il a y 1 connexion acceptée toutes les &4%d&7 milisecondes (%1$,.2f secondes).\n" +
					"Et donc qu'il y a &4%d&7 connexion/secondes.", "TIME_BETWEEN_2", i, i, i / 1000, 1000d / QueueHandler.TIME_BETWEEN_2);
		else {
			QueueHandler.TIME_BETWEEN_2 = (Integer) cmd.getArgument(0);
			sendMessage(Prefix.DEFAULT_GOOD, "Le paramètre &2%s&a du bungee est passé de &2%d&a à &2%d&a.", "TIME_BETWEEN_2", i, QueueHandler.TIME_BETWEEN_2);
		}
	}

	@Cmd(permissionName = "BUNGEE_COMMAND_MAXPLAYERS", min = 1, args = "INTEGER")
	public void maxSlots(CommandContext cmd) {
		try {
			@SuppressWarnings("deprecation")
			int playerLimit = ProxyServer.getInstance().getConfig().getPlayerLimit();
			BungeeUtils.changeSlots(cmd.getArgument(0));
			sendMessage(Prefix.DEFAULT_GOOD, "Le paramètre &2%s&a du bungee est passé de &2%d&a à &2%d&a.", "PLAYER_LIMIT", playerLimit, cmd.getArgument(0));
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
			this.sendMessage(Prefix.DEFAULT_BAD, "Config &4%d&7 inconnu.", cmd.getArgument(0));
			return;
		}
		String arg1 = cmd.getArgument(1);
		switch (arg1) {
		case "reload":
			long time = System.nanoTime();
			try {
				config.reload();
				time = System.nanoTime() - time;
				sendMessage(Prefix.DEFAULT_GOOD, "Config &2%s&a chargé en &2%s milisecondes", config.getName(), new DecimalFormat("0,#").format(time / 1000000d));
			} catch (IOException e) {
				sendMessage(Prefix.ERROR, "Impossible de charger la config &4%d&7 : &4%d&7.", config.getName(), e.getMessage());
				e.printStackTrace();
			}
			break;
		case "save":
			time = System.nanoTime();
			try {
				config.save();
				time = System.nanoTime() - time;
				sendMessage(Prefix.DEFAULT_GOOD, "Config &2%s&a sauvegarder en &2%s milisecondes", config.getName(), new DecimalFormat("0,#").format(time / 1000000d));
			} catch (IOException e) {
				sendMessage(Prefix.ERROR, "Impossible de sauvegarder la config &4%d&7 sur le disque : &4%d&7.", config.getName(), e.getMessage());
				e.printStackTrace();
			}
			break;
		default:
			sendUsage(cmd.label);
			break;
		}
	}
}
