package fr.olympa.core.bungee.login.listener;

import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.login.HandlerLogin;
import fr.olympa.core.bungee.login.events.OlympaPlayerLoginEvent;
import fr.olympa.core.bungee.servers.ServersConnection;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class OlympaLoginListener {

	@EventHandler(priority = EventPriority.HIGH)
	public void onOlympaPlayerLogin(OlympaPlayerLoginEvent event) {
		ProxiedPlayer player = event.getPlayer();
		OlympaPlayer olympaPlayer = event.getOlympaPlayer();
		ProxyServer.getInstance().getScheduler().schedule(OlympaBungee.getInstance(), () -> {
			new AccountProvider(olympaPlayer.getUniqueId()).removeFromCache();
		}, 1, TimeUnit.SECONDS);
		if (event.isCancelled()) {
			return;
		}
		HandlerLogin.unlogged.remove(player);
		player.addGroups(olympaPlayer.getGroup().getAllGroups().stream().map(OlympaGroup::getName).collect(Collectors.toSet()).toArray(new String[0]));
		if (ServersConnection.isAuth(player)) {
			ServersConnection.tryConnectToLobby(event.getPlayer());
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onOlympaPlayerLogin(PostLoginEvent event) {
		ProxiedPlayer player = event.getPlayer();
		HandlerLogin.unlogged.add(player);
	}
}
