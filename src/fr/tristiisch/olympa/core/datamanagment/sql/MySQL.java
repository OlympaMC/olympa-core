package fr.tristiisch.olympa.core.datamanagment.sql;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import fr.tristiisch.olympa.api.objects.OlympaPlayer;
import fr.tristiisch.olympa.api.permission.OlympaPlayerObject;
import fr.tristiisch.olympa.api.utils.Utils;

public class MySQL {

	public static boolean createPlayer(final OlympaPlayer olympaPlayer) {
		try {
			String ps = "INSERT INTO players (uuid, name, ip, `groups`, first_connection, last_connection) VALUES (?, ?, ?, ?, ?, ?);";
			int i = 1;
			PreparedStatement pstate = DatabaseManager.getConnection().prepareStatement(ps);
			pstate.setString(i++, olympaPlayer.getUniqueId().toString());
			pstate.setString(i++, olympaPlayer.getName());
			pstate.setString(i++, olympaPlayer.getIp());
			pstate.setString(i++, olympaPlayer.getGroupsToString());
			pstate.setDate(i++, new Date(olympaPlayer.getFirstConnection() * 1000L));
			pstate.setDate(i++, new Date(olympaPlayer.getLastConnection() * 1000L));
			pstate.executeUpdate();
		} catch (final SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Création d'une table
	 */
	public static void createTable(final String paramString) {
		if (!paramString.contains("CREATE TABLE")) {
			throw new IllegalArgumentException("\"" + paramString + "\" n'est pas le bon argument pour lire une/des valeur(s).");
		}
		try {
			final Statement state = DatabaseManager.getConnection().createStatement();
			state.executeUpdate(paramString);
		} catch (final SQLException e) {
			e.printStackTrace();
		}
	}

	public static Set<String> getAllPlayersNames() {
		Set<String> names = new HashSet<>();
		try {
			final Statement state = DatabaseManager.getConnection().createStatement();
			final ResultSet resultSet = state.executeQuery("SELECT name FROM players;");
			while (resultSet.next()) {
				names.add(resultSet.getString(1));
			}
		} catch (final SQLException e) {
			e.printStackTrace();
			return null;
		}
		return names;
	}

	/**
	 * Récupère le nom exacte d'un joueur dans la base de données à l'aide de son
	 * UUID
	 */
	public static String getNameFromUUID(final UUID uuid) {
		final List<Object> list = MySQL.selectTable("SELECT name FROM players WHERE uuid = '" + uuid.toString() + "';", "name");
		if (!list.isEmpty()) {
			return list.get(0).toString();
		}
		return null;
	}

	/**
	 * Récupère la prochaine ID d'une table
	 */
	public static int getnextID(final String tableName, final String id) {
		try {
			final Statement state = DatabaseManager.getConnection().createStatement();
			final ResultSet resultSet = state.executeQuery("SELECT " + id + " FROM " + tableName + " ORDER BY id DESC LIMIT 1;");
			if (resultSet.next()) {
				return resultSet.getInt(1) + 1;
			}
		} catch (final SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}

	public static OlympaPlayer getOlympaPlayer(final ResultSet resultSet) {
		try {
			return new OlympaPlayerObject(
					resultSet.getInt("id"),
					UUID.fromString(resultSet.getString("uuid")),
					resultSet.getString("name"),
					resultSet.getString("groups"),
					resultSet.getString("ip"),
					resultSet.getDate("first_connection").getTime() / 1000L,
					resultSet.getDate("last_connection").getTime() / 1000L);
		} catch (final SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Permet de récupérer les donnés d'un joueur dans la base de données grâce à
	 * son id
	 */
	public static OlympaPlayer getPlayer(final int id) {
		try {
			final Statement state = DatabaseManager.getConnection().createStatement();
			final ResultSet resultSet = state.executeQuery("SELECT * FROM players WHERE id = '" + id + "';");
			if (resultSet.next()) {
				return getOlympaPlayer(resultSet);
			}
		} catch (final SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Permet de récupérer les donnés d'un joueur dans la base de données grâce à
	 * son pseudo
	 */
	public static OlympaPlayer getPlayer(final String playerName) {
		try {
			final Statement state = DatabaseManager.getConnection().createStatement();
			final ResultSet resultSet = state.executeQuery("SELECT * FROM players WHERE name = '" + playerName + "';");
			if (resultSet.next()) {
				return getOlympaPlayer(resultSet);
			}
		} catch (final SQLException e) {
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
	public static OlympaPlayer getPlayer(final UUID playerUUID) throws SQLException {
		final Statement state = DatabaseManager.getConnection().createStatement();
		final ResultSet resultSet = state.executeQuery("SELECT * FROM players WHERE uuid = '" + playerUUID + "';");
		if (resultSet.next()) {
			return getOlympaPlayer(resultSet);
		}
		return null;
	}

	/**
	 * Permet de récupérer les donnés d'un joueur dans la base de données grâce à
	 * son ts3databaseid
	 */
	public static OlympaPlayer getPlayerByTS3ID(final int ts3databaseid) {
		try {
			final Statement state = DatabaseManager.getConnection().createStatement();
			final ResultSet resultSet = state.executeQuery("SELECT * FROM players WHERE ts3databaseid = '" + ts3databaseid + "';");
			if (resultSet.next()) {

				return getOlympaPlayer(resultSet);
			}
		} catch (final SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getPlayerExactName(final String playerName) {
		final List<Object> list = MySQL.selectTable("SELECT name FROM players WHERE 'name' = " + playerName + ";", "name");
		if (list.isEmpty()) {
			return null;
		} else {
			return (String) list.get(0);
		}
	}

	public static List<OlympaPlayer> getPlayersByIp(final String ip) {
		final List<OlympaPlayer> olympaPlayers = new ArrayList<>();
		try {
			final Statement state = DatabaseManager.getConnection().createStatement();
			final ResultSet resultSet = state.executeQuery("SELECT * FROM players WHERE ip = '" + ip + "';");
			while (resultSet.next()) {
				olympaPlayers.add(getOlympaPlayer(resultSet));
			}
		} catch (final SQLException e) {
			e.printStackTrace();
		}
		return olympaPlayers;
	}

	public static List<OlympaPlayer> getPlayersByIpHistory(final String ipHistory) {
		final List<OlympaPlayer> olympaPlayers = new ArrayList<>();
		try {
			final Statement state = DatabaseManager.getConnection().createStatement();
			final ResultSet resultSet = state.executeQuery("SELECT * FROM server.players WHERE `ip_history` LIKE '%" + ipHistory + "%';");
			while (resultSet.next()) {
				olympaPlayers.add(getOlympaPlayer(resultSet));
			}
		} catch (final SQLException e) {
			e.printStackTrace();
		}
		return olympaPlayers;
	}

	public static List<OlympaPlayer> getPlayersByNameHistory(final String nameHistory) {
		final List<OlympaPlayer> olympaPlayers = new ArrayList<>();
		try {
			final Statement state = DatabaseManager.getConnection().createStatement();
			final ResultSet resultSet = state.executeQuery("SELECT * FROM players WHERE `name_history` LIKE '%" + nameHistory + "%';");
			while (resultSet.next()) {
				olympaPlayers.add(getOlympaPlayer(resultSet));
			}
		} catch (final SQLException e) {
			e.printStackTrace();
		}
		return olympaPlayers;
	}

	public static Set<OlympaPlayer> getPlayersByRegex(final String regex) {
		final Set<OlympaPlayer> olympaPlayers = new HashSet<>();
		try {
			final Statement state = DatabaseManager.getConnection().createStatement();
			final ResultSet resultSet = state.executeQuery("SELECT * FROM players WHERE `name` REGEXP '" + regex + "';");
			while (resultSet.next()) {
				olympaPlayers.add(getOlympaPlayer(resultSet));
			}
		} catch (final SQLException e) {
			e.printStackTrace();
		}
		return olympaPlayers;
	}

	public static Set<OlympaPlayer> getPlayersBySimilarName(String name) {
		name = Utils.insertChar(name, "%");
		final Set<OlympaPlayer> olympaPlayers = new HashSet<>();
		try {
			final Statement state = DatabaseManager.getConnection().createStatement();
			final ResultSet resultSet = state.executeQuery("SELECT * FROM players WHERE `name` LIKE '" + name + "';");
			while (resultSet.next()) {
				olympaPlayers.add(getOlympaPlayer(resultSet));
			}
		} catch (final SQLException e) {
			e.printStackTrace();
		}
		return olympaPlayers;
	}

	public static Set<String> getPlayersNamesBySimilarName(String name) {
		name = Utils.insertChar(name, "%");
		final Set<String> names = new HashSet<>();
		try {
			final Statement state = DatabaseManager.getConnection().createStatement();
			final ResultSet resultSet = state.executeQuery("SELECT name FROM players WHERE `name` LIKE '" + name + "';");
			while (resultSet.next()) {
				names.add(resultSet.getString(1));
			}
		} catch (final SQLException e) {
			e.printStackTrace();
		}
		return names;
	}

	/**
	 * Récupère l'uuid d'un joueur dans la base de données à l'aide de son pseudo
	 */
	public static UUID getPlayerUniqueId(final String playerName) {
		final List<Object> list = MySQL.selectTable("SELECT uuid FROM players WHERE 'name' = " + playerName + ";", "uuid");
		if (list.isEmpty()) {
			return null;
		} else {
			return (UUID) list.get(0);
		}
	}

	/**
	 * @param ts3databaseid ID de la base de donnés teamspeak
	 */
	public static UUID getUUIDfromTS3DatabaseID(final int ts3databaseid) {
		final List<Object> players = MySQL.selectTable("SELECT uuid FROM players WHERE ts3_id = '" + ts3databaseid + ";", "uuid");
		if (!players.isEmpty()) {
			return (UUID) players.get(0);
		}
		return null;
	}

	public static boolean playerExist(final UUID playerUuid) {
		try {
			final Statement state = DatabaseManager.getConnection().createStatement();
			final ResultSet resultSet = state.executeQuery("SELECT * FROM players WHERE uuid = '" + playerUuid + "';");
			if (resultSet.next()) {
				return true;
			} else {
				return false;
			}
		} catch (final SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Sauvegarde les infos du olympaPlayer
	 */
	public static void savePlayer(final OlympaPlayer olympaPlayer) {
		try {
			final PreparedStatement pstate = DatabaseManager.getConnection()
					.prepareStatement("UPDATE players SET name = ?, ip = ?, `groups` = ?, last_connection = ? WHERE uuid = ?;");
			int i = 1;
			pstate.setString(i++, olympaPlayer.getName());
			pstate.setString(i++, olympaPlayer.getIp());
			pstate.setString(i++, olympaPlayer.getGroupsToString());
			pstate.setDate(i++, new Date(olympaPlayer.getLastConnection() * 1000L));
			pstate.setString(i++, olympaPlayer.getUniqueId().toString());
			pstate.executeUpdate();
		} catch (final SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Lire une/des valeur(s) dans une table
	 */
	public static List<Object> selectTable(final String paramString, final String paramString2) {

		if (!paramString.contains("SELECT")) {
			throw new IllegalArgumentException("\"" + paramString + "\" n'est pas le bon argument pour lire une/des valeur(s).");
		}
		final List<Object> result = new ArrayList<>();
		try {
			final Statement state = DatabaseManager.getConnection().createStatement();
			final ResultSet resultSet = state.executeQuery(paramString);
			while (resultSet.next()) {
				result.add(resultSet.getObject(paramString2));
			}
		} catch (final SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * public static void updateIp(final String newIp, final OlympaPlayer
	 * olympaPlayer) { try { final PreparedStatement pstate =
	 * DatabaseManager.getConnection().prepareStatement("UPDATE players SET `ip` =
	 * ?, `ip_history` = ? WHERE `id` = ?;");
	 *
	 * int i = 1; pstate.setString(i++, newIp); pstate.setString(i++,
	 * String.join(";", olympaPlayer.getIpHistory())); pstate.setInt(i++,
	 * olympaPlayer.getId()); pstate.executeUpdate(); } catch (final SQLException e)
	 * { e.printStackTrace(); } olympaPlayer.setIP(newIp); }
	 **/

	/**
	 * Update une/des valeur(s) dans une table
	 */
	public static void updateTable(final String paramString) {
		if (!paramString.contains("UPDATE")) {
			throw new IllegalArgumentException("\"" + paramString + "\" n'est pas le bon argument pour lire une/des valeur(s).");
		}
		try {
			final Statement state = DatabaseManager.getConnection().createStatement();
			state.executeUpdate(paramString);
		} catch (final SQLException e) {
			e.printStackTrace();
		}
	}

	public static void updateUsername(final String newName, final OlympaPlayer olympaPlayer) {
		try {
			final PreparedStatement pstate = DatabaseManager.getConnection().prepareStatement("UPDATE players SET `name` = ?, `name_history` = CONCAT_WS(';', ?, name_history) WHERE `uuid` = ?;");

			int i = 1;
			pstate.setString(i++, newName);
			pstate.setString(i++, olympaPlayer.getName());
			pstate.setString(i++, olympaPlayer.getUniqueId().toString());
			pstate.executeUpdate();
		} catch (final SQLException e) {
			e.printStackTrace();
		}
		olympaPlayer.setName(newName);
	}
}
