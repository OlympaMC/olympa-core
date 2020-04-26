package fr.olympa.core.bungee.login.listener;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.objects.OlympaServer;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.datamanagment.CachePlayer;
import fr.olympa.core.bungee.datamanagment.DataHandler;
import fr.olympa.core.bungee.login.events.OlympaPlayerLoginEvent;
import fr.olympa.core.bungee.servers.ServersConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class OlympaLoginListener implements Listener {

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onOlympaPlayerLogin(OlympaPlayerLoginEvent event) {
		ProxiedPlayer player = event.getPlayer();
		OlympaPlayer olympaPlayer = event.getOlympaPlayer();
		Set<String> groupsNames = olympaPlayer.getGroups().keySet().stream().map(OlympaGroup::getName).collect(Collectors.toSet());
		if (!groupsNames.isEmpty()) {
			player.addGroups(groupsNames.toArray(new String[0]));
		}
		String ip = player.getAddress().getAddress().getHostAddress();
		if (!olympaPlayer.getIp().equals(ip)) {
			olympaPlayer.addNewIp(ip);
		}
		OlympaBungee.getInstance().getTask().schedule(OlympaBungee.getInstance(), () -> {
			CachePlayer cache = DataHandler.get(player.getName());
			if (cache != null) {
				String subdomain = cache.getSubDomain();
				DataHandler.removePlayer(player.getName());
				if (subdomain != null) {
					if (subdomain.equalsIgnoreCase("buildeur")) {
						ServersConnection.tryConnect(player, OlympaServer.BUILDEUR, null);
						return;
					} else if (subdomain.equalsIgnoreCase("dev")) {
						ServersConnection.tryConnect(player, OlympaServer.DEV, null);
						return;
					}
				}
			}
			ServersConnection.tryConnect(player, OlympaServer.LOBBY, null);
		}, 2, TimeUnit.SECONDS);
	}

	@EventHandler
	public void onPlayerDisconnect(PlayerDisconnectEvent event) {
		ProxiedPlayer player = event.getPlayer();
		player.removeGroups(player.getGroups().stream().collect(Collectors.toSet()).toArray(new String[0]));
		ServersConnection.removeTryToConnect(player);
	}

	@EventHandler
	public void onServerConnect(ServerConnectEvent event) {
		ProxiedPlayer player = event.getPlayer();
		ServersConnection.removeTryToConnect(player);
	}
}
