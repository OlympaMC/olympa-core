package fr.olympa.core.bungee.redis;

import fr.olympa.api.customevents.AsyncOlympaPlayerChangeGroupEvent.ChangeType;
import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.utils.GsonCustomizedObjectTypeAdapter;
import fr.olympa.core.bungee.api.customevent.OlympaGroupChangeEvent;
import fr.olympa.core.bungee.servers.ServersConnection;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import redis.clients.jedis.JedisPubSub;

public class PlayerGroupChangeListener extends JedisPubSub {

	@Override
	public void onMessage(String channel, String message) {
		super.onMessage(channel, message);
		String[] args = message.split(";");
		if (args.length < 4) {
			return;
		}
		ServerInfo serverInfo = ServersConnection.getServerByNameOrIpPort(args[0]);
		if (serverInfo == null) {
			return;
		}
		OlympaPlayer olympaPlayer = GsonCustomizedObjectTypeAdapter.GSON.fromJson(args[1], OlympaPlayer.class);
		String[] infoGroup = args[2].split(":");
		OlympaGroup groupChanged = OlympaGroup.getById(Integer.parseInt(infoGroup[0]));
		long timestamp = Integer.parseInt(infoGroup[1]);
		ChangeType state = ChangeType.get(Integer.parseInt(args[3]));
		ProxiedPlayer player = ProxyServer.getInstance().getPlayer(olympaPlayer.getUniqueId());
		ProxyServer.getInstance().getPluginManager().callEvent(new OlympaGroupChangeEvent(player, olympaPlayer, groupChanged, timestamp, state));
	}
}
