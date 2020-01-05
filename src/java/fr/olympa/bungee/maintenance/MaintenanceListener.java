package fr.olympa.bungee.maintenance;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import fr.olympa.api.objects.OlympaGroup;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.bungee.OlympaBungee;
import fr.olympa.bungee.utils.BungeeConfigUtils;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.TabCompleteEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class MaintenanceListener implements Listener {

	private String command;
	private OlympaGroup group;
	private List<String> arg2 = Arrays.asList("on", "off", "dev", "soon", "add", "remove", "list");

	public MaintenanceListener() {
		this.command = "maintenance";
	}

	@EventHandler
	public void onTabComplete(TabCompleteEvent event) {
		if (event.isCancelled()) {
			return;
		}
		String msg = event.getCursor();
		if (!msg.startsWith("/" + this.command)) {
			return;
		}
		String[] args = msg.split(" ");
		List<String> suggestion = event.getSuggestions();
		ProxyServer.getInstance().getScheduler().runAsync(OlympaBungee.getInstance(), () -> {
			if (event.getSender() instanceof ProxiedPlayer) {

				ProxiedPlayer player = (ProxiedPlayer) event.getSender();
				try {
					if (!new AccountProvider(player.getUniqueId()).get().hasPower(this.group)) {
						return;
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			String check = args[args.length - 1].toLowerCase();
			if (args.length == 1) {
				if (msg.endsWith(" ")) {
					for (String arg : this.arg2) {
						if (arg.toLowerCase().startsWith(check)) {
							suggestion.add(arg);
						}
					}
				}
			} else if (args.length == 2) {
				if (!msg.endsWith(" ")) {
					suggestion.addAll(this.arg2);
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
		});
	}
}
