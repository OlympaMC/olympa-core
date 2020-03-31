package fr.olympa.core.bungee.login.listener;

import java.util.Set;
import java.util.stream.Collectors;

import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.core.bungee.datamanagment.AuthListener;
import fr.olympa.core.bungee.login.HandlerLogin;
import fr.olympa.core.bungee.login.events.OlympaPlayerLoginEvent;
import fr.olympa.core.bungee.servers.ServersConnection;
import fr.olympa.core.bungee.utils.BungeeUtils;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class OlympaLoginListener implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onOlympaPlayerLogin(OlympaPlayerLoginEvent event) {
		ProxiedPlayer player = event.getPlayer();
		OlympaPlayer olympaPlayer = event.getOlympaPlayer();
		HandlerLogin.unlogged.remove(player);
		Set<String> groupsNames = olympaPlayer.getGroups().keySet().stream().map(OlympaGroup::getName).collect(Collectors.toSet());
		if (!groupsNames.isEmpty()) {
			player.addGroups(groupsNames.toArray(new String[0]));
		}

		if (player.getServer() != null) {
			ServersConnection.tryConnectToLobby(player, player.getServer());
		}
	}

	@EventHandler
	public void onPlayerDisconnect(PlayerDisconnectEvent event) {
		ProxiedPlayer player = event.getPlayer();
		player.removeGroups(player.getGroups().stream().collect(Collectors.toSet()).toArray(new String[0]));
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPostLogin(PostLoginEvent event) {
		ProxiedPlayer player = event.getPlayer();
		HandlerLogin.unlogged.add(player);
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onServerConnect(ServerConnectedEvent event) {
		ProxiedPlayer player = event.getPlayer();

		String subdomain = AuthListener.cacheServer.asMap().get(player.getName());
		AuthListener.cacheServer.invalidate(player.getName());
		if (subdomain != null) {
			System.out.println("Subdomain " + subdomain);
			if (subdomain.equalsIgnoreCase("play")) {
				ServersConnection.tryConnectToLobby(player, null);
			} else {
				ServerInfo server = ServersConnection.getServer(subdomain);
				if (server == null) {
					player.disconnect(BungeeUtils.connectScreen("&cLe serveur &4" + subdomain + "&c n'est pas disponible."));
					return;
				}
				player.connect(server);
			}
		}
	}

	@EventHandler
	public void onServerConnected(ServerConnectedEvent event) {
		ProxiedPlayer player = event.getPlayer();
		new AccountProvider(player.getUniqueId()).removeFromCache();
	}
}
