package fr.olympa.core.bungee.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import fr.olympa.api.common.server.OlympaServer;
import fr.olympa.api.common.server.ResourcePack;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.packets.BungeePackets;
import fr.olympa.core.bungee.packets.ResourcePackSendPacket;
import fr.olympa.core.bungee.packets.ResourcePackStatusPacket;
import fr.olympa.core.bungee.packets.ResourcePackStatusPacket.ResourcePackStatus;
import fr.olympa.core.bungee.redis.RedisBungeeSend;
import fr.olympa.core.bungee.servers.MonitorServers;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;

public class SpigotPlayerPack {
	
	private static final ResourcePackSendPacket EMPTY_RESOURCE_PACK_PACKET = new ResourcePackSendPacket(new ResourcePack("https://drive.google.com/uc?export=download&id=1ZQ9UJeGnXX7k2bT887htSaGfuGFv7wY8", "6F171A3EC8055762D4763308B9149D4078D17B2D"));
	
	public static boolean enabled = true;
	
	public static Map<UUID, PackInfo> hasPack = new HashMap<>();
	public static int emptySent = 0;
	
	public static void sendPacket(ResourcePackSendPacket packet, ProxiedPlayer player) {
		PackInfo info = hasPack.get(player.getUniqueId());
		if (info == null || !info.pack().equals(packet.getResourcePack())) {
			hasPack.put(player.getUniqueId(), new PackInfo(packet.getResourcePack(), player.getServer().getInfo()));
		}else {
			OlympaBungee.getInstance().sendMessage("§6%s§e avait déjà le pack de ressources chargé.", player.getName());
			if (enabled) BungeePackets.cancelPacket();
		}
	}
	
	public static void statusPacket(ResourcePackStatusPacket packet, ProxiedPlayer player) {
		if (packet.getStatus() == ResourcePackStatus.SUCCESSFULLY_LOADED) {
			OlympaBungee.getInstance().sendMessage("§6%s§e a chargé un pack de ressources.", player.getName());
			RedisBungeeSend.sendPlayerPack(player, true);
		}else if (packet.getStatus() == ResourcePackStatus.FAILED_DOWNLOAD) {
			OlympaBungee.getInstance().sendMessage("§4%s§c a échoué le chargement d'un pack de ressources.", player.getName());
			playerLeaves(player);
		}
	}
	
	public static void serverConnected(ProxiedPlayer player, Server server) {
		if (!enabled) return;
		if (!hasPack.containsKey(player.getUniqueId())) return;
		OlympaServer connectingServer = MonitorServers.getMonitor(server.getInfo()).getOlympaServer();
		if (!connectingServer.hasPack() && !MonitorServers.getMonitor(hasPack.get(player.getUniqueId()).server()).getOlympaServer().equals(connectingServer)) {
			hasPack.remove(player.getUniqueId());
			player.unsafe().sendPacket(EMPTY_RESOURCE_PACK_PACKET);
			emptySent++;
		}
	}
	
	public static void playerLeaves(ProxiedPlayer player) {
		if (hasPack.remove(player.getUniqueId()) != null) RedisBungeeSend.sendPlayerPack(player, false);
	}
	
}

record PackInfo(ResourcePack pack, ServerInfo server) {}
