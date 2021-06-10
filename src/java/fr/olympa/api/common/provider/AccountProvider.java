package fr.olympa.api.common.provider;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.utils.Utils;
import redis.clients.jedis.Jedis;

public class AccountProvider extends AccountProviderAPI {

	public static AccountProviderGetter getter() {
		return (AccountProviderGetter) getter;
	}

	public AccountProvider(UUID uuid) {
		super(uuid);
	}

	public void accountExpire() {
		try (Jedis jedis = redisAccesss.connect()) {
			jedis.expire(getKey(), DELAY_FOR_CACHE_PLAYER);
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
				olympaPlayer.getGroupsToString(),
				olympaPlayer.getPassword(),
				olympaPlayer.getIp(),
				new Date(olympaPlayer.getFirstConnection() * 1000),
				new Timestamp(olympaPlayer.getLastConnection() * 1000));
		resultSet.next();
		olympaPlayer.setId(resultSet.getInt("id"));
		resultSet.close();
		return olympaPlayer;
	}
	/*
	 * Inherited by AccountProviderAPI
	 */
	/*@Override
	public OlympaPlayer fromDb() throws SQLException {
		return playerSQL.getPlayer(uuid);
	}*/

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
}
