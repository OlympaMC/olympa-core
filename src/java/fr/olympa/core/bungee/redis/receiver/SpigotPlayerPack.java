package fr.olympa.core.bungee.redis.receiver;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.servers.MonitorServers;
import fr.olympa.core.bungee.servers.ServersConnection;
import fr.olympa.core.bungee.utils.ResourcePackSendPacket;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import redis.clients.jedis.JedisPubSub;

public class SpigotPlayerPack extends JedisPubSub {
	
	private static final ResourcePackSendPacket EMPTY_RESOURCE_PACK_PACKET = new ResourcePackSendPacket("https://drive.google.com/uc?export=download&id=1ZQ9UJeGnXX7k2bT887htSaGfuGFv7wY8", "6F171A3EC8055762D4763308B9149D4078D17B2D");
	
	public static boolean enabled = false;
	
	public static Map<UUID, ServerInfo> hasPack = new HashMap<>();
	public static int emptySent = 0;
	
	@Override
	public void onMessage(String channel, String message) {
		super.onMessage(channel, message);
		String[] args = message.split(";");
		ProxiedPlayer player = OlympaBungee.getInstance().getProxy().getPlayer(args[0]);
		ServerInfo server = ServersConnection.getServerByNameOrIpPort(args[1]);
		hasPack.put(player.getUniqueId(), server);
	}
	
	public static void serverConnected(ProxiedPlayer player, Server server) {
		if (!hasPack.containsKey(player.getUniqueId())) return;
		if (!MonitorServers.getMonitor(server.getInfo()).getOlympaServer().hasPack() && !hasPack.get(player.getUniqueId()).equals(server.getInfo())) {
			hasPack.remove(player.getUniqueId());
			if (enabled) {
				player.unsafe().sendPacket(EMPTY_RESOURCE_PACK_PACKET);
				emptySent++;
			}
		}
	}
	
}
