package fr.olympa.core.bungee.maintenance;

import java.util.Arrays;
import java.util.List;

import fr.olympa.api.maintenance.MaintenanceStatus;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.utils.SpigotUtils;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.api.command.BungeeCommand;
import fr.olympa.core.bungee.utils.BungeeConfigUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;

@SuppressWarnings("deprecation")
public class MaintenanceCommand extends BungeeCommand {

	/*
	 * Dev: Tristiisch74 Pemet de mettre le serveur en maintenance ./maintenance
	 * [on|off] <dev|raison> Raison va s'afficher dans le motd players. TODO: -
	 * Envoyer un tweet pour signaler la maintenance - mettre en bdd pour afficher
	 * le même msg sur le site
	 */

	private OlympaPermission permission;

	public MaintenanceCommand(Plugin plugin) {
		super(plugin, "maintenance", OlympaCorePermissions.MAINTENANCE_COMMAND, "maint");
		this.minArg = 1;

		this.usageString = "<add|remove|list|status|" + String.join("|", MaintenanceStatus.getNames()).toLowerCase() + "> [joueur]";
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		ProxyServer.getInstance().getScheduler().runAsync(OlympaBungee.getInstance(), () -> {
			if (sender instanceof ProxiedPlayer) {
				ProxiedPlayer player = (ProxiedPlayer) sender;
				if (!this.permission.hasPermission(player.getUniqueId())) {
					this.sendDoNotHavePermission();
					return;
				}
			}
			MaintenanceStatus maintenanceStatus = MaintenanceStatus.getByCommandArg(args[0]);
			if (maintenanceStatus != null) {
				switch (maintenanceStatus) {

				case OPEN:
					this.setMaintenance(maintenanceStatus, null, sender);
					break;

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
						this.setMaintenance(maintenanceStatus, "&c" + String.join(" ", reason), sender);
					} else {
						this.setMaintenance(maintenanceStatus, "", sender);
					}
					break;

				case DEV:
					this.setMaintenance(maintenanceStatus, null, sender);
					break;

				case SOON:
					this.setMaintenance(maintenanceStatus, null, sender);
					break;

				case BETA:
					this.setMaintenance(maintenanceStatus, null, sender);
					break;
				default:
					break;
				}
			} else {
				Configuration config = BungeeConfigUtils.getConfig("maintenance");
				switch (args[0].toLowerCase()) {
				case "add":
					if (args.length < 2) {
						this.sendUsage();
						return;
					}

					List<String> whitelist = config.getStringList("whitelist");

					if (!whitelist.contains(args[1])) {
						whitelist.add(args[1]);
						sender.sendMessage(BungeeConfigUtils.getString("maintenance.messages.added").replace("%player%", args[1]));
					} else {
						sender.sendMessage(BungeeConfigUtils.getString("maintenance.messages.alreadyadded").replace("%player%", args[1]));
					}
					config.set("whitelist", whitelist);
					BungeeConfigUtils.saveConfig("maintenance");
					break;

				case "remove":
					if (args.length < 2) {
						this.sendUsage();
						return;
					}
					whitelist = config.getStringList("whitelist");
					if (whitelist.contains(args[1])) {
						whitelist.remove(args[1]);
						sender.sendMessage(BungeeConfigUtils.getString("maintenance.messages.removed").replace("%player%", args[1]));
					} else {
						sender.sendMessage(BungeeConfigUtils.getString("maintenance.messages.alreadyremoved").replace("%player%", args[1]));
					}
					config.set("whitelist", whitelist);
					BungeeConfigUtils.saveConfig("maintenance");
					break;

				case "list":
					whitelist = BungeeConfigUtils.getConfig("maintenance").getStringList("whitelist");
					sender.sendMessage(BungeeConfigUtils.getString("messages.whitelist")
							.replace("%size%", String.valueOf(whitelist.size()))
							.replace("%list%", String.join(BungeeConfigUtils.getString("maintenance.messages.whitelist_separator"), whitelist)));
					break;

				case "status":

					String message = config.getString("settings.message");
					String statusString = config.getString("settings.status");
					maintenanceStatus = MaintenanceStatus.get(statusString);
					String statusmsg = "";
					if (message != "") {
						statusmsg = "(" + message.replaceAll("\n", "") + ")";
					}
					sender.sendMessage(SpigotUtils.color("&6Le mode maintenance est en mode " + maintenanceStatus.getNameColored() + "&6" + statusmsg + "."));
					break;

				default:
					this.sendUsage();
					break;
				}
			}
		});

	}

	private void setMaintenance(MaintenanceStatus maintenanceStatus, String message, CommandSender player) {
		Configuration config = BungeeConfigUtils.getConfig("maintenance");
		config.set("settings.status", maintenanceStatus.getName());
		config.set("settings.message", message);
		BungeeConfigUtils.saveConfig("maintenance");
		String statusmsg = "";
		if (message != null && !message.isEmpty()) {
			statusmsg = "(" + message.replace("\n", "") + ")";
		}
		player.sendMessage(SpigotUtils.color("&6Le mode maintenance est désormais en mode " + maintenanceStatus.getNameColored() + "&6" + statusmsg + "."));
	}

}
