package fr.olympa.core.bungee.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.packets.ResourcePackSendPacket;
import fr.olympa.core.bungee.packets.ResourcePackStatusPacket;
import fr.olympa.core.bungee.packets.ResourcePackStatusPacket.ResourcePackStatus;
import fr.olympa.core.bungee.redis.RedisBungeeSend;
import fr.olympa.core.bungee.servers.MonitorServers;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;

public class SpigotPlayerPack {
	
	private static final ResourcePackSendPacket EMPTY_RESOURCE_PACK_PACKET = new ResourcePackSendPacket("https://drive.google.com/uc?export=download&id=1ZQ9UJeGnXX7k2bT887htSaGfuGFv7wY8", "6F171A3EC8055762D4763308B9149D4078D17B2D");
	
	public static boolean enabled = false;
	
	public static Map<UUID, ServerInfo> hasPack = new HashMap<>();
	public static int emptySent = 0;
	
	public static void sendPacket(ResourcePackSendPacket packet) {
		
	}
	
	public static void statusPacket(ResourcePackStatusPacket packet, ProxiedPlayer player) {
		if (packet.getStatus() == ResourcePackStatus.SUCCESSFULLY_LOADED) {
			OlympaBungee.getInstance().sendMessage("§6%s§e a chargé un pack de ressources.", player.getName());
			RedisBungeeSend.sendPlayerPack(player, true);
			
			hasPack.put(player.getUniqueId(), player.getServer().getInfo());
		}else if (packet.getStatus() == ResourcePackStatus.FAILED_DOWNLOAD) {
			OlympaBungee.getInstance().sendMessage("§4%s§c a échoué le chargement d'un pack de ressources.", player.getName());
			playerLeaves(player);
		}
	}
	
	public static void serverConnected(ProxiedPlayer player, Server server) {
		if (!enabled) return;
		if (!hasPack.containsKey(player.getUniqueId())) return;
		if (!MonitorServers.getMonitor(server.getInfo()).getOlympaServer().hasPack() && !hasPack.get(player.getUniqueId()).equals(server.getInfo())) {
			hasPack.remove(player.getUniqueId());
			player.unsafe().sendPacket(EMPTY_RESOURCE_PACK_PACKET);
			emptySent++;
		}
	}
	
	public static void playerLeaves(ProxiedPlayer player) {
		if (hasPack.remove(player.getUniqueId()) != null) RedisBungeeSend.sendPlayerPack(player, false);
	}
	
}
