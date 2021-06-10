package fr.olympa.api.common.provider;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import org.apache.commons.lang.Validate;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.common.player.OlympaAccount;
import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.common.player.OlympaPlayerInformations;
import fr.olympa.api.common.player.OlympaPlayerProvider;
import fr.olympa.api.common.redis.RedisAccess;
import fr.olympa.api.common.sql.SQLColumn;
import fr.olympa.api.common.sql.SQLTable;
import fr.olympa.api.sql.MySQL;
import fr.olympa.api.utils.CacheStats;
import fr.olympa.api.utils.GsonCustomizedObjectTypeAdapter;
import redis.clients.jedis.Jedis;

public class AccountProviderGetter implements AccountProviderGetterInterface {

	private static Map<Long, OlympaPlayerInformations> cachedInformations = new HashMap<>();
	public static OlympaPlayerProvider pluginPlayerProvider = OlympaPlayerObject::new;

	private Class<? extends OlympaPlayer> playerClass = OlympaPlayerObject.class;
	private SQLTable<? extends OlympaPlayer> pluginPlayerTable = null;
	private MySQL playerSQL;

	public AccountProviderGetter(MySQL sqlClass) throws SQLException {
		CacheStats.addDebugMap("PLAYERS", OlympaAccount.cache);
		CacheStats.addDebugMap("PLAYERS_INFO", cachedInformations);
		playerSQL = sqlClass;
	}

	@Override
	public MySQL getSQL() {
		return playerSQL;
	}

	@Override
	public Class<? extends OlympaPlayer> getPlayerClass() {
		return playerClass;
	}

	@Override
	@SuppressWarnings("unchecked")
	@Nullable
	public <T extends OlympaPlayer> T get(String name) throws SQLException {
		OlympaPlayer olympaPlayer = getFromCache(name);
		if (olympaPlayer == null)
			olympaPlayer = getFromRedis(name);
		if (olympaPlayer == null)
			olympaPlayer = getFromDatabase(name);
		return (T) olympaPlayer;
	}

	@Override
	@SuppressWarnings("unchecked")
	@Nullable
	public <T extends OlympaPlayer> T get(long id) throws SQLException {
		OlympaPlayer olympaPlayer = getFromCache(id);
		if (olympaPlayer == null)
			olympaPlayer = getFromRedis(id);
		if (olympaPlayer == null)
			olympaPlayer = getFromDatabase(id);
		return (T) olympaPlayer;
	}

	@Override
	@SuppressWarnings("unchecked")
	@Nullable
	public <T extends OlympaPlayer> T get(UUID uuid) {
		return (T) OlympaAccount.cache.get(uuid);
	}

	@Override
	public Collection<OlympaPlayer> getAll() {
		return OlympaAccount.cache.values();
	}

	@Override
	public Collection<OlympaPlayerInformations> getAllPlayersInformations() {
		return cachedInformations.values();
	}

	@Override
	public List<OlympaPlayerInformations> getAllConnectedPlayersInformations() {
		List<OlympaPlayerInformations> list = new ArrayList<>();
		OlympaAccount.cache.forEach((uuid, olympaPlayer) -> {
			if (olympaPlayer.isConnected())
				list.add(getPlayerInformations(olympaPlayer.getId()));
		});
		return list;
	}

	@Nullable
	private OlympaPlayer getFromCache(String name) {
		return OlympaAccount.cache.values().stream().filter(p -> p.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
	}

	@Nullable
	private OlympaPlayer getFromCache(long id) {
		return OlympaAccount.cache.values().stream().filter(p -> p.getId() == id).findFirst().orElse(null);
	}

	@Override
	@Nullable
	public OlympaPlayer getFromDatabase(String name) throws SQLException {
		return playerSQL.getPlayer(name);
	}

	@Override
	@Nullable
	public OlympaPlayer getFromDatabase(UUID uuid) throws SQLException {
		return playerSQL.getPlayer(uuid);
	}

	@Override
	@Nullable
	public OlympaPlayer getFromDatabase(long id) throws SQLException {
		return playerSQL.getPlayer(id);
	}

	@Override
	@Nullable
	public OlympaPlayer getFromRedis(String name) {
		OlympaPlayer olympaPlayer = null;
		try (Jedis jedis = RedisAccess.INSTANCE.connect()) {
			olympaPlayer = jedis.keys("player:*").stream().filter(v -> v.contains(name)).map(v -> GsonCustomizedObjectTypeAdapter.GSON.fromJson(v, playerClass))
					.filter(p -> p.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
		}
		RedisAccess.INSTANCE.disconnect();
		return olympaPlayer;
	}

	@Override
	@Nullable
	public OlympaPlayer getFromRedis(long id) {
		OlympaPlayer olympaPlayer = null;
		try (Jedis jedis = RedisAccess.INSTANCE.connect()) {
			olympaPlayer = jedis.keys("player:*").stream().filter(v -> v.contains(String.valueOf(id))).map(value -> GsonCustomizedObjectTypeAdapter.GSON.fromJson(value, playerClass))
					.filter(p -> p.getId() == id).findFirst().orElse(null);
		}
		RedisAccess.INSTANCE.disconnect();
		return olympaPlayer;
	}

	@Override
	@Nullable
	public synchronized OlympaPlayerInformations getPlayerInformations(long id) {
		OlympaPlayerInformations info = cachedInformations.get(id);
		if (info == null)
			try {
				info = playerSQL.getPlayerInformations(id);
				cachedInformations.put(id, info);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		return info;
	}

	@Override
	@Nullable
	public synchronized OlympaPlayerInformations getPlayerInformations(UUID uuid) {
		OlympaPlayerInformations info = cachedInformations.values().stream().filter(opi -> opi.getUUID().equals(uuid)).findFirst().orElse(null);
		if (info == null)
			try {
				info = playerSQL.getPlayerInformations(uuid);
				if (info != null)
					cachedInformations.put(info.getId(), info);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		return info;
	}

	@Override
	@Nullable
	public synchronized OlympaPlayerInformations getPlayerInformations(String name) {
		OlympaPlayerInformations info = cachedInformations.values().stream().filter(opi -> opi.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
		if (info == null)
			try {
				info = playerSQL.getPlayerInformations(name);
				if (info != null)
					cachedInformations.put(info.getId(), info);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		return info;
	}

	@Override
	@Nullable
	public synchronized OlympaPlayerInformations getPlayerInformations(OlympaPlayer player) {
		OlympaPlayerInformations info = cachedInformations.get(player.getId());
		if (info == null) {
			info = new OlympaPlayerInformationsObject(player.getId(), player.getName(), player.getUniqueId());
			cachedInformations.put(player.getId(), info);
		}
		return info;
	}

	@Override
	public SQLTable<? extends OlympaPlayer> getPluginPlayerTable() {
		return pluginPlayerTable;
	}

	@Override
	public <T extends OlympaPlayer> void setPlayerProvider(Class<T> playerClass, OlympaPlayerProvider provider, String pluginName, List<SQLColumn<T>> columns) {
		Validate.isTrue(columns.stream().noneMatch(SQLColumn::isNotDefault), "All columns must have default values");
		try {
			List<SQLColumn<T>> newColumns = new ArrayList<>(columns.size() + 1);
			newColumns.add(new SQLColumn<T>("player_id", "BIGINT NOT NULL", Types.BIGINT).setPrimaryKey(T::getId));
			newColumns.addAll(columns);
			pluginPlayerTable = new SQLTable<>(pluginName.toLowerCase() + "_players", newColumns).createOrAlter();
			//MySQL.setDatasTable(providerTableName, columns);
			this.playerClass = playerClass;
			pluginPlayerProvider = provider;
		} catch (SQLException e) {
			e.printStackTrace();
			pluginPlayerTable = null;
		}
	}

	@Override
	public boolean loadPlayerDatas(OlympaPlayer player) throws SQLException {
		if (pluginPlayerTable == null)
			return false;
		ResultSet resultSet = pluginPlayerTable.get(player.getId());
		if (resultSet.next()) {
			player.loadDatas(resultSet);
			player.loaded();
			return false;
		}
		pluginPlayerTable.insert(player.getId());
		LinkSpigotBungee.Provider.link.sendMessage("Données créées pour le joueur §6%s", player.getName());
		player.loaded();
		return true;
	}

	@Override
	public Map<Long, OlympaPlayerInformations> getCachedInformations() {
		return cachedInformations;
	}
}
