package fr.olympa.core.spigot.redis;

import org.bukkit.entity.Player;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.customevents.AsyncOlympaPlayerChangeGroupEvent.ChangeType;
import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.RedisAccess;
import fr.olympa.api.redis.RedisChannel;
import fr.olympa.api.server.OlympaServer;
import fr.olympa.api.utils.GsonCustomizedObjectTypeAdapter;
import fr.olympa.core.spigot.OlympaCore;
import redis.clients.jedis.Jedis;

public class RedisSpigotSend {

	public static void askServerName() {
		LinkSpigotBungee.Provider.link.launchAsync(() -> {
			try (Jedis jedis = RedisAccess.INSTANCE.newConnection()) {
				String serverName = OlympaCore.getInstance().getServerName();
				jedis.publish(RedisChannel.SPIGOT_ASK_SERVERNAME.name(), serverName);
			}
			RedisAccess.INSTANCE.disconnect();
			OlympaCore core = OlympaCore.getInstance();
			core.getTask().runTaskLater(() -> {
				if (core.getServerName().contains(":"))
					RedisSpigotSend.askServerName();
			}, 5 * 20);
		});
	}

	public static void giveOlympaPlayer(OlympaPlayer olympaPlayer, String serverTo) {
		try (Jedis jedis = RedisAccess.INSTANCE.newConnection()) {
			String serverName = OlympaCore.getInstance().getServerName();
			jedis.publish(RedisChannel.SPIGOT_SEND_OLYMPAPLAYER.name(), serverName + ";" + serverTo + ";" + GsonCustomizedObjectTypeAdapter.GSON.toJson(olympaPlayer));
		}
		RedisAccess.INSTANCE.disconnect();
	}

	public static void sendOlympaGroupChange(OlympaPlayer olympaPlayer, OlympaGroup groupChanged, long timestamp, ChangeType state) {
		try (Jedis jedis = RedisAccess.INSTANCE.newConnection()) {
			String serverName = OlympaCore.getInstance().getServerName();
			jedis.publish(RedisChannel.SPIGOT_PLAYER_HAS_GROUP_CHANGED.name(), serverName + ";" + GsonCustomizedObjectTypeAdapter.GSON.toJson(olympaPlayer) + ";" + groupChanged.getId() + ":" + timestamp + ";" + state.getState());
		}
		RedisAccess.INSTANCE.disconnect();
	}

	public static void sendShutdown() {
		try (Jedis jedis = RedisAccess.INSTANCE.newConnection()) {
			String serverName = OlympaCore.getInstance().getServerName();
			jedis.publish(RedisChannel.SPIGOT_SERVER_SHUTDOWN.name(), serverName);
		}
		RedisAccess.INSTANCE.disconnect();
	}

	public static void sendServerSwitch(Player p, OlympaServer server) {
		try (Jedis jedis = RedisAccess.INSTANCE.newConnection()) {
			jedis.publish(RedisChannel.SPIGOT_PLAYER_SWITCH_SERVER.name(), p.getName() + ":" + server.name());
		}
		RedisAccess.INSTANCE.disconnect();
	}

}
