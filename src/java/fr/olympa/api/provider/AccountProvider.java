package fr.olympa.api.provider;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.player.OlympaAccount;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.player.OlympaPlayerInformations;
import fr.olympa.api.player.OlympaPlayerProvider;
import fr.olympa.api.sql.MySQL;
import fr.olympa.api.utils.GsonCustomizedObjectTypeAdapter;
import redis.clients.jedis.Jedis;

public class AccountProvider implements OlympaAccount {

	private static String REDIS_KEY = "player:";
	private static int cachePlayer = 60;
	private static Map<UUID, OlympaPlayer> cache = new HashMap<>();
	private static Map<Long, OlympaPlayerInformations> cachedInformations = new HashMap<>();

	public static Class<? extends OlympaPlayer> playerClass = OlympaPlayerObject.class;
	public static OlympaPlayerProvider playerProvider = OlympaPlayerObject::new;
	private static String providerTableName = null;

	@SuppressWarnings("unchecked")
	public static <T extends OlympaPlayer> T get(String name) throws SQLException {
		OlympaPlayer olympaPlayer = AccountProvider.getFromCache(name);
		if (olympaPlayer == null)
			olympaPlayer = AccountProvider.getFromRedis(name);
		if (olympaPlayer == null)
			olympaPlayer = AccountProvider.getFromDatabase(name);
		return (T) olympaPlayer;
	}

	@SuppressWarnings("unchecked")
	public static <T extends OlympaPlayer> T get(long id) throws SQLException {
		OlympaPlayer olympaPlayer = AccountProvider.getFromCache(id);
		if (olympaPlayer == null)
			olympaPlayer = AccountProvider.getFromRedis(id);
		if (olympaPlayer == null)
			olympaPlayer = AccountProvider.getFromDatabase(id);
		return (T) olympaPlayer;
	}

	@SuppressWarnings("unchecked")
	public static <T extends OlympaPlayer> T get(UUID uuid) {
		return (T) cache.get(uuid);
	}

	private static OlympaPlayer getFromCache(String name) {
		return cache.values().stream().filter(p -> p.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
	}

	private static OlympaPlayer getFromCache(long id) {
		return cache.values().stream().filter(p -> p.getId() == id).findFirst().orElse(null);
	}

	public static OlympaPlayer getFromDatabase(String name) throws SQLException {
		return MySQL.getPlayer(name);
	}

	public static OlympaPlayer getFromDatabase(UUID uuid) throws SQLException {
		return MySQL.getPlayer(uuid);
	}

	public static OlympaPlayer getFromDatabase(long id) throws SQLException {
		return MySQL.getPlayer(id);
	}

	public static OlympaPlayer getFromRedis(String name) {
		OlympaPlayer olympaPlayer = null;
		try (Jedis jedis = RedisAccess.INSTANCE.connect()) {
			olympaPlayer = jedis.hgetAll(name).entrySet().stream().map(entry -> GsonCustomizedObjectTypeAdapter.GSON.fromJson(entry.getValue(), playerClass))
					.filter(p -> p.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
		}
		RedisAccess.INSTANCE.disconnect();
		return olympaPlayer;
	}

	public static OlympaPlayer getFromRedis(long id) {
		OlympaPlayer olympaPlayer = null;
		try (Jedis jedis = RedisAccess.INSTANCE.connect()) {
			olympaPlayer = jedis.hgetAll(String.valueOf(id)).entrySet().stream().map(entry -> GsonCustomizedObjectTypeAdapter.GSON.fromJson(entry.getValue(), playerClass))
					.filter(p -> p.getId() == id).findFirst().orElse(null);
		}
		RedisAccess.INSTANCE.disconnect();
		return olympaPlayer;
	}

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

	public static void setPlayerProvider(Class<? extends OlympaPlayerObject> playerClass, OlympaPlayerProvider provider, String pluginName, Map<String, String> columns) {
		try {
			providerTableName = pluginName.toLowerCase() + "_players";
			MySQL.setDatasTable(providerTableName, columns);
			AccountProvider.playerClass = playerClass;
			playerProvider = provider;
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
		try (Jedis jedis = redisAccesss.connect()) {
			jedis.expire(getKey(), cachePlayer);
		}
		redisAccesss.disconnect();
	}

	public void accountPersist() {
		try (Jedis jedis = redisAccesss.connect()) {
			jedis.persist(getKey());
		}
		redisAccesss.disconnect();
	}

	public OlympaPlayer createNew(OlympaPlayer olympaPlayer) throws SQLException {
		olympaPlayer.setId(MySQL.createPlayer(olympaPlayer));
		return olympaPlayer;
	}

	@Override
	public OlympaPlayer createOlympaPlayer(String name, String ip) {
		OlympaPlayer newOlympaPlayer = playerProvider.create(uuid, name, ip);
		newOlympaPlayer.setGroup(OlympaGroup.PLAYER);
		return newOlympaPlayer;
	}

	public OlympaPlayer fromDb() throws SQLException {
		return MySQL.getPlayer(uuid);
	}

	@Override
	public OlympaPlayer get() throws SQLException {
		OlympaPlayer olympaPlayer = this.getFromCache();
		if (olympaPlayer == null) {
			olympaPlayer = getFromRedis();
			if (olympaPlayer == null)
				return fromDb();
		}
		return olympaPlayer;
	}

	public OlympaPlayer getFromCache() {
		return cache.get(uuid);
	}

	public OlympaPlayer getFromRedis() {
		String json = null;
		try (Jedis jedis = redisAccesss.connect()) {
			json = jedis.get(getKey());
		}
		redisAccesss.disconnect();

		if (json == null || json.isEmpty())
			return null;
		return GsonCustomizedObjectTypeAdapter.GSON.fromJson(json, playerClass);
	}

	private String getKey() {
		return REDIS_KEY + uuid.toString();
	}

	public void removeFromCache() {
		cache.remove(uuid);
	}

	public void removeFromRedis() {
		try (Jedis jedis = redisAccesss.connect()) {
			jedis.del(getKey());
		}
		redisAccesss.disconnect();
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
			try (Jedis jedis = redisAccesss.connect()) {
				jedis.set(getKey(), GsonCustomizedObjectTypeAdapter.GSON.toJson(olympaPlayer));
			}
			redisAccesss.disconnect();
		});
	}
}
