package fr.tristiisch.olympa.core.datamanagment.sql;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import fr.tristiisch.olympa.api.objects.OlympaPlayer;
import fr.tristiisch.olympa.api.objects.OlympaPlayerObject;
import fr.tristiisch.olympa.api.utils.Utils;

public class MySQL {

	public static boolean createPlayer(OlympaPlayer olympaPlayer) {
		try {
			String ps = "INSERT INTO players (uuid, name, ip, `groups`, first_connection, last_connection) VALUES (?, ?, ?, ?, ?, ?);";
			int i = 1;
			PreparedStatement pstate = DatabaseManager.getConnection().prepareStatement(ps);
			pstate.setString(i++, olympaPlayer.getUniqueId().toString());
			pstate.setString(i++, olympaPlayer.getName());
			pstate.setString(i++, olympaPlayer.getIp());
			pstate.setString(i++, olympaPlayer.getGroupsToString());
			pstate.setDate(i++, new Date(olympaPlayer.getFirstConnection() * 1000L));
			pstate.setTimestamp(i++, new Timestamp(olympaPlayer.getLastConnection() * 1000L));
			pstate.executeUpdate();
			pstate.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Création d'une table
	 */
	public static void createTable(String paramString) {
		if (!paramString.contains("CREATE TABLE")) {
			throw new IllegalArgumentException("\"" + paramString + "\" n'est pas le bon argument pour lire une/des valeur(s).");
		}
		try {
			Statement state = DatabaseManager.getConnection().createStatement();
			state.executeUpdate(paramString);
			state.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static Set<String> getAllPlayersNames() {
		Set<String> names = new HashSet<>();
		try {
			Statement state = DatabaseManager.getConnection().createStatement();
			ResultSet resultSet = state.executeQuery("SELECT name FROM players;");
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
	public static String getNameFromUUID(UUID uuid) {
		List<Object> list = MySQL.selectTable("SELECT name FROM players WHERE uuid = '" + uuid.toString() + "';", "name");
		if (!list.isEmpty()) {
			return list.get(0).toString();
		}
		return null;
	}

	/**
	 * Récupère la prochaine ID d'une table
	 */
	public static int getnextID(String tableName, String id) {
		try {
			Statement state = DatabaseManager.getConnection().createStatement();
			ResultSet resultSet = state.executeQuery("SELECT " + id + " FROM " + tableName + " ORDER BY id DESC LIMIT 1;");
			if (resultSet.next()) {
				return resultSet.getInt(1) + 1;
			}
			state.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}

	public static OlympaPlayer getOlympaPlayer(ResultSet resultSet) {
		try {
			return new OlympaPlayerObject(
					resultSet.getInt("id"),
					UUID.fromString(resultSet.getString("uuid")),
					resultSet.getString("name"),
					resultSet.getString("groups"),
					resultSet.getString("ip"),
					resultSet.getDate("first_connection").getTime() / 1000L,
					resultSet.getTimestamp("last_connection").getTime() / 1000L);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Permet de récupérer les donnés d'un joueur dans la base de données grâce à
	 * son id
	 */
	public static OlympaPlayer getPlayer(int id) {
		try {
			Statement state = DatabaseManager.getConnection().createStatement();
			ResultSet resultSet = state.executeQuery("SELECT * FROM players WHERE id = '" + id + "';");
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
			Statement state = DatabaseManager.getConnection().createStatement();
			ResultSet resultSet = state.executeQuery("SELECT * FROM players WHERE name = '" + playerName + "';");
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
		Statement state = DatabaseManager.getConnection().createStatement();
		ResultSet resultSet = state.executeQuery("SELECT * FROM players WHERE uuid = '" + playerUUID + "';");
		if (resultSet.next()) {
			return getOlympaPlayer(resultSet);
		}
		state.close();
		return null;
	}

	/**
	 * Permet de récupérer les donnés d'un joueur dans la base de données grâce à
	 * son ts3databaseid
	 */
	public static OlympaPlayer getPlayerByTS3ID(int ts3databaseid) {
		try {
			Statement state = DatabaseManager.getConnection().createStatement();
			ResultSet resultSet = state.executeQuery("SELECT * FROM players WHERE ts3databaseid = '" + ts3databaseid + "';");
			if (resultSet.next()) {
				return getOlympaPlayer(resultSet);
			}
			state.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getPlayerExactName(String playerName) {
		List<Object> list = MySQL.selectTable("SELECT name FROM players WHERE 'name' = " + playerName + ";", "name");
		if (list.isEmpty()) {
			return null;
		} else {
			return (String) list.get(0);
		}
	}

	public static List<OlympaPlayer> getPlayersByIp(String ip) {
		List<OlympaPlayer> olympaPlayers = new ArrayList<>();
		try {
			Statement state = DatabaseManager.getConnection().createStatement();
			ResultSet resultSet = state.executeQuery("SELECT * FROM players WHERE ip = '" + ip + "';");
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
			Statement state = DatabaseManager.getConnection().createStatement();
			ResultSet resultSet = state.executeQuery("SELECT * FROM server.players WHERE `ip_history` LIKE '%" + ipHistory + "%';");
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
			Statement state = DatabaseManager.getConnection().createStatement();
			ResultSet resultSet = state.executeQuery("SELECT * FROM players WHERE `name_history` LIKE '%" + nameHistory + "%';");
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
			Statement state = DatabaseManager.getConnection().createStatement();
			ResultSet resultSet = state.executeQuery("SELECT * FROM players WHERE `name` REGEXP '" + regex + "';");
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
		name = Utils.insertChar(name, "%");
		Set<OlympaPlayer> olympaPlayers = new HashSet<>();
		try {
			Statement state = DatabaseManager.getConnection().createStatement();
			ResultSet resultSet = state.executeQuery("SELECT * FROM players WHERE `name` LIKE '" + name + "';");
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
			Statement state = DatabaseManager.getConnection().createStatement();
			ResultSet resultSet = state.executeQuery("SELECT name FROM players WHERE `name` LIKE '" + name + "';");
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
		List<Object> list = MySQL.selectTable("SELECT uuid FROM players WHERE 'name' = " + playerName + ";", "uuid");
		if (list.isEmpty()) {
			return null;
		} else {
			return (UUID) list.get(0);
		}
	}

	/**
	 * @param ts3databaseid ID de la base de donnés teamspeak
	 */
	public static UUID getUUIDfromTS3DatabaseID(int ts3databaseid) {
		List<Object> players = MySQL.selectTable("SELECT uuid FROM players WHERE ts3_id = '" + ts3databaseid + ";", "uuid");
		if (!players.isEmpty()) {
			return (UUID) players.get(0);
		}
		return null;
	}

	public static boolean playerExist(UUID playerUuid) {
		try {
			Statement state = DatabaseManager.getConnection().createStatement();
			ResultSet resultSet = state.executeQuery("SELECT * FROM players WHERE uuid = '" + playerUuid + "';");
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
			PreparedStatement pstate = DatabaseManager.getConnection()
					.prepareStatement("UPDATE players SET name = ?, ip = ?, `groups` = ?, last_connection = ? WHERE uuid = ?;");
			int i = 1;
			pstate.setString(i++, olympaPlayer.getName());
			pstate.setString(i++, olympaPlayer.getIp());
			pstate.setString(i++, olympaPlayer.getGroupsToString());
			pstate.setTimestamp(i++, new Timestamp(olympaPlayer.getLastConnection() * 1000L));
			pstate.setString(i++, olympaPlayer.getUniqueId().toString());
			pstate.executeUpdate();
			pstate.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Lire une/des valeur(s) dans une table
	 */
	public static List<Object> selectTable(String paramString, String paramString2) {
		if (!paramString.contains("SELECT")) {
			throw new IllegalArgumentException("\"" + paramString + "\" n'est pas le bon argument pour lire une/des valeur(s).");
		}
		List<Object> result = new ArrayList<>();
		try {
			Statement state = DatabaseManager.getConnection().createStatement();
			ResultSet resultSet = state.executeQuery(paramString);
			while (resultSet.next()) {
				result.add(resultSet.getObject(paramString2));
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
	 * DatabaseManager.getConnection().prepareStatement("UPDATE players SET `ip` =
	 * ?, `ip_history` = ? WHERE `id` = ?;");
	 *
	 * int i = 1; pstate.setString(i++, newIp); pstate.setString(i++,
	 * String.join(";", olympaPlayer.getIpHistory())); pstate.setInt(i++,
	 * olympaPlayer.getId()); pstate.executeUpdate(); } catch (SQLException e)
	 * { e.printStackTrace(); } olympaPlayer.setIP(newIp); }
	 **/

	/**
	 * Update une/des valeur(s) dans une table
	 */
	public static void updateTable(String paramString) {
		if (!paramString.contains("UPDATE")) {
			throw new IllegalArgumentException("\"" + paramString + "\" n'est pas le bon argument pour lire une/des valeur(s).");
		}
		try {
			Statement state = DatabaseManager.getConnection().createStatement();
			state.executeUpdate(paramString);
			state.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void updateUsername(String newName, OlympaPlayer olympaPlayer) {
		try {
			PreparedStatement pstate = DatabaseManager.getConnection().prepareStatement("UPDATE players SET `name` = ?, `name_history` = CONCAT_WS(';', ?, name_history) WHERE `uuid` = ?;");

			int i = 1;
			pstate.setString(i++, newName);
			pstate.setString(i++, olympaPlayer.getName());
			pstate.setString(i++, olympaPlayer.getUniqueId().toString());
			pstate.executeUpdate();
			pstate.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		olympaPlayer.setName(newName);
	}
}
