package fr.olympa.core.bungee.redis;

import java.net.InetSocketAddress;
import java.util.UUID;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.common.redis.RedisChannel;
import fr.olympa.core.bungee.ban.objects.OlympaSanction;
import fr.olympa.core.common.redis.RedisAccess;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import redis.clients.jedis.Jedis;

public class RedisBungeeSend {

	// TODO Bungee recever
	public static void sendSanction(ServerInfo serverFrom, UUID uuid, OlympaSanction sanction) {
		try (Jedis jedis = RedisAccess.INSTANCE.connect()) {
			jedis.publish(RedisChannel.SPIGOT_SEND_SANCTION.name(), serverFrom.getName() + ";" + uuid.toString() + ";" + LinkSpigotBungee.getInstance().getGson().toJson(sanction));
		}
		RedisAccess.INSTANCE.disconnect();
	}

	public static void askGiveOlympaPlayer(ServerInfo serverFrom, ServerInfo serverTo, UUID uuid) {
		try (Jedis jedis = RedisAccess.INSTANCE.connect()) {
			jedis.publish(RedisChannel.BUNGEE_ASK_SEND_OLYMPAPLAYER.name(), serverFrom.getName() + ";" + serverTo.getName() + ";" + uuid.toString());
		}
		RedisAccess.INSTANCE.disconnect();
	}

	public static void sendOlympaPlayer(ServerInfo target, OlympaPlayer olympaPlayer) {
		try (Jedis jedis = RedisAccess.INSTANCE.connect()) {
			jedis.publish(RedisChannel.BUNGEE_SEND_OLYMPAPLAYER.name(), target.getName() + ";" + LinkSpigotBungee.getInstance().getGson().toJson(olympaPlayer));
		}
		RedisAccess.INSTANCE.disconnect();
	}

	public static void sendOlympaPlayerFirstConnection(ServerInfo target, OlympaPlayer olympaPlayer) {
		try (Jedis jedis = RedisAccess.INSTANCE.connect()) {
			jedis.publish(RedisChannel.SPIGOT_SEND_OLYMPAPLAYER.name(), "bungee;" + target.getName() + ";" + LinkSpigotBungee.getInstance().getGson().toJson(olympaPlayer));
		}
		RedisAccess.INSTANCE.disconnect();
	}

	public static void sendOlympaPlayerTeamspeakIDChanged(OlympaPlayer olympaPlayer) {
		try (Jedis jedis = RedisAccess.INSTANCE.connect()) {
			jedis.publish(RedisChannel.BUNGEE_SEND_TEAMSPEAKID.name(), olympaPlayer.getUniqueId() + ";" + olympaPlayer.getTeamspeakId());
		}
		RedisAccess.INSTANCE.disconnect();
	}

	@SuppressWarnings("deprecation")
	public static void sendServerName(ServerInfo serverInfo) {
		try (Jedis jedis = RedisAccess.INSTANCE.connect()) {
			InetSocketAddress adress = serverInfo.getAddress();
			jedis.publish(RedisChannel.BUNGEE_ASK_SEND_SERVERNAME.name(), adress.getAddress().getHostAddress().replace("127.0.0.1", "localhost") + ";" + adress.getPort() + ";" + serverInfo.getName());
		}
		RedisAccess.INSTANCE.disconnect();
	}

	public static void sendPlayerServer(String serverName, String playerUUID, String playerServer) {
		try (Jedis jedis = RedisAccess.INSTANCE.connect()) {
			jedis.publish(RedisChannel.BUNGEE_SEND_PLAYERSERVER.name(), serverName + ";" + playerUUID + ";" + playerServer);
		}
		RedisAccess.INSTANCE.disconnect();
	}

	public static void sendPlayerPack(ProxiedPlayer p, boolean set) {
		try (Jedis jedis = RedisAccess.INSTANCE.connect()) {
			jedis.publish(RedisChannel.BUNGEE_PLAYER_RESOUREPACK.name(), p.getName() + ";" + p.getServer().getInfo().getName() + ";" + Boolean.toString(set));
		}
		RedisAccess.INSTANCE.disconnect();
	}

}
