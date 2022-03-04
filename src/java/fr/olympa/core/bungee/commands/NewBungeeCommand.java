package fr.olympa.core.bungee.commands;

import java.io.IOException;
import java.sql.SQLException;
import java.text.DecimalFormat;

import fr.olympa.api.bungee.command.BungeeComplexCommand;
import fr.olympa.api.bungee.config.BungeeCustomConfig;
import fr.olympa.api.bungee.utils.BungeeUtils;
import fr.olympa.api.common.chat.TableGenerator;
import fr.olympa.api.common.chat.TableGenerator.Alignment;
import fr.olympa.api.common.chat.TableGenerator.Receiver;
import fr.olympa.api.common.command.complex.Cmd;
import fr.olympa.api.common.command.complex.CommandContext;
import fr.olympa.api.common.machine.TpsMessageProvider;
import fr.olympa.api.common.module.OlympaModule;
import fr.olympa.api.utils.CacheStats;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.bungee.antibot.AntiBotHandler;
import fr.olympa.core.bungee.connectionqueue.QueueHandler;
import fr.olympa.core.bungee.security.SecurityHandler;
import fr.olympa.core.bungee.utils.SpigotPlayerPack;
import fr.olympa.core.bungee.vpn.OlympaVpn;
import fr.olympa.core.bungee.vpn.VpnHandler;
import fr.olympa.core.common.permission.list.OlympaCorePermissionsBungee;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

public class NewBungeeCommand extends BungeeComplexCommand {

	public NewBungeeCommand(Plugin plugin) {
		super(plugin, "bungee", "Diverses gestion du serveur bungee.", OlympaCorePermissionsBungee.BUNGEE_COMMAND, "bung");
		addArgumentParser("CACHE", (sender, arg) -> CacheStats.getCaches().keySet(), x -> CacheStats.getCache(x), x -> "&4%s&c doit être un id de cache qui existe.");
		addArgumentParser("DEBUG_LIST", (sender, arg) -> CacheStats.getDebugLists().keySet(), x -> CacheStats.getDebugList(x), x -> "&4%s&c doit être un id de debugList qui existe.");
		addArgumentParser("DEBUG_MAP", (sender, arg) -> CacheStats.getDebugMaps().keySet(), x -> CacheStats.getDebugMap(x), x -> "&4%s&c doit être un id de debugMap qui existe.");
	}

	@Cmd(args = "on|off", min = 1)
	public void debugModule(CommandContext cmd) {
		String arg0 = cmd.getArgument(0);
		Boolean toOn;
		if (arg0.equalsIgnoreCase("on"))
			toOn = true;
		else if (arg0.equalsIgnoreCase("off"))
			toOn = false;
		else
			toOn = null;
		if (toOn == null)
			sendMessage(Prefix.DEFAULT, "Le module &8%s&7 est %s", "de debug", OlympaModule.DEBUG ? "&2Activé" : "&4Désativé");
		else if (toOn) {
			OlympaModule.DEBUG = true;
			sendMessage(Prefix.DEFAULT_GOOD, "Le module %s est désormais %s", "de debug", OlympaModule.DEBUG ? "&2Activé" : "&4Désativé");
		} else {
			OlympaModule.DEBUG = false;
			sendMessage(Prefix.DEFAULT_BAD, "Le module %s est désormais %s", "de debug", OlympaModule.DEBUG ? "&2Activé" : "&4Désativé");
		}
	}

	@Cmd(permissionName = "BUNGEE_COMMAND_CACHE", args = { "CACHE", "clear|print|stats" })
	public void cache(CommandContext cmd) {
		CacheStats.executeOnCache(this, cmd);
	}

	@Cmd(permissionName = "BUNGEE_COMMAND_CACHE", args = { "DEBUG_LIST", "clear|print" })
	public void list(CommandContext cmd) {
		CacheStats.executeOnList(this, cmd);
	}

	@Cmd(permissionName = "BUNGEE_COMMAND_CACHE", args = { "DEBUG_MAP", "clear|print|remove" })
	public void map(CommandContext cmd) {
		CacheStats.executeOnMap(this, cmd);
	}

	@Cmd(permissionName = "BUNGEE_COMMAND_TPS")
	public void tps(CommandContext cmd) {
		sender.sendMessage(new TpsMessageProvider(getOlympaPlayer()).getInfoMessage().build());
	}

	@Cmd(permissionName = "BUNGEE_COMMAND_SETTINGS", args = "BOOLEAN")
	public void pingBeforeJoin(CommandContext cmd) {
		boolean old = SecurityHandler.getInstance().pingBeforeJoin;
		if (cmd.getArgumentsLength() == 0)
			sendMessage(Prefix.DEFAULT, "Le paramètre &e%s&7 du bungee est actuellement à &e%s&7.", "PING_BEFORE_JOIN", old);
		else {
			SecurityHandler.getInstance().pingBeforeJoin = cmd.getArgument(0);
			sendMessage(Prefix.DEFAULT_GOOD, "Le paramètre &2%s&a du bungee est passé de &2%s&a à &2%s&a.", "PING_BEFORE_JOIN", old, SecurityHandler.getInstance().pingBeforeJoin);
		}
	}

	@Cmd(permissionName = "BUNGEE_COMMAND_SETTINGS", args = "BOOLEAN")
	public void allowCrack(CommandContext cmd) {
		boolean old = SecurityHandler.getInstance().allowCrack;
		if (cmd.getArgumentsLength() == 0)
			sendMessage(Prefix.DEFAULT, "Le paramètre &e%s&7 du bungee est actuellement à &e%s&7.", "ALLOW_CRACK", old ? "true" : "false");
		else {
			SecurityHandler.getInstance().allowCrack = cmd.getArgument(0);
			sendMessage(Prefix.DEFAULT_GOOD, "Le paramètre &2%&a du bungee est passé de &2%s&a à &2%s&a.", "ALLOW_CRACK", old, SecurityHandler.getInstance().allowCrack);
		}
	}

	@Cmd(permissionName = "BUNGEE_COMMAND_SETTINGS", args = "BOOLEAN")
	public void allowPremium(CommandContext cmd) {
		boolean old = SecurityHandler.getInstance().allowPremium;
		if (cmd.getArgumentsLength() == 0)
			sendMessage(Prefix.DEFAULT, "Le paramètre &e%s&7 du bungee est actuellement à &e%s&7.", "ALLOW_PREMIUM", old);
		else {
			SecurityHandler.getInstance().allowPremium = cmd.getArgument(0);
			sendMessage(Prefix.DEFAULT_GOOD, "Le paramètre &2%s&a du bungee est passé de &2%s&a à &2%s&a.", "ALLOW_PREMIUM", old, SecurityHandler.getInstance().allowPremium);
		}
	}

	@Cmd(permissionName = "BUNGEE_COMMAND_SETTINGS", args = "BOOLEAN")
	public void allowQueue(CommandContext cmd) {
		boolean old = QueueHandler.ENABLED;
		if (cmd.getArgumentsLength() == 0)
			sendMessage(Prefix.DEFAULT, "Le paramètre &e%s&7 du bungee est actuellement à &e%s&7.", "QUEUE", old);
		else {
			QueueHandler.ENABLED = cmd.getArgument(0);
			sendMessage(Prefix.DEFAULT_GOOD, "Le paramètre &2%s&a du bungee est passé de &2%s&a à &2%s&a.", "QUEUE", old, QueueHandler.ENABLED);
		}
	}

	@Cmd(permissionName = "BUNGEE_COMMAND_SETTINGS", args = "BOOLEAN")
	public void checkCorrectIP(CommandContext cmd) {
		boolean old = SecurityHandler.getInstance().checkCorrectEntredIp;
		if (cmd.getArgumentsLength() == 0)
			sendMessage(Prefix.DEFAULT, "Le paramètre &e%s&7 du bungee est actuellement à &e%s&7.\nCela veux dire qu'un vérifie si l'ip est 'play.olympa.fr'.", "CHECK_CORRECT_ENTRED_IP", old);
		else {
			SecurityHandler.getInstance().checkCorrectEntredIp = cmd.getArgument(0);
			sendMessage(Prefix.DEFAULT_GOOD, "Le paramètre &2%s&a du bungee est passé de &2%s&a à &2%s&a.", "CHECK_CORRECT_ENTRED_IP", old, SecurityHandler.getInstance().checkCorrectEntredIp);
		}
	}

	@Cmd(permissionName = "BUNGEE_COMMAND_SETTINGS", args = "BOOLEAN")
	public void checkIPNumber(CommandContext cmd) {
		boolean b = SecurityHandler.getInstance().checkCorrectEntredIpNumber;
		if (cmd.getArgumentsLength() == 0)
			sendMessage(Prefix.DEFAULT, "Le paramètre &e%s&7 du bungee est actuellement à &e%s&7.\nCela veux dire qu'on autorise/interdit l'utilisation de l'ip en chiffre (cad 89.234.182.172).",
					"CHECK_CORRECT_ENTRED_IP_NUMBER", b);
		else {
			SecurityHandler.getInstance().checkCorrectEntredIpNumber = cmd.getArgument(0);
			sendMessage(Prefix.DEFAULT_GOOD, "Le paramètre &2%s&a du bungee est passé de &2%s&a à &2%s&a.", "CHECK_CORRECT_ENTRED_IP_NUMBER", b, SecurityHandler.getInstance().checkCorrectEntredIpNumber);
		}
	}

	@Cmd(permissionName = "BUNGEE_COMMAND_SETTINGS", args = "BOOLEAN")
	public void vpnCheck(CommandContext cmd) {
		boolean old = SecurityHandler.getInstance().checkVpn;
		if (cmd.getArgumentsLength() == 0)
			sendMessage(Prefix.DEFAULT, "Le paramètre &e%s&7 du bungee est actuellement à &e%s&7.\nCela veux dire que lorsqu'un joueur se connecte, on vérifie son IP.", "CHECK_VPN", old);
		else {
			SecurityHandler.getInstance().checkVpn = cmd.getArgument(0);
			sendMessage(Prefix.DEFAULT_GOOD, "Le paramètre &2%s&a du bungee est passé de &2%s&a à &2%s&a.", "CHECK_VPN", old, SecurityHandler.getInstance().checkVpn);
		}
	}

	@Cmd(permissionName = "BUNGEE_COMMAND_SETTINGS", args = "BOOLEAN")
	public void vpnCheckOnMOTD(CommandContext cmd) {
		boolean old = SecurityHandler.getInstance().checkVpnOnMotd;
		if (cmd.getArgumentsLength() == 0)
			sendMessage(Prefix.DEFAULT, "Le paramètre &e%s&7 du bungee est actuellement à &e%s&7.\nCela veux dire que lorsqu'un joueur demande le motd, on vérifie son IP.", "CHECK_VPN_ON_MOTD", old);
		else {
			SecurityHandler.getInstance().checkVpnOnMotd = cmd.getArgument(0);
			sendMessage(Prefix.DEFAULT_GOOD, "Le paramètre &2%s&a du bungee est passé de &2%s&a à &2%s&a.", "CHECK_VPN_ON_MOTD", old, SecurityHandler.getInstance().checkVpnOnMotd);
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
			table.setReceiver(sender instanceof CommandSender ? Receiver.CONSOLE : Receiver.CLIENT);
			table.addRow("&ePays &6" + vpnInfo.getCountry(), "&eVille &6" + vpnInfo.getCity());
			table.addRow("&eOrganisation &6" + vpnInfo.getAs(), "&eNom court &6" + vpnInfo.getOrg());
			if (vpnInfo != null && vpnInfo.getUsers() != null)
				table.addRow("&eUsers &6" + String.join(",", vpnInfo.getUsers()));
			if (vpnInfo != null)
				table.addRow("&eWhitelist &6" + String.join(",", vpnInfo.getWhitelistUsers()));
			sendMessage(table.toString());
		} catch (SQLException | IOException e) {
			sendError(e);
		}
	}

	@Cmd(permissionName = "BUNGEE_COMMAND_SETTINGS", args = "INTEGER")
	public void antibotStarter(CommandContext cmd) {
		int i = QueueHandler.NUMBER_BEFORE_START_ANTIBOT;
		if (cmd.getArgumentsLength() == 0)
			sendMessage(Prefix.DEFAULT, "Le paramètre &e%s&7 du bungee est actuellement à &e%d&7.\n&c" +
					"C'est à dire qu'il faut &4%d& connections dans la file d'attente pour lancer l'antibot.\n" +
					"Et donc &4%s&c de file d'attente.", "NUMBER_BEFORE_START_ANTIBOT", i, i, QueueHandler.getStartBotTimeString());
		else {
			QueueHandler.NUMBER_BEFORE_START_ANTIBOT = (Integer) cmd.getArgument(0);
			sendMessage(Prefix.DEFAULT_GOOD, "Le paramètre &2%s&a du bungee est passé de &2%d&a à &2%d&a.", "NUMBER_BEFORE_START_ANTIBOT", i, QueueHandler.NUMBER_BEFORE_START_ANTIBOT);
		}
	}

	@Cmd(permissionName = "BUNGEE_COMMAND_SETTINGS", args = "INTEGER")
	public void antibotCancelConnection(CommandContext cmd) {
		int i = QueueHandler.NUMBER_BEFORE_CANCEL;
		if (cmd.getArgumentsLength() == 0)
			sendMessage(Prefix.DEFAULT, "Le paramètre &e%s&7 du bungee est actuellement à &e%d&7.\n&c" +
					"C'est à dire qu'il peut y avoir &4%d&c connexions dans la file d'attente MAXIMUM. Les autres connexions seront cancel.\n" +
					"Et donc que le temps d'attente maximum est de &4%s&c.", "NUMBER_BEFORE_CANCEL", i, i, QueueHandler.getMaxQueueTimeString());
		else {
			QueueHandler.NUMBER_BEFORE_CANCEL = (Integer) cmd.getArgument(0);
			sendMessage(Prefix.DEFAULT_GOOD, "Le paramètre &2%s&a du bungee est passé de &2%d&a à &2%d&a.", "NUMBER_BEFORE_CANCEL", i, QueueHandler.NUMBER_BEFORE_CANCEL);
		}
	}

	@Cmd(permissionName = "BUNGEE_COMMAND_SETTINGS", args = "INTEGER")
	public void timeBetween2connections(CommandContext cmd) {
		int i = QueueHandler.TIME_BETWEEN_2;
		if (cmd.getArgumentsLength() == 0)
			sendMessage(Prefix.DEFAULT, "Le paramètre &e%s&7 du bungee est actuellement à &e%.2f&7.\n&c" +
					"C'est à dire qu'il a y 1 connection acceptée toutes les &4%d&7 milisecondes (%d secondes).\n" +
					"Et donc qu'il y a &4%d&c connection/secondes.", "TIME_BETWEEN_2", i, i, i / 1000, 1000d / QueueHandler.TIME_BETWEEN_2);
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
		AntiBotHandler antibot = SecurityHandler.getInstance().getAntibot();
		if (cmd.getArgumentsLength() == 0)
			antibot.showStatus(sender);
		else if (cmd.getArgument(0) instanceof Boolean)
			antibot.setStatus(sender, cmd.getArgument(0));
		else
			antibot.setStatus(sender, !antibot.isEnable());
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

	@Cmd(permissionName = "BUNGEE_COMMAND_RESOURCE_PACK", min = 1, args = { "toggle|get" })
	public void resourcePack(CommandContext cmd) {
		switch (cmd.<String>getArgument(0)) {
		case "toggle":
			sendSuccess("La gestion des resources packs est désormais %s§a.", (SpigotPlayerPack.enabled = !SpigotPlayerPack.enabled) ? "§aactivée" : "§cdésactivée");
			break;
		case "get":
			sendInfo("Resource pack vides envoyés : %d", SpigotPlayerPack.emptySent);
			sendInfo("Joueurs ayant un resource pack de serveur : %d", SpigotPlayerPack.hasPack.size());
			break;
		}
	}

}
