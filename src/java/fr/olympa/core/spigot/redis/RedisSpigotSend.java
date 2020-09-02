package fr.olympa.core.spigot.redis;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.bukkit.entity.Player;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.customevents.AsyncOlympaPlayerChangeGroupEvent.ChangeType;
import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.RedisAccess;
import fr.olympa.api.redis.RedisChannel;
import fr.olympa.api.server.OlympaServer;
import fr.olympa.api.server.ServerStatus;
import fr.olympa.api.utils.GsonCustomizedObjectTypeAdapter;
import fr.olympa.core.spigot.OlympaCore;
import redis.clients.jedis.Jedis;

public class RedisSpigotSend {

	public static Map<UUID, Consumer<? super Boolean>> modificationReceive = new HashMap<>();
	public static Cache<UUID, Consumer<String>> askPlayerServer = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).build();

	public static void askServerName() {
		LinkSpigotBungee.Provider.link.launchAsync(() -> {
			try (Jedis jedis = RedisAccess.INSTANCE.connect()) {
				String serverName = OlympaCore.getInstance().getServerName();
				jedis.publish(RedisChannel.SPIGOT_ASK_SERVERNAME.name(), serverName);
			}
			RedisAccess.INSTANCE.disconnect();
			OlympaCore core = OlympaCore.getInstance();
			core.getTask().runTaskLater(() -> {
				if (core.getServerName().contains(":"))
					RedisSpigotSend.askServerName();
			}, 10 * 20);
		});
	}

	public static void askPlayerServer(UUID uuid, Consumer<String> result) {
		RedisSpigotSend.askPlayerServer.put(uuid, result);
		LinkSpigotBungee.Provider.link.launchAsync(() -> {
			try (Jedis jedis = RedisAccess.INSTANCE.connect()) {
				String serverName = OlympaCore.getInstance().getServerName();
				jedis.publish(RedisChannel.SPIGOT_ASK_PLAYERSERVER.name(), serverName + ";" + uuid);
			}
			RedisAccess.INSTANCE.disconnect();
		});
	}

	public static void sendOlympaPlayerToOtherSpigot(OlympaPlayer olympaPlayer, String serverTo) {
		LinkSpigotBungee.Provider.link.launchAsync(() -> {
			try (Jedis jedis = RedisAccess.INSTANCE.connect()) {
				String serverName = OlympaCore.getInstance().getServerName();
				jedis.publish(RedisChannel.SPIGOT_SEND_OLYMPAPLAYER.name(), serverName + ";" + serverTo + ";" + GsonCustomizedObjectTypeAdapter.GSON.toJson(olympaPlayer));
			}
			RedisAccess.INSTANCE.disconnect();
		});
	}

	public static void sendOlympaPlayerToBungee(OlympaPlayer olympaPlayer, String serverTo) {
		try (Jedis jedis = RedisAccess.INSTANCE.connect()) {
			jedis.publish(RedisChannel.SPIGOT_SEND_OLYMPAPLAYER_TO_BUNGEE.name(), GsonCustomizedObjectTypeAdapter.GSON.toJson(olympaPlayer));
		}
		RedisAccess.INSTANCE.disconnect();
	}

	public static void sendOlympaGroupChange(OlympaPlayer olympaPlayer, OlympaGroup groupChanged, long timestamp, ChangeType state, Consumer<? super Boolean> callable) {
		UUID uuid = olympaPlayer.getUniqueId();
		try (Jedis jedis = RedisAccess.INSTANCE.connect()) {
			String serverName = OlympaCore.getInstance().getServerName();
			jedis.publish(RedisChannel.SPIGOT_CHANGE_GROUP.name(), serverName + ";" + GsonCustomizedObjectTypeAdapter.GSON.toJson(olympaPlayer) + ";" + groupChanged.getId() + ":" + timestamp + ";" + state.getState());
		}
		if (callable != null) {
			modificationReceive.put(uuid, callable);
			OlympaCore.getInstance().getTask().runTaskLater("waitModifications" + uuid.toString(), () -> {
				callable.accept(false);
				modificationReceive.remove(uuid);
			}, 3 * 20);
		}
		RedisAccess.INSTANCE.disconnect();
	}

	public static void sendModificationsReceive(UUID uuid) {
		LinkSpigotBungee.Provider.link.launchAsync(() -> {
			try (Jedis jedis = RedisAccess.INSTANCE.connect()) {
				jedis.publish(RedisChannel.SPIGOT_CHANGE_GROUP_RECEIVE.toString(), uuid.toString());
			}
			RedisAccess.INSTANCE.disconnect();
		});
	}

	public static void changeStatus(ServerStatus status) {
		try (Jedis jedis = RedisAccess.INSTANCE.connect()) {
			String serverName = OlympaCore.getInstance().getServerName();
			jedis.publish(RedisChannel.SPIGOT_SERVER_CHANGE_STATUS.name(), serverName + ";" + status.getId());
		}
		RedisAccess.INSTANCE.disconnect();
	}

	public static void sendServerSwitch(Player p, OlympaServer server) {
		try (Jedis jedis = RedisAccess.INSTANCE.connect()) {
			jedis.publish(RedisChannel.SPIGOT_PLAYER_SWITCH_SERVER.name(), p.getName() + ":" + server.name());
		}
		RedisAccess.INSTANCE.disconnect();
	}
	
	public static void sendError(String stackTrace) {
		String serverName = OlympaCore.getInstance().getServerName();
		try (Jedis jedis = RedisAccess.INSTANCE.connect()) {
			jedis.publish(RedisChannel.SPIGOT_RECEIVE_ERROR.name(), serverName + ":" + stackTrace);
		}
		RedisAccess.INSTANCE.disconnect();
	}

}
