package fr.olympa.api.provider;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.player.OlympaAccount;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.player.OlympaPlayerInformations;
import fr.olympa.api.player.OlympaPlayerProvider;
import fr.olympa.api.sql.MySQL;
import fr.olympa.api.utils.GsonCustomizedObjectTypeAdapter;
import fr.olympa.core.spigot.OlympaCore;
import redis.clients.jedis.Jedis;

public class AccountProvider implements OlympaAccount {

	private static String REDIS_KEY = "player:";
	private static int cachePlayer = 60;
	private static Map<UUID, OlympaPlayer> cache = new HashMap<>();
	public static Map<UUID, Consumer<? super Boolean>> modificationReceive = new HashMap<>();
	private static Map<Long, OlympaPlayerInformations> cachedInformations = new HashMap<>();

	public static Class<? extends OlympaPlayer> playerClass = OlympaPlayerObject.class;
	public static OlympaPlayerProvider playerProvider = OlympaPlayerObject::new;
	private static String providerTableName = null;

	public static <T extends OlympaPlayer> T get(String name) throws SQLException {
		OlympaPlayer olympaPlayer = AccountProvider.getFromCache(name);
		if (olympaPlayer == null) {
			// olympaPlayer = AccountProvider.getFromRedis(name);
			// if (olympaPlayer == null) {
			olympaPlayer = AccountProvider.getFromDatabase(name);
			// }
		}
		return (T) olympaPlayer;
	}

	public static <T extends OlympaPlayer> T get(UUID uuid) {
		return (T) cache.get(uuid);
	}

	private static OlympaPlayer getFromCache(String name) {
		return cache.values().stream().filter(p -> p.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
	}

	public static OlympaPlayer getFromDatabase(String name) throws SQLException {
		return MySQL.getPlayer(name);
	}

	public static OlympaPlayer getFromDatabase(UUID uuid) throws SQLException {
		return MySQL.getPlayer(uuid);
	}

	/*
	 * public static OlympaPlayer getFromRedis(String name) { return
	 * getFromRedis(name, false); }
	 *
	 * public static OlympaPlayer getFromRedis(String name, boolean cachePersist) {
	 * OlympaPlayer olympaPlayer = null;
	 *
	 * try (Jedis jedis = RedisAccess.INSTANCE.newConnection()) { olympaPlayer =
	 * jedis.hgetAll(name).entrySet().stream().map(entry ->
	 * GsonCustomizedObjectTypeAdapter.GSON.fromJson(entry.getValue(),
	 * playerClass)).filter(p ->
	 * p.getName().equalsIgnoreCase(name)).findFirst().orElse(null); if
	 * (cachePersist) { jedis.persist(REDIS_KEY + olympaPlayer.getUniqueId()); } }
	 * RedisAccess.INSTANCE.closeResource(); return olympaPlayer; }
	 */

	public synchronized static OlympaPlayerInformations getPlayerInformations(long id) {
		OlympaPlayerInformations info = cachedInformations.get(id);
		if (info == null) {
			info = MySQL.getPlayerInformations(id);
			cachedInformations.put(id, info);
		}
		return info;
	}

	public synchronized static OlympaPlayerInformations getPlayerInformations(OlympaPlayer player) {
		OlympaPlayerInformations info = cachedInformations.get(player.getId());
		if (info == null) {
			info = new OlympaPlayerInformationsObject(player.getId(), player.getName(), player.getUniqueId());
			cachedInformations.put(player.getId(), info);
		}
		return info;
	}

	public static String getPlayerProviderTableName() {
		return providerTableName;
	}

	public static void setPlayerProvider(Class<? extends OlympaPlayerObject> playerClass, OlympaPlayerProvider supplier, String pluginName, Map<String, String> columns) {
		try {
			providerTableName = "`" + pluginName.toLowerCase() + "_players`";
			MySQL.setDatasTable(providerTableName, columns);
			AccountProvider.playerClass = playerClass;
			playerProvider = supplier;
		} catch (SQLException e) {
			e.printStackTrace();
			providerTableName = null;
		}
	}

	RedisAccess redisAccesss;

	UUID uuid;

	public AccountProvider(UUID uuid) {
		this.uuid = uuid;
		redisAccesss = RedisAccess.INSTANCE;
	}

	public void accountExpire() {
		try (Jedis jedis = redisAccesss.newConnection()) {
			jedis.expire(getKey(), cachePlayer);
		}
		redisAccesss.closeResource();
	}

	public void accountPersist() {
		try (Jedis jedis = redisAccesss.newConnection()) {
			jedis.persist(getKey());
		}
		redisAccesss.disconnect();
	}

	public OlympaPlayer createNew(OlympaPlayer olympaPlayer) throws SQLException {
		olympaPlayer.addGroup(OlympaGroup.PLAYER);
		olympaPlayer.setId(MySQL.createPlayer(olympaPlayer));
		return olympaPlayer;
	}

	@Override
	public OlympaPlayer createOlympaPlayer(String name, String ip) {
		return playerProvider.create(uuid, name, ip);
	}

	public OlympaPlayer fromDb() throws SQLException {
		return MySQL.getPlayer(uuid);
	}

	@Override
	public OlympaPlayer get() throws SQLException {
		OlympaPlayer olympaPlayer = this.getFromCache();
		if (olympaPlayer == null) {
			olympaPlayer = getFromRedis();
			if (olympaPlayer == null) {
				return fromDb();
			}
		}
		return olympaPlayer;
	}

	public OlympaPlayer getFromCache() {
		return cache.get(uuid);
	}

	public OlympaPlayer getFromRedis() {
		String json = null;
		try (Jedis jedis = redisAccesss.newConnection()) {
			json = jedis.get(getKey());
		}
		redisAccesss.closeResource();

		if (json == null || json.isEmpty()) {
			return null;
		}
		return GsonCustomizedObjectTypeAdapter.GSON.fromJson(json, playerClass);
	}

	private String getKey() {
		return REDIS_KEY + uuid.toString();
	}

	public void removeFromCache() {
		cache.remove(uuid);
	}

	public void removeFromRedis() {
		try (Jedis jedis = redisAccesss.newConnection()) {
			jedis.del(getKey());
		}
		redisAccesss.closeResource();
	}

	public void saveToCache(OlympaPlayer olympaPlayer) {
		cache.put(uuid, olympaPlayer);
	}

	@Override
	public void saveToDb(OlympaPlayer olympaPlayer) {
		LinkSpigotBungee.Provider.link.launchAsync(() -> MySQL.savePlayer(olympaPlayer));
	}

	@Override
	public void saveToRedis(OlympaPlayer olympaPlayer) {
		LinkSpigotBungee.Provider.link.launchAsync(() -> {
			try (Jedis jedis = redisAccesss.newConnection()) {
				jedis.set(getKey(), GsonCustomizedObjectTypeAdapter.GSON.toJson(olympaPlayer));
			}
			redisAccesss.closeResource();
		});
	}

	public void sendModifications(OlympaPlayer olympaPlayer) {
		LinkSpigotBungee.Provider.link.launchAsync(() -> {
			try (Jedis jedis = redisAccesss.newConnection()) {
				jedis.publish("OlympaPlayer", GsonCustomizedObjectTypeAdapter.GSON.toJson(olympaPlayer));
			}
			redisAccesss.closeResource();
		});
	}

	/*
	 * Only Spigot
	 */
	public void sendModifications(OlympaPlayer olympaPlayer, Consumer<? super Boolean> done) {
		this.sendModifications(olympaPlayer);
		modificationReceive.put(olympaPlayer.getUniqueId(), done);
		OlympaCore.getInstance().getTask().runTaskLater("waitModifications" + uuid.toString(), () -> {
			Consumer<? super Boolean> callable = modificationReceive.get(uuid);
			callable.accept(false);
			modificationReceive.remove(uuid);
		}, 3 * 20);
	}

	public void sendModificationsReceive() {
		LinkSpigotBungee.Provider.link.launchAsync(() -> {
			try (Jedis jedis = redisAccesss.newConnection()) {
				jedis.publish("OlympaPlayerReceive", uuid.toString());
			}
			redisAccesss.closeResource();
		});
	}
}
