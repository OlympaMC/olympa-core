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
import java.util.Set;
import java.util.UUID;

import fr.olympa.OlympaCore;
import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.provider.OlympaPlayerObject;
import fr.olympa.api.utils.Utils;

public class MySQL {

	static String tableName = "olympa.users";

	public static boolean createPlayer(OlympaPlayer olympaPlayer) {
		try {
			String ps = "INSERT INTO " + tableName + " (auth-uuid, name, uuid, ip, `groups`, created, last_connection, email, password) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";
			int i = 1;
			PreparedStatement pstate = OlympaCore.getInstance().getDatabase().prepareStatement(ps);
			pstate.setString(i++, olympaPlayer.getUniqueId().toString());
			if (olympaPlayer.getPremiumUniqueId() != null) {
				pstate.setString(i++, olympaPlayer.getPremiumUniqueId().toString());
			} else {
				pstate.setString(i++, null);
			}
			pstate.setString(i++, olympaPlayer.getName());
			pstate.setString(i++, olympaPlayer.getIp());
			pstate.setString(i++, olympaPlayer.getGroupsToString());
			pstate.setDate(i++, new Date(olympaPlayer.getFirstConnection() * 1000L));
			pstate.setTimestamp(i++, new Timestamp(olympaPlayer.getLastConnection() * 1000L));
			pstate.setString(i++, olympaPlayer.getEmail());
			pstate.setString(i++, olympaPlayer.getPassword());
			pstate.executeUpdate();
			pstate.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static Set<String> getAllPlayersNames() {
		Set<String> names = new HashSet<>();
		try {
			Statement state = OlympaCore.getInstance().getDatabase().createStatement();
			ResultSet resultSet = state.executeQuery("SELECT name FROM " + tableName);
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
		List<Object> list = MySQL.selectTable("SELECT name FROM " + tableName + " WHERE auth-uuid = '" + uuid.toString() + "';", "name");
		if (!list.isEmpty()) {
			return list.get(0).toString();
		}
		return null;
	}

	public static OlympaPlayer getOlympaPlayer(ResultSet resultSet) {
		try {
			return new OlympaPlayerObject(
					resultSet.getInt("id"),
					UUID.fromString(resultSet.getString("auth-uuid")),
					resultSet.getString("name"),
					resultSet.getString("groups"),
					resultSet.getString("ip"),
					resultSet.getDate("first_connection").getTime() / 1000L,
					resultSet.getTimestamp("last_connection").getTime() / 1000L,
					resultSet.getString("password"),
					resultSet.getString("email"));
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
			Statement state = OlympaCore.getInstance().getDatabase().createStatement();
			ResultSet resultSet = state.executeQuery("SELECT * FROM " + tableName + " WHERE id = '" + id + "';");
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
			Statement state = OlympaCore.getInstance().getDatabase().createStatement();
			ResultSet resultSet = state.executeQuery("SELECT * FROM " + tableName + " WHERE name = '" + playerName + "';");
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
		Statement state = OlympaCore.getInstance().getDatabase().createStatement();
		ResultSet resultSet = state.executeQuery("SELECT * FROM " + tableName + " WHERE auth-uuid = '" + playerUUID + "';");
		if (resultSet.next()) {
			return getOlympaPlayer(resultSet);
		}
		state.close();
		return null;
	}

	/**
	 * Permet de récupérer les donnés d'un joueur dans la base de données grâce à
	 * son uuid premium
	 */
	public static OlympaPlayer getPlayerByPremiumUuid(UUID premiumUuid) throws SQLException {
		Statement state = OlympaCore.getInstance().getDatabase().createStatement();
		ResultSet resultSet = state.executeQuery("SELECT * FROM " + tableName + " WHERE uuid = '" + premiumUuid + "';");
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
	public static OlympaPlayer getPlayerByTs3Id(int ts3databaseid) {
		try {
			Statement state = OlympaCore.getInstance().getDatabase().createStatement();
			ResultSet resultSet = state.executeQuery("SELECT * FROM " + tableName + " WHERE ts3_id = '" + ts3databaseid + "';");
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
		List<Object> list = MySQL.selectTable("SELECT name FROM " + tableName + " WHERE `pseudo` = " + playerName + ";", "name");
		if (list.isEmpty()) {
			return null;
		} else {
			return (String) list.get(0);
		}
	}

	public static List<OlympaPlayer> getPlayersByIp(String ip) {
		List<OlympaPlayer> olympaPlayers = new ArrayList<>();
		try {
			Statement state = OlympaCore.getInstance().getDatabase().createStatement();
			ResultSet resultSet = state.executeQuery("SELECT * FROM " + tableName + " WHERE ip = '" + ip + "';");
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
			Statement state = OlympaCore.getInstance().getDatabase().createStatement();
			ResultSet resultSet = state.executeQuery("SELECT * FROM " + tableName + " WHERE `ip_history` LIKE '%" + ipHistory + "%';");
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
			Statement state = OlympaCore.getInstance().getDatabase().createStatement();
			ResultSet resultSet = state.executeQuery("SELECT * FROM " + tableName + " WHERE `name_history` LIKE '%" + nameHistory + "%';");
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
			Statement state = OlympaCore.getInstance().getDatabase().createStatement();
			ResultSet resultSet = state.executeQuery("SELECT * FROM" + tableName + "WHERE `pseudo` REGEXP '" + regex + "';");
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
			Statement state = OlympaCore.getInstance().getDatabase().createStatement();
			ResultSet resultSet = state.executeQuery("SELECT * FROM" + tableName + "WHERE `pseudo` LIKE '" + name + "';");
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
			Statement state = OlympaCore.getInstance().getDatabase().createStatement();
			ResultSet resultSet = state.executeQuery("SELECT name FROM" + tableName + "WHERE `pseudo` LIKE '" + name + "';");
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
		List<Object> list = MySQL.selectTable("SELECT auth-uuid FROM" + tableName + "WHERE `pseudo` = " + playerName + ";", "uuid");
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
		List<Object> players = MySQL.selectTable("SELECT auth-uuid FROM" + tableName + "WHERE ts3_id = '" + ts3databaseid + ";", "uuid");
		if (!players.isEmpty()) {
			return (UUID) players.get(0);
		}
		return null;
	}

	public static boolean playerExist(UUID playerUuid) {
		try {
			Statement state = OlympaCore.getInstance().getDatabase().createStatement();
			ResultSet resultSet = state.executeQuery("SELECT * FROM" + tableName + "WHERE auth-uuid = '" + playerUuid + "';");
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
			PreparedStatement pstate = OlympaCore.getInstance().getDatabase()
					.prepareStatement("UPDATE" + tableName + "SET name = ?, ip = ?, `groups` = ?, last_connection = ? WHERE auth-uuid = ?;");
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
			Statement state = OlympaCore.getInstance().getDatabase().createStatement();
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
	 * OlympaCore.getInstance().getDatabase().prepareStatement("UPDATE" + tableName + "SET `ip` =
	 * ?, `ip_history` = ? WHERE `id` = ?;");
	 *
	 * int i = 1; pstate.setString(i++, newIp); pstate.setString(i++,
	 * String.join(";", olympaPlayer.getIpHistory())); pstate.setInt(i++,
	 * olympaPlayer.getId()); pstate.executeUpdate(); } catch (SQLException e)
	 * { e.printStackTrace(); } olympaPlayer.setIP(newIp); }
	 **/

	public static void updateUsername(String newName, OlympaPlayer olympaPlayer) {
		try {
			PreparedStatement pstate = OlympaCore.getInstance().getDatabase().prepareStatement("UPDATE" + tableName + "SET `pseudo` = ?, `name_history` = CONCAT_WS(';', ?, name_history) WHERE `auth-uuid` = ?;");

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
