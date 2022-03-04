package fr.olympa.core.bungee.login.listener;

import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.redis.RedisBungeeSend;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class PlayerSwitchListener implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onServerConnect(ServerConnectEvent event) {
		ProxiedPlayer player = event.getPlayer();
		OlympaBungee.getInstance().sendMessage("§7ServerConnect §e" + player.getName() + " §7(§6" + (player.getServer() == null ? "" : player.getServer().getInfo().getName() + "§7 -> §6") + event.getTarget().getName() + "§7)");
		if (event.isCancelled())
			return;
		Server server = player.getServer();
		if (server == null)
			return;
		ServerInfo serverInfo = server.getInfo();
		ServerInfo targetServer = event.getTarget();
		RedisBungeeSend.askGiveOlympaPlayer(serverInfo, targetServer, player.getUniqueId());
	}
}
