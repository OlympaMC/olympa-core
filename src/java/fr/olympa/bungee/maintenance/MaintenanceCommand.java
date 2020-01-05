package fr.olympa.bungee.maintenance;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import fr.olympa.api.objects.OlympaGroup;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.SpigotUtils;
import fr.olympa.bungee.OlympaBungee;
import fr.olympa.bungee.utils.BungeeConfigUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.config.Configuration;

@SuppressWarnings("deprecation")
public class MaintenanceCommand extends Command {

	/*
	 * Dev: Tristiisch74 Pemet de mettre le serveur en maintenance ./maintenance
	 * [on|off] <dev|raison> Raison va s'afficher dans le motd players. TODO: -
	 * Envoyer un tweet pour signaler la maintenance - mettre en bdd pour afficher
	 * le même msg sur le site
	 */

	private OlympaGroup group;

	public MaintenanceCommand() {
		super("maintenance");
		this.group = OlympaGroup.DEV;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		ProxyServer.getInstance().getScheduler().runAsync(OlympaBungee.getInstance(), () -> {
			if (sender instanceof ProxiedPlayer) {
				ProxiedPlayer player = (ProxiedPlayer) sender;
				try {
					if (!new AccountProvider(player.getUniqueId()).get().hasPower(this.group)) {
						sender.sendMessage(SpigotUtils.color("Vous n'avez pas la permission &l(◑_◑)"));
						return;
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (args.length == 0) {
				sender.sendMessage(SpigotUtils.color("Usage > /maintenance <on|off|dev|soon|add|remove|list|status>"));
				return;

			}
			switch (args[0].toLowerCase()) {

			case "off":
				this.setMaintenance(0, null, sender);
				break;

			case "on":
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
					this.setMaintenance(1, "&c" + String.join(" ", reason), sender);
				} else {
					this.setMaintenance(1, "", sender);
				}
				break;

			case "dev":
				this.setMaintenance(2, null, sender);
				break;

			case "soon":
				this.setMaintenance(3, null, sender);
				break;

			case "add":
				if (args.length < 2) {
					sender.sendMessage(BungeeConfigUtils.getString("maintenance.messages.usage"));
					return;
				}

				Configuration config = BungeeConfigUtils.getConfig("maintenance");
				List<String> whitelist = config.getStringList("whitelist");

				if (!whitelist.contains(args[1])) {
					whitelist.add(args[1]);
					sender.sendMessage(BungeeConfigUtils.getString("maintenance.messages.added").replaceAll("%player%", args[1]));
				} else {
					sender.sendMessage(BungeeConfigUtils.getString("maintenance.messages.alreadyadded").replaceAll("%player%", args[1]));
				}
				config.set("whitelist", whitelist);
				BungeeConfigUtils.saveConfig("maintenance");
				break;

			case "remove":
				if (args.length < 2) {
					sender.sendMessage(BungeeConfigUtils.getString("maintenance.messages.usage"));
					return;
				}
				config = BungeeConfigUtils.getConfig("maintenance");
				whitelist = config.getStringList("whitelist");
				if (whitelist.contains(args[1])) {
					whitelist.remove(args[1]);
					sender.sendMessage(BungeeConfigUtils.getString("maintenance.messages.").replaceAll("%player%", args[1]));
				} else {
					sender.sendMessage(BungeeConfigUtils.getString("maintenance.messages.alreadyremoved").replaceAll("%player%", args[1]));
				}
				config.set("whitelist", whitelist);
				BungeeConfigUtils.saveConfig("maintenance");
				break;

			case "list":
				whitelist = BungeeConfigUtils.getConfig("maintenance").getStringList("whitelist");
				sender.sendMessage(BungeeConfigUtils.getString("bungee.maintenance.messages.whitelist")
						.replaceAll("%size%", String.valueOf(whitelist.size()))
						.replaceAll("%list%", String.join(BungeeConfigUtils.getString("bungee.maintenance.messages.whitelist_separator"), whitelist)));
				break;

			case "status":

				config = BungeeConfigUtils.getConfig("maintenance");
				/*
				 * EmeraldServerStatus status =
				 * EmeraldServerStatus.getStatus(BungeeConfigUtils.getConfig("maintenance").
				 * getInt("bungee.maintenance.settings.status")); String message =
				 * BungeeConfigUtils.getConfig("maintenance").getString(
				 * "bungee.maintenance.settings.message"); String statusmsg;
				 * if(status.equals(EmeraldServerStatus.)){ if(!message.isEmpty()) { statusmsg =
				 * "&aActiver (" + message +"&a)"; } else { statusmsg = "&aActiver"; } } else if
				 * (status == 2){ statusmsg = "&aActiver (&6Dev&a)"; } else { statusmsg =
				 * "&cDésactiver"; } sender.sendMessage("&6Le mode maintenance est " +
				 * statusmsg.replaceAll("\n", ""));
				 */
				break;

			default:
				sender.sendMessage(BungeeConfigUtils.getString("bungee.maintenance.messages.usage"));
				break;
			}
		});
	}

	private void setMaintenance(int status, String message, CommandSender player) {
		BungeeConfigUtils.getConfig("maintenance").set("settings.status", status);
		BungeeConfigUtils.getConfig("maintenance").set("settings.message", message);
		BungeeConfigUtils.saveConfig("maintenance");
		String statusmsg;
		if (status == 1) {
			if (message != "") {
				statusmsg = "&aActivé (" + message + "&a)";
			} else {
				statusmsg = "&aActivé";
			}
		} else if (status == 2) {
			statusmsg = "&aActivé (&6Dev&a)";
		} else {
			statusmsg = "&cDésactivé";
		}
		player.sendMessage(SpigotUtils.color("&6Le mode maintenance est désormais " + statusmsg.replaceAll("\n", "")));
	}

}
