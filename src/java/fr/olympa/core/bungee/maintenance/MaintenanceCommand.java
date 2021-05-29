package fr.olympa.core.bungee.maintenance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import fr.olympa.api.bungee.command.BungeeCommand;
import fr.olympa.api.bungee.config.BungeeCustomConfig;
import fr.olympa.api.chat.ColorUtils;
import fr.olympa.api.permission.list.OlympaCorePermissionsBungee;
import fr.olympa.api.server.ServerStatus;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.OlympaBungee;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.TabExecutor;
import net.md_5.bungee.config.Configuration;

public class MaintenanceCommand extends BungeeCommand implements TabExecutor {

	/*
	 * Dev: Tristiisch74 Pemet de mettre le serveur en maintenance ./maintenance
	 * [on|off] <dev|raison> Raison va s'afficher dans le motd players. TODO: -
	 * Envoyer un tweet pour signaler la maintenance - mettre en bdd pour afficher
	 * le même msg sur le site
	 */

	static List<String> arg2 = new ArrayList<>();

	public MaintenanceCommand(Plugin plugin) {
		super(plugin, "maintenance", OlympaCorePermissionsBungee.MAINTENANCE_COMMAND, "maint");
		minArg = 1;
		arg2.addAll(Arrays.asList("status", "add", "remove", "list"));
		arg2.addAll(ServerStatus.getCommandsArgs());
		usageString = "<" + String.join("|", MaintenanceCommand.arg2).toLowerCase() + "> [joueur]";
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		ServerStatus maintenanceStatus = ServerStatus.getByCommandArg(buildText(0, args));
		if (maintenanceStatus != null)
			switch (maintenanceStatus) {
			case MAINTENANCE:
				if (args.length >= 2) {
					String[] reason = Arrays.copyOfRange(args, 1, args.length);
					int i1 = 0;
					for (int i2 = 0; reason.length > i2; i2++) {
						i1 += reason[i2].length() + 1;
						if (i1 >= 25) {
							reason[i2] += "\n&c";
							i1 = 0;
						}
					}
					setServerStatus(maintenanceStatus, "&c" + String.join(" ", reason), sender);
				} else
					setServerStatus(maintenanceStatus, "", sender);
				break;
			default:
				setServerStatus(maintenanceStatus, null, sender);
				break;
			}
		else {
			OlympaBungee olympaBungee = (OlympaBungee) plugin;
			BungeeCustomConfig customConfig = olympaBungee.getMaintCustomConfig();
			Configuration maintconfig = customConfig.getConfig();
			Configuration defaultConfig = olympaBungee.getConfig();
			switch (args[0].toLowerCase()) {
			case "add":
				if (args.length < 2) {
					sendUsage(command);
					return;
				}
				List<String> whitelist = maintconfig.getStringList("whitelist");
				if (!whitelist.contains(args[1])) {
					whitelist.add(args[1]);
					sendMessage(defaultConfig.getString("maintenance.messages.added").replace("%player%", args[1]));
				} else
					sendMessage(defaultConfig.getString("maintenance.messages.alreadyadded").replace("%player%", args[1]));
				maintconfig.set("whitelist", whitelist);
				customConfig.saveSafe();
				break;
			case "remove":
				if (args.length < 2) {
					sendUsage(command);
					return;
				}
				whitelist = maintconfig.getStringList("whitelist");
				if (whitelist.contains(args[1])) {
					whitelist.remove(args[1]);
					sendMessage(defaultConfig.getString("maintenance.messages.removed").replace("%player%", args[1]));
				} else
					sendMessage(defaultConfig.getString("maintenance.messages.alreadyremoved").replace("%player%", args[1]));
				maintconfig.set("whitelist", whitelist);
				customConfig.saveSafe();
				break;
			case "list":
				whitelist = maintconfig.getStringList("whitelist");
				sendMessage(defaultConfig.getString("maintenance.messages.whitelist")
						.replace("%size%", String.valueOf(whitelist.size()))
						.replace("%list%", String.join(defaultConfig.getString("maintenance.messages.whitelist_separator"), whitelist)));
				break;
			case "status":
				String message = maintconfig.getString("settings.message");
				String statusString = maintconfig.getString("settings.status");
				maintenanceStatus = ServerStatus.get(statusString);
				String statusmsg = "";
				if (message.equals(""))
					statusmsg = "(" + message.replace("\n", "") + ")";
				sendMessage(ColorUtils.color("&6Le mode maintenance est en mode " + maintenanceStatus.getNameColored() + "&6" + statusmsg + "."));
				break;
			default:
				sendUsage(command);
				break;
			}
		}
	}

	@Override
	public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
		switch (args.length) {
		case 0:
			return arg2;
		case 1:
			return Utils.startWords(args[0], arg2);
		case 2:
			switch (Utils.removeAccents(args[0]).toLowerCase()) {
			case "remove":
				Configuration maintconfig = OlympaBungee.getInstance().getMaintConfig();
				if (maintconfig != null)
					return Utils.startWords(args[1], maintconfig.getStringList("whitelist"));
				break;
			case "add":
				return /*MySQL.getPlayersBySimilarName(args[1]).stream().map(OlympaPlayer::getName).collect(Collectors.toList());*/ Collections.emptyList();
			}
			break;
		}
		return Collections.emptyList();
	}

	@SuppressWarnings("deprecation")
	private void setServerStatus(ServerStatus status, String message, CommandSender player) {
		BungeeCustomConfig customConfig = OlympaBungee.getInstance().getMaintCustomConfig();
		Configuration config = customConfig.getConfig();
		config.set("settings.status", status.getName());
		config.set("settings.message", message);
		customConfig.saveSafe();
		String statusmsg = "";
		if (message != null && !message.isEmpty())
			statusmsg = "(" + message.replace("\n", "") + ")";
		player.sendMessage(ColorUtils.color("&6Le mode maintenance est désormais en mode " + status.getNameColored() + "&6" + statusmsg + "."));
	}
}
