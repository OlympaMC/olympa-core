package fr.olympa.core.bungee.login.listener;

import fr.olympa.core.bungee.redis.RedisBungeeSend;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class PlayerSwitchListener implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onServerConnect(ServerConnectEvent event) {
		if (event.isCancelled())
			return;
		ProxiedPlayer player = event.getPlayer();
		ServerInfo server = player.getServer().getInfo();
		ServerInfo targetServer = event.getTarget();
		RedisBungeeSend.askGiveOlympaPlayer(server, targetServer, player.getUniqueId());
	}
}
