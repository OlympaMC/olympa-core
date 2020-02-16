package fr.olympa.api.sql;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.UUID;

import com.google.gson.Gson;

import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.objects.Gender;
import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.spigot.OlympaCore;

public class MySQL {

	static DbConnection dbConnection;
	static String tableName = "olympa.users";

	private static OlympaStatement insertPlayerStatement = new OlympaStatement("INSERT INTO " + tableName + " (`uuid-server`, `uuid`, `pseudo`, `ip`, `groups`, `created`, `last_connection`, `password`) VALUES (?, ?, ?, ?, ?, ?, ?, ?)", true);

	public static long createPlayer(OlympaPlayer olympaPlayer) throws SQLException {
		PreparedStatement statement = insertPlayerStatement.getStatement();
		int i = 1;
		statement.setString(i++, olympaPlayer.getUniqueId().toString());
		if (olympaPlayer.getPremiumUniqueId() != null) {
			statement.setString(i++, olympaPlayer.getPremiumUniqueId().toString());
		} else {
			statement.setString(i++, null);
		}
		statement.setString(i++, olympaPlayer.getName());
		statement.setString(i++, olympaPlayer.getIp());
		statement.setString(i++, olympaPlayer.getGroupsToString());
		statement.setDate(i++, new Date(olympaPlayer.getFirstConnection() * 1000L));
		statement.setTimestamp(i++, new Timestamp(olympaPlayer.getLastConnection() * 1000L));
		statement.setString(i++, olympaPlayer.getPassword());

		statement.executeUpdate();
		ResultSet resultSet = statement.getGeneratedKeys();
		resultSet.next();
		long id = resultSet.getLong("id");
		resultSet.close();
		return id;
	}

	public static Set<String> getAllPlayersNames() {
		Set<String> names = new HashSet<>();
		try {
			Statement state = dbConnection.getConnection().createStatement();
			ResultSet resultSet = state.executeQuery("SELECT `pseudo` FROM " + tableName);
			while (resultSet.next()) {
				names.add(resultSet.getString(1));
			}
			state.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return names;
	}

	/**
	 * Récupère le nom exacte d'un joueur dans la base de données à l'aide de son
	 * UUID
	 */
	public static String getNameFromUuid(UUID uuid) {
		List<Object> list = MySQL.selectTable("SELECT `pseudo` FROM " + tableName + " WHERE `uuid-server` = '" + uuid.toString() + "';");
		if (!list.isEmpty()) {
			return list.get(0).toString();
		}
		return null;
	}

	private static OlympaStatement getPlayerPluginDatas;
	private static OlympaStatement updatePlayerPluginDatas;
	private static int updatePlayerPluginDatasID;
	public static OlympaPlayer getOlympaPlayer(ResultSet resultSet) {
		try {
			String uuidPremiumString = resultSet.getString("uuid");
			UUID uuidPremium = null;
			if (uuidPremiumString != null) {
				uuidPremium = UUID.fromString(uuidPremiumString);
			}
			OlympaPlayer player = AccountProvider.playerProvider.create(UUID.fromString(resultSet.getString("uuid-server")), resultSet.getString("pseudo"), resultSet.getString("ip"));
			player.loadSavedDatas(
					resultSet.getLong("id"),
					uuidPremium,
					resultSet.getString("groups"),
					resultSet.getDate("created").getTime() / 1000L,
					resultSet.getTimestamp("last_connection").getTime() / 1000L,
					resultSet.getString("password"),
					resultSet.getString("email"),
					Gender.get(resultSet.getInt("gender")),
					resultSet.getString("name_history"),
					resultSet.getString("ip_history"));
			/*if (getPlayerPluginDatas != null) {
				OlympaCore.getInstance().getTask().runTaskAsynchronously(() -> {
					try {
						PreparedStatement statement = getPlayerPluginDatas.getStatement();
						statement.setLong(1, player.getId());
						ResultSet pluginSet = statement.executeQuery();
						if (pluginSet.next()) player.loadDatas(pluginSet);
						pluginSet.close();
					}catch (SQLException e) {
						e.printStackTrace();
					}
				});
			}*/
			return player;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void setDatasTable(String tableName, Map<String, String> columns) throws SQLException {
		StringJoiner creationJoiner = new StringJoiner(", ", "CREATE TABLE IF NOT EXISTS `" + tableName + "` (", ")");
		creationJoiner.add("`player_id` BIGINT NOT NULL");
		for (Entry<String, String> column : columns.entrySet()) {
			creationJoiner.add("`" + column.getKey() + "` " + column.getValue());
		}
		creationJoiner.add("PRIMARY KEY (`player_id`)");
		OlympaCore.getInstance().getDatabase().createStatement().executeUpdate(creationJoiner.toString());

		StringJoiner updateJoiner = new StringJoiner(", ", "UPDATE " + tableName + " SET ", " WHERE `player_id` = ?");
		for (String columnName : columns.keySet()) {
			creationJoiner.add("`" + columnName + "` = ?");
		}
		updatePlayerPluginDatas = new OlympaStatement(updateJoiner.toString());
		updatePlayerPluginDatasID = columns.size() + 1;

		getPlayerPluginDatas = new OlympaStatement("SELECT * FROM " + tableName + " WHERE `player_id` = ?");
	}

	/**
	 * Permet de récupérer les donnés d'un joueur dans la base de données grâce à
	 * son id
	 */
	public static OlympaPlayer getPlayer(int id) {
		try {
			Statement state = dbConnection.getConnection().createStatement();
			ResultSet resultSet = state.executeQuery("SELECT * FROM " + tableName + " WHERE id = '" + id + "'");
			if (resultSet.next()) {
				return getOlympaPlayer(resultSet);
			}
			state.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Permet de récupérer les donnés d'un joueur dans la base de données grâce à
	 * son pseudo
	 */
	public static OlympaPlayer getPlayer(String playerName) {
		try {
			Statement state = dbConnection.getConnection().createStatement();
			ResultSet resultSet = state.executeQuery("SELECT * FROM " + tableName + " WHERE `pseudo` = '" + playerName + "'");
			if (resultSet.next()) {
				return getOlympaPlayer(resultSet);
			}
			state.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Permet de récupérer les donnés d'un joueur dans la base de données grâce à
	 * son uuid
	 *
	 * @throws SQLException
	 */
	public static OlympaPlayer getPlayer(UUID playerUUID) throws SQLException {
		OlympaPlayer olympaPlayer = null;
		Statement state = dbConnection.getConnection().createStatement();
		ResultSet resultSet = state.executeQuery("SELECT * FROM " + tableName + " WHERE `uuid-server` = '" + playerUUID + "'");
		if (resultSet.next()) {
			olympaPlayer = getOlympaPlayer(resultSet);
		}
		state.close();
		return olympaPlayer;
	}

	public static OlympaPlayer getPlayerByName(String name) throws SQLException {
		OlympaPlayer olympaPlayer = null;
		Statement state = dbConnection.getConnection().createStatement();
		ResultSet resultSet = state.executeQuery("SELECT * FROM " + tableName + " WHERE `pseudo` = '" + name + "'");
		while (resultSet.next() && resultSet.getString(2).equalsIgnoreCase(name)) {
			olympaPlayer = getOlympaPlayer(resultSet);
		}
		state.close();
		return olympaPlayer;
	}

	/**
	 * Permet de récupérer les donnés d'un joueur dans la base de données grâce à
	 * son uuid premium
	 * @throws SQLException
	 */
	public static OlympaPlayer getPlayerByPremiumUuid(UUID premiumUuid) throws SQLException {
		OlympaPlayer olympaPlayer = null;
		Statement state = dbConnection.getConnection().createStatement();
		ResultSet resultSet = state.executeQuery("SELECT * FROM " + tableName + " WHERE `uuid` = '" + premiumUuid + "'");
		if (resultSet.next()) {
			olympaPlayer = getOlympaPlayer(resultSet);
		}
		state.close();
		return olympaPlayer;
	}

	/**
	 * Permet de récupérer les donnés d'un joueur dans la base de données grâce à
	 * son ts3databaseid
	 * @throws SQLException
	 */
	public static OlympaPlayer getPlayerByTs3Id(int ts3databaseid) throws SQLException {
		OlympaPlayer olympaPlayer = null;
		Statement state = dbConnection.getConnection().createStatement();
		ResultSet resultSet = state.executeQuery("SELECT * FROM " + tableName + " WHERE `ts3_id` = '" + ts3databaseid + "'");
		if (resultSet.next()) {
			olympaPlayer = getOlympaPlayer(resultSet);
		}
		state.close();
		return olympaPlayer;
	}

	public static List<OlympaPlayer> getPlayersByIp(String ip) {
		List<OlympaPlayer> olympaPlayers = new ArrayList<>();
		try {
			Statement state = dbConnection.getConnection().createStatement();
			ResultSet resultSet = state.executeQuery("SELECT * FROM " + tableName + " WHERE `ip` = '" + ip + "'");
			while (resultSet.next()) {
				olympaPlayers.add(getOlympaPlayer(resultSet));
			}
			state.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return olympaPlayers;
	}

	public static List<OlympaPlayer> getPlayersByIpHistory(String ipHistory) {
		List<OlympaPlayer> olympaPlayers = new ArrayList<>();
		try {
			Statement state = dbConnection.getConnection().createStatement();
			ResultSet resultSet = state.executeQuery("SELECT * FROM " + tableName + " WHERE `ip_history` LIKE '%" + ipHistory + "%'");
			while (resultSet.next()) {
				olympaPlayers.add(getOlympaPlayer(resultSet));
			}
			state.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return olympaPlayers;
	}

	public static List<OlympaPlayer> getPlayersByNameHistory(String nameHistory) {
		List<OlympaPlayer> olympaPlayers = new ArrayList<>();
		try {
			Statement state = dbConnection.getConnection().createStatement();
			ResultSet resultSet = state.executeQuery("SELECT * FROM " + tableName + " WHERE `name_history` LIKE '%" + nameHistory + "%'");
			while (resultSet.next()) {
				olympaPlayers.add(getOlympaPlayer(resultSet));
			}
			state.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return olympaPlayers;
	}

	public static Set<OlympaPlayer> getPlayersByRegex(String regex) {
		Set<OlympaPlayer> olympaPlayers = new HashSet<>();
		try {
			Statement state = dbConnection.getConnection().createStatement();
			ResultSet resultSet = state.executeQuery("SELECT * FROM " + tableName + " WHERE `pseudo` REGEXP '" + regex);
			while (resultSet.next()) {
				olympaPlayers.add(getOlympaPlayer(resultSet));
			}
			state.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return olympaPlayers;
	}

	public static Set<OlympaPlayer> getPlayersBySimilarName(String name) {
		Set<OlympaPlayer> olympaPlayers = new HashSet<>();
		try {
			Statement state = dbConnection.getConnection().createStatement();
			ResultSet resultSet = state.executeQuery("SELECT * FROM " + tableName + " WHERE `pseudo` LIKE '" + name);
			while (resultSet.next()) {
				olympaPlayers.add(getOlympaPlayer(resultSet));
			}
			state.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return olympaPlayers;
	}

	public static Set<String> getPlayersNamesBySimilarName(String name) {
		name = Utils.insertChar(name, "%");
		Set<String> names = new HashSet<>();
		try {
			Statement state = dbConnection.getConnection().createStatement();
			ResultSet resultSet = state.executeQuery("SELECT `pseudo` FROM " + tableName + " WHERE `pseudo` LIKE '" + name);
			while (resultSet.next()) {
				names.add(resultSet.getString(1));
			}
			state.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return names;
	}

	/**
	 * Récupère l'uuid d'un joueur dans la base de données à l'aide de son pseudo
	 */
	public static UUID getPlayerUniqueId(String playerName) {
		List<?> list = MySQL.selectTable("SELECT `uuid-server` FROM " + tableName + " WHERE `pseudo` = " + playerName);
		if (!list.isEmpty()) {
			return (UUID) list.get(0);
		}
		return null;
	}

	public static int getRankIdSite(OlympaGroup group) {
		List<?> list = MySQL.selectTable("SELECT `rank_id` FROM" + "`olympa.ranks`" + "WHERE `pseudo` = " + group.getName());
		if (!list.isEmpty()) {
			return (Integer) list.get(0);
		}
		return -1;
	}

	public static boolean playerExist(UUID playerUuid) {
		try {
			Statement state = dbConnection.getConnection().createStatement();
			ResultSet resultSet = state.executeQuery("SELECT * FROM " + tableName + " WHERE `uuid-server` = '" + playerUuid);
			if (resultSet.next()) {
				state.close();
				return true;
			} else {
				state.close();
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Sauvegarde les infos du olympaPlayer
	 */
	public static void savePlayer(OlympaPlayer olympaPlayer) {
		try {
			PreparedStatement pstate = dbConnection.getConnection().prepareStatement("UPDATE " + tableName + " SET `pseudo` = ?, `uuid-server` = ?,"
					+ " `uuid` = ?, `ip` = ?, `groups` = ?, `last_connection` = ?, `name_history` = ?, `ip_history` = ?, `gender` = ? WHERE `id` = ?");
			int i = 1;
			pstate.setString(i++, olympaPlayer.getName());
			pstate.setString(i++, olympaPlayer.getUniqueId().toString());
			UUID premiumUuid = olympaPlayer.getPremiumUniqueId();
			if (premiumUuid != null) {
				pstate.setString(i++, premiumUuid.toString());
			} else {
				pstate.setString(i++, null);
			}
			pstate.setString(i++, olympaPlayer.getIp());
			pstate.setString(i++, olympaPlayer.getGroupsToString());
			pstate.setTimestamp(i++, new Timestamp(olympaPlayer.getLastConnection() * 1000L));
			TreeMap<Long, String> histName = olympaPlayer.getHistHame();
			if (!histName.isEmpty()) {
				pstate.setString(i++, new Gson().toJson(histName));
			} else {
				pstate.setString(i++, null);
			}
			TreeMap<Long, String> histIp = olympaPlayer.getHistIp();
			if (!histIp.isEmpty()) {
				pstate.setString(i++, new Gson().toJson(histIp));
			} else {
				pstate.setString(i++, null);
			}
			pstate.setInt(i++, olympaPlayer.getGender().getId());
			/*Map<String, String> data = olympaPlayer.getData();
			if (!data.isEmpty()) {
				pstate.setString(i++, new Gson().toJson(data));
			} else {
				pstate.setString(i++, null);
			}*/
			pstate.setLong(i++, olympaPlayer.getId());
			pstate.executeUpdate();
			pstate.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void savePlayerPluginDatas(OlympaPlayer olympaPlayer) throws SQLException {
		System.out.println("MySQL.savePlayerPluginDatas()");
		if (updatePlayerPluginDatas == null) return;
		System.out.println(updatePlayerPluginDatas.getStatementCommand());
		PreparedStatement statement = updatePlayerPluginDatas.getStatement();
		olympaPlayer.saveDatas(statement);
		statement.setLong(updatePlayerPluginDatasID, olympaPlayer.getId());
		statement.executeUpdate();
	}

	public static void loadPlayerPluginDatas(OlympaPlayer olympaPlayer) throws SQLException {
		System.out.println("MySQL.loadPlayerPluginDatas()");
		if (getPlayerPluginDatas == null) return;
		System.out.println(getPlayerPluginDatas.getStatementCommand());
		PreparedStatement statement = getPlayerPluginDatas.getStatement();
		statement.setLong(1, olympaPlayer.getId());
		ResultSet pluginSet = statement.executeQuery();
		if (pluginSet.next()) olympaPlayer.loadDatas(pluginSet);
		pluginSet.close();
	}

	public static void savePlayerPassOrEmail(OlympaPlayer olympaPlayer) throws SQLException {
		PreparedStatement pstate = dbConnection.getConnection()
				.prepareStatement("UPDATE " + tableName + " SET `email` = ?, `password` = ? WHERE `id` = ?");
		int i = 1;
		pstate.setString(i++, olympaPlayer.getEmail());
		pstate.setString(i++, olympaPlayer.getPassword());
		pstate.setLong(i++, olympaPlayer.getId());
		pstate.executeUpdate();
		pstate.close();
	}

	/**
	 * Lire une/des valeur(s) dans une table
	 */
	public static List<Object> selectTable(String paramString) {
		if (!paramString.contains("SELECT")) {
			throw new IllegalArgumentException("\"" + paramString + "\" n'est pas le bon argument pour lire une/des valeur(s).");
		}
		List<Object> result = new ArrayList<>();
		try {
			Statement state = dbConnection.getConnection().createStatement();
			ResultSet resultSet = state.executeQuery(paramString);
			while (resultSet.next()) {
				result.add(resultSet.getObject(1));
			}
			state.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * public static void updateIp(String newIp, OlympaPlayer
	 * olympaPlayer) { try { PreparedStatement pstate =
	 * connection.prepareStatement("UPDATE " + tableName + " SET `ip` =
	 * ?, `ip_history` = ? WHERE `id` = ?;");
	 *
	 * int i = 1; pstate.setString(i++, newIp); pstate.setString(i++,
	 * String.join(";", olympaPlayer.getIpHistory())); pstate.setInt(i++,
	 * olympaPlayer.getId()); pstate.executeUpdate(); } catch (SQLException e)
	 * { e.printStackTrace(); } olympaPlayer.setIP(newIp); }
	 **/
	public MySQL(DbConnection dbConnection) {
		MySQL.dbConnection = dbConnection;
	}
}
