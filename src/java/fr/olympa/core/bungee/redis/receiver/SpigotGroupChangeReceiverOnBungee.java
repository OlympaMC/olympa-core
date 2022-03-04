package fr.olympa.core.bungee.redis.receiver;

import fr.olympa.api.bungee.customevent.BungeeOlympaGroupChangeEvent;
import fr.olympa.api.common.groups.OlympaGroup;
import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.spigot.customevents.AsyncOlympaPlayerChangeGroupEvent.ChangeType;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.servers.ServersConnection;
import fr.olympa.core.common.utils.GsonCustomizedObjectTypeAdapter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import redis.clients.jedis.JedisPubSub;

public class SpigotGroupChangeReceiverOnBungee extends JedisPubSub {

	@Override
	public void onMessage(String channel, String message) {
		String[] args = message.split(";");
		ServerInfo serverInfo = ServersConnection.getServerByNameOrIpPort(args[0]);
		OlympaPlayer olympaPlayer = GsonCustomizedObjectTypeAdapter.GSON.fromJson(args[1], OlympaPlayer.class);
		String[] infoGroup = args[2].split(":");
		OlympaGroup groupChanged = OlympaGroup.getById(Integer.parseInt(infoGroup[0]));
		long timestamp = Integer.parseInt(infoGroup[1]);
		ChangeType state = ChangeType.get(Integer.parseInt(args[3]));
		ProxiedPlayer player = ProxyServer.getInstance().getPlayer(olympaPlayer.getUniqueId());
		ProxyServer.getInstance().getPluginManager().callEvent(new BungeeOlympaGroupChangeEvent(player, olympaPlayer, groupChanged, timestamp, state));
		OlympaBungee.getInstance().sendRedis("§aChangement de groupe pour §2§l" + olympaPlayer.getName() + " §a: " + olympaPlayer.getGroupsToHumainString() + " §7(depuis " + serverInfo.getName() + ")");
	}
}
