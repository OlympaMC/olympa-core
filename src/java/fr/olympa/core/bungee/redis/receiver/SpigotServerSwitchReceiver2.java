package fr.olympa.core.bungee.redis.receiver;

import fr.olympa.api.chat.TxtComponentBuilder;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.servers.MonitorInfoBungee;
import fr.olympa.core.bungee.servers.MonitorServers;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent.Reason;
import redis.clients.jedis.JedisPubSub;

public class SpigotServerSwitchReceiver2 extends JedisPubSub {

	@Override
	public void onMessage(String channel, String message) {
		super.onMessage(channel, message);
		String[] args = message.split(":");
		OlympaBungee instance = OlympaBungee.getInstance();
		ProxiedPlayer player = OlympaBungee.getInstance().getProxy().getPlayer(args[0]);
		String serverName;
		ServerInfo server = instance.getProxy().getServersCopy().get(args[1]);
		if (server == null)
			serverName = args[1];
		else {
			MonitorInfoBungee monitorInfo = MonitorServers.getMonitor(server);
			if (monitorInfo == null)
				serverName = Utils.capitalize(server.getName());
			else
				serverName = monitorInfo.getHumanName();
		}
		Callback<Boolean> callback = (result, error) -> {
			if (result)
				player.sendMessage(TxtComponentBuilder.of(Prefix.DEFAULT_GOOD, "Connexion au serveur %s établie !", serverName));
			else
				player.sendMessage(TxtComponentBuilder.of(Prefix.DEFAULT_BAD, "Echec de la connexion au serveur &4%s&c: &4%s&c. ", serverName, error.getMessage()));
		};
		System.out.println(String.format("[REDIS] Demande de serveur switch %s sur le serv %s.", player.getName(), serverName));
		player.connect(instance.getProxy().getServersCopy().get(serverName), callback, false, Reason.PLUGIN_MESSAGE, 10000);
	}

}
