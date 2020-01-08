package fr.olympa.api.provider;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import org.bukkit.entity.Player;

import com.google.gson.Gson;

import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.permission.OlympaAccount;
import fr.olympa.api.provider.OlympaPlayerObject;
import fr.olympa.api.sql.MySQL;
import fr.olympa.core.spigot.OlympaCore;
import redis.clients.jedis.Jedis;

public class AccountProvider implements OlympaAccount {

	private static String REDIS_KEY = "player:";
	public static Map<UUID, Consumer<? super Boolean>> modificationReceive = new HashMap<>();
	private static Map<UUID, OlympaPlayer> cache = new HashMap<>();

	public static OlympaPlayer get(Player player) {
		return get(player.getUniqueId());
	}

	public static OlympaPlayer get(UUID uuid) {
		return cache.get(uuid);
	}

	public static OlympaPlayer getFromDatabase(String name) throws SQLException {
		return MySQL.getPlayer(name);
	}

	public static OlympaPlayer getFromDatabase(UUID uuid) throws SQLException {
		return MySQL.getPlayer(uuid);
	}

	RedisAccess redisAccesss;

	UUID uuid;

	public AccountProvider(UUID uuid) {
		this.uuid = uuid;
		this.redisAccesss = RedisAccess.INSTANCE;
	}

	public void accountExpire(OlympaPlayer olympaPlayer) {
		try (Jedis jedis = this.redisAccesss.connect()) {
			jedis.expire(this.getKey(), 60);
		}
		this.redisAccesss.closeResource();
	}

	public boolean createNew(OlympaPlayer olympaPlayer) {
		return MySQL.createPlayer(olympaPlayer);
	}

	@Override
	public OlympaPlayerObject createOlympaPlayer(String name, String ip) {
		return new OlympaPlayerObject(this.uuid, name, ip);
	}

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

	public void removeFromCache() {
		cache.remove(this.uuid);
	}

	public void saveToCache(OlympaPlayer olympaPlayer) {
		cache.put(this.uuid, olympaPlayer);
	}

	@Override
	public void saveToDb(OlympaPlayer olympaPlayer) {
		OlympaCore.getInstance().getTask().runTaskAsynchronously(() -> MySQL.savePlayer(olympaPlayer));
	}

	@Override
	public void saveToRedis(OlympaPlayer olympaPlayer) {
		OlympaCore.getInstance().getTask().runTaskAsynchronously(() -> {
			try (Jedis jedis = this.redisAccesss.connect()) {
				jedis.set(this.getKey(), new Gson().toJson(olympaPlayer));
			}
			this.redisAccesss.closeResource();
		});
	}

	/*
	 * @Override public void sendModifications(OlympaPlayer olympaPlayer) {
	 * OlympaCore.getInstance().getTask().runTaskAsynchronously(() -> { try (Jedis
	 * jedis = this.redisAccesss.connect()) { jedis.publish("OlympaPlayer", new
	 * Gson().toJson(olympaPlayer)); } this.redisAccesss.closeResource(); }); }
	 *
	 * @Override public void sendModifications(OlympaPlayer olympaPlayer, Consumer<?
	 * super Boolean> done) { this.sendModifications(olympaPlayer);
	 * modificationReceive.put(olympaPlayer.getUniqueId(), done);
	 * OlympaCore.getInstance().getTask().runTaskLater("waitModifications" +
	 * olympaPlayer.getUniqueId().toString(), () -> { Consumer<? super Boolean>
	 * callable = modificationReceive.get(this.uuid); callable.accept(false);
	 * modificationReceive.remove(this.uuid); }, 5 * 20); }
	 *
	 * @Override public void sendModificationsReceive() {
	 * OlympaCore.getInstance().getTask().runTaskAsynchronously(() -> { try (Jedis
	 * jedis = this.redisAccesss.connect()) { jedis.publish("OlympaPlayerReceive",
	 * this.uuid.toString()); } this.redisAccesss.closeResource(); }); }
	 */
}
