package fr.olympa.api.provider;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.Validate;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.player.OlympaAccount;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.player.OlympaPlayerInformations;
import fr.olympa.api.player.OlympaPlayerProvider;
import fr.olympa.api.redis.RedisAccess;
import fr.olympa.api.sql.MySQL;
import fr.olympa.api.sql.SQLColumn;
import fr.olympa.api.sql.SQLTable;
import fr.olympa.api.utils.GsonCustomizedObjectTypeAdapter;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.spigot.OlympaCore;
import redis.clients.jedis.Jedis;

public class AccountProvider implements OlympaAccount {

	private static String REDIS_KEY = "player:";
	private static int cachePlayer = 60;
	private static Map<UUID, OlympaPlayer> cache = new HashMap<>();
	private static Map<Long, OlympaPlayerInformations> cachedInformations = new HashMap<>();

	public static Class<? extends OlympaPlayer> playerClass = OlympaPlayerObject.class;
	public static OlympaPlayerProvider pluginPlayerProvider = OlympaPlayerObject::new;
	private static SQLTable<? extends OlympaPlayer> pluginPlayerTable = null;
	
	private static SQLTable<OlympaPlayerObject> olympaPlayerTable;

	public static void init(MySQL mysql) throws SQLException {
		olympaPlayerTable = new SQLTable<>(mysql.getTable(), OlympaPlayerObject.COLUMNS).createOrAlter();
	}
	
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

	public static Collection<OlympaPlayer> getAll() {
		return cache.values();
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
			olympaPlayer = jedis.keys("*").stream().filter(v -> v.contains(name)).map(v -> GsonCustomizedObjectTypeAdapter.GSON.fromJson(v, playerClass))
					.filter(p -> p.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
		}
		RedisAccess.INSTANCE.disconnect();
		return olympaPlayer;
	}

	public static OlympaPlayer getFromRedis(long id) {
		OlympaPlayer olympaPlayer = null;
		try (Jedis jedis = RedisAccess.INSTANCE.connect()) {
			olympaPlayer = jedis.keys("*").stream().filter(v -> v.contains(String.valueOf(id))).map(value -> GsonCustomizedObjectTypeAdapter.GSON.fromJson(value, playerClass))
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

	public synchronized static OlympaPlayerInformations getPlayerInformations(UUID uuid) {
		OlympaPlayerInformations info = cachedInformations.values().stream().filter(opi -> opi.getUUID().equals(uuid)).findFirst().orElse(null);
		if (info == null) {
			info = MySQL.getPlayerInformations(uuid);
			cachedInformations.put(info.getId(), info);
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

	public static SQLTable<? extends OlympaPlayer> getPluginPlayerTable() {
		return pluginPlayerTable;
	}

	public static <T extends OlympaPlayer> void setPlayerProvider(Class<T> playerClass, OlympaPlayerProvider provider, String pluginName, List<SQLColumn<T>> columns) {
		Validate.isTrue(columns.stream().noneMatch(SQLColumn::isNotDefault), "All columns must have default values");
		try {
			List<SQLColumn<T>> newColumns = new ArrayList<>(columns.size() + 1);
			newColumns.add(new SQLColumn<T>("player_id", "BIGINT NOT NULL", Types.BIGINT).setPrimaryKey(T::getId));
			newColumns.addAll(columns);
			pluginPlayerTable = new SQLTable<>(pluginName.toLowerCase() + "_players", newColumns).createOrAlter();
			//MySQL.setDatasTable(providerTableName, columns);
			AccountProvider.playerClass = playerClass;
			pluginPlayerProvider = provider;
		} catch (SQLException e) {
			e.printStackTrace();
			pluginPlayerTable = null;
		}
	}
	
	public static boolean loadPlayerDatas(OlympaPlayer player) throws SQLException {
		ResultSet resultSet = pluginPlayerTable.get(player.getId());
		if (resultSet.next()) {
			player.loadDatas(resultSet);
			return false;
		}
		pluginPlayerTable.insert(player.getId());
		OlympaCore.getInstance().sendMessage("Données créées pour le joueur §6" + player.getName());
		return true;
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
		ResultSet resultSet = olympaPlayerTable.insert(
				olympaPlayer.getName(),
				Utils.getUUIDString(olympaPlayer.getUniqueId()),
				Utils.getUUIDString(olympaPlayer.getPremiumUniqueId()),
				olympaPlayer.getGroupsToString(),
				olympaPlayer.getPassword(),
				new Date(olympaPlayer.getFirstConnection() * 1000),
				new Timestamp(olympaPlayer.getLastConnection() * 1000));
		resultSet.next();
		olympaPlayer.setId(resultSet.getInt("id"));
		resultSet.close();
		return olympaPlayer;
	}

	@Override
	public OlympaPlayer createOlympaPlayer(String name, String ip) {
		OlympaPlayer newOlympaPlayer = pluginPlayerProvider.create(uuid, name, ip);
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

	/*@Override
	public void saveToDb(OlympaPlayer olympaPlayer) {
		LinkSpigotBungee.Provider.link.launchAsync(() -> MySQL.savePlayer(olympaPlayer));
	}*/

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
