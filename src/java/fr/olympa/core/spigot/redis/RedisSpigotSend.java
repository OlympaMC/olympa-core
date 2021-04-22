package fr.olympa.core.spigot.redis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.bukkit.entity.Player;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.GsonBuilder;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.customevents.AsyncOlympaPlayerChangeGroupEvent.ChangeType;
import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.redis.RedisAccess;
import fr.olympa.api.redis.RedisChannel;
import fr.olympa.api.report.OlympaReport;
import fr.olympa.api.server.MonitorInfo;
import fr.olympa.api.server.OlympaServer;
import fr.olympa.api.server.ServerStatus;
import fr.olympa.api.utils.GsonCustomizedObjectTypeAdapter;
import fr.olympa.core.spigot.OlympaCore;
import redis.clients.jedis.Jedis;

public class RedisSpigotSend {

	public static Map<UUID, Consumer<? super Boolean>> modificationReceive = new HashMap<>();
	public static Cache<UUID, Consumer<String>> askPlayerServer = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).build();
	public static List<Consumer<List<MonitorInfo>>> askServerInfo = new ArrayList<>();
	public static boolean errorsEnabled = false;

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
			}, 10, TimeUnit.SECONDS);
		});
	}

	/**
	 * Déclanche {@link fr.olympa.api.customevents.MonitorServerInfoReceiveEvent#MonitorServerInfoReceiveEvent monitorServerInfoReceiveEvent}
	 */
	public static void askServerInfo(Consumer<List<MonitorInfo>> callback) {
		askServerInfo.add(callback);
		LinkSpigotBungee.Provider.link.launchAsync(() -> {
			try (Jedis jedis = RedisAccess.INSTANCE.connect()) {
				String serverName = OlympaCore.getInstance().getServerName();
				jedis.publish(RedisChannel.SPIGOT_ASK_SERVERINFO.name(), serverName);
			}
			RedisAccess.INSTANCE.disconnect();
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
		Runnable run = () -> {
			try (Jedis jedis = RedisAccess.INSTANCE.connect()) {
				String serverName = OlympaCore.getInstance().getServerName();
				jedis.publish(RedisChannel.SPIGOT_SEND_OLYMPAPLAYER.name(), serverName + ";" + serverTo + ";" + GsonCustomizedObjectTypeAdapter.GSON.toJson(olympaPlayer));
			}
			RedisAccess.INSTANCE.disconnect();
		};
		if (LinkSpigotBungee.Provider.link.isEnabled())
			LinkSpigotBungee.Provider.link.launchAsync(run);
		else
			run.run();
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
				if (!modificationReceive.containsKey(uuid))
					return;
				modificationReceive.remove(uuid);
				callable.accept(false);
			}, 4, TimeUnit.SECONDS);
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
		if (RedisAccess.INSTANCE == null)
			return;
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

	public static void sendServerSwitch(Player p, String serverName) {
		try (Jedis jedis = RedisAccess.INSTANCE.connect()) {
			jedis.publish(RedisChannel.SPIGOT_PLAYER_SWITCH_SERVER2.name(), p.getName() + ":" + serverName);
		}
		RedisAccess.INSTANCE.disconnect();
	}

	public static void sendError(String stackTrace) {
		if (!errorsEnabled)
			return;
		String serverName = OlympaCore.getInstance().getServerName();
		try (Jedis jedis = RedisAccess.INSTANCE.connect()) {
			jedis.publish(RedisChannel.SPIGOT_RECEIVE_ERROR.name(), serverName + ":" + stackTrace);
		}
		RedisAccess.INSTANCE.disconnect();
	}

	public static boolean sendReport(OlympaReport report) {
		long i;
		try (Jedis jedis = RedisAccess.INSTANCE.connect()) {
			i = jedis.publish(RedisChannel.SPIGOT_REPORT_SEND.name(), new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().toJson(report));
		}
		RedisAccess.INSTANCE.disconnect();
		return i != 0;
	}
}
