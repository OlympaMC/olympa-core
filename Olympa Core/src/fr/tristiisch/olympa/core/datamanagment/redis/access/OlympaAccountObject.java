package fr.tristiisch.olympa.core.datamanagment.redis.access;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import com.google.gson.Gson;

import fr.tristiisch.olympa.api.objects.OlympaPlayer;
import fr.tristiisch.olympa.api.permission.OlympaAccount;
import fr.tristiisch.olympa.api.permission.OlympaPlayerObject;
import fr.tristiisch.olympa.api.task.TaskManager;
import fr.tristiisch.olympa.core.datamanagment.redis.RedisAccess;
import fr.tristiisch.olympa.core.datamanagment.sql.MySQL;
import redis.clients.jedis.Jedis;

public class OlympaAccountObject implements OlympaAccount {

	private static String REDIS_KEY = "player:";
	public static Map<UUID, Consumer<? super Boolean>> modificationReceive = new HashMap<>();
	public static Map<UUID, OlympaPlayer> cache = new HashMap<>();

	RedisAccess redisAccesss;
	UUID uuid;

	public OlympaAccountObject(final UUID uuid) {
		this.uuid = uuid;
		this.redisAccesss = RedisAccess.INSTANCE;
	}

	@Override
	public void accountExpire(OlympaPlayer olympaPlayer) {
		try (Jedis jedis = this.redisAccesss.connect()) {
			jedis.expire(this.getKey(), 60);
		}
		this.redisAccesss.closeResource();
	}

	@Override
	public boolean createNew(OlympaPlayer olympaPlayer, String name, final String ip) {
		olympaPlayer = new OlympaPlayerObject(this.uuid, name, ip);
		return MySQL.createPlayer(olympaPlayer);
	}

	@Override
	public OlympaPlayer fromDb() throws SQLException {
		return MySQL.getPlayer(this.uuid);
	}

	@Override
	public OlympaPlayer get() throws SQLException {
		OlympaPlayer olympaPlayer = this.getFromCache();
		if (olympaPlayer == null) {
			olympaPlayer = this.getFromRedis();
			if (olympaPlayer == null) {
				return this.fromDb();
			}
		}
		return olympaPlayer;
	}

	@Override
	public OlympaPlayer getFromCache() {
		return cache.get(this.uuid);
	}

	protected OlympaPlayer getFromRedis() {
		String json = null;

		try (Jedis jedis = this.redisAccesss.connect()) {
			json = jedis.get(this.getKey());
			if (json != null) {
				jedis.persist(this.getKey());
			}
		}
		this.redisAccesss.closeResource();

		if (json == null || json.isEmpty()) {
			return null;
		}
		return new Gson().fromJson(json, OlympaPlayerObject.class);
	}

	private String getKey() {
		return REDIS_KEY + this.uuid.toString();
	}

	@Override
	public void removeFromCache() {
		cache.remove(this.uuid);
	}

	@Override
	public void saveToCache(OlympaPlayer olympaPlayer) {
		cache.put(this.uuid, olympaPlayer);
	}

	@Override
	public void saveToDb(OlympaPlayer olympaPlayer) {
		TaskManager.runTaskAsynchronously(() -> MySQL.savePlayer(olympaPlayer));
	}

	@Override
	public void saveToRedis(final OlympaPlayer olympaPlayer) {
		TaskManager.runTaskAsynchronously(() -> {
			try (Jedis jedis = this.redisAccesss.connect()) {
				jedis.set(this.getKey(), new Gson().toJson(olympaPlayer));
			}
			this.redisAccesss.closeResource();
		});
	}

	@Override
	public void sendModifications(final OlympaPlayer olympaPlayer) {
		TaskManager.runTaskAsynchronously(() -> {
			try (Jedis jedis = this.redisAccesss.connect()) {
				jedis.publish("OlympaPlayer", new Gson().toJson(olympaPlayer));
			}
			this.redisAccesss.closeResource();
		});
	}

	@Override
	public void sendModifications(final OlympaPlayer olympaPlayer, Consumer<? super Boolean> done) {
		this.sendModifications(olympaPlayer);
		modificationReceive.put(olympaPlayer.getUniqueId(), done);
		TaskManager.runTaskLater("waitModifications" + olympaPlayer.getUniqueId().toString(), () -> {
			Consumer<? super Boolean> callable = modificationReceive.get(this.uuid);
			callable.accept(false);
			modificationReceive.remove(this.uuid);
		}, 5 * 20);
	}

	@Override
	public void sendModificationsReceive() {
		TaskManager.runTaskAsynchronously(() -> {
			try (Jedis jedis = this.redisAccesss.connect()) {
				jedis.publish("OlympaPlayerReceive", this.uuid.toString());
			}
			this.redisAccesss.closeResource();
		});
	}
}