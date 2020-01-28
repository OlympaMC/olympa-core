package fr.olympa.core.bungee.maintenance;

import java.util.List;
import java.util.stream.Collectors;

import fr.olympa.core.bungee.utils.BungeeConfigUtils;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.TabCompleteEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class MaintenanceListener implements Listener {

	@EventHandler
	public void onTabComplete(TabCompleteEvent event) {
		if (event.isCancelled()) {
			return;
		}
		String msg = event.getCursor();
		if (!msg.startsWith("/" + MaintenanceCommand.command)) {
			return;
		}
		String[] args = msg.split(" ");
		List<String> suggestion = event.getSuggestions();
		if (event.getSender() instanceof ProxiedPlayer) {
			ProxiedPlayer player = (ProxiedPlayer) event.getSender();
			if (!MaintenanceCommand.permission.hasPermission(player.getUniqueId())) {
				return;
			}
		}
		String check = args[args.length - 1].toLowerCase();
		if (args.length == 1) {
			if (msg.endsWith(" ")) {
				for (String arg : MaintenanceCommand.arg2) {
					if (arg.toLowerCase().startsWith(check)) {
						suggestion.add(arg);
					}
				}
			}
		} else if (args.length == 2) {
			if (!msg.endsWith(" ")) {
				suggestion.addAll(MaintenanceCommand.arg2);
			} else {
				if (args[1].equalsIgnoreCase("remove")) {
					suggestion.addAll(BungeeConfigUtils.getConfig("maintenance").getStringList("whitelist"));
				}
			}
		} else if (args.length == 3) {
			if (!msg.endsWith(" ")) {
				if (args[1].equalsIgnoreCase("add")) {
					suggestion.addAll(ProxyServer.getInstance().getPlayers().stream().map(ProxiedPlayer::getName).filter(playerName -> playerName.toLowerCase().startsWith(check)).collect(Collectors.toList()));
				} else if (args[1].equalsIgnoreCase("remove")) {
					suggestion.addAll(BungeeConfigUtils.getConfig("maintenance").getStringList("whitelist").stream().filter(playerName -> playerName.toLowerCase().startsWith(check)).collect(Collectors.toList()));
				}
			}
		}
	}
}
