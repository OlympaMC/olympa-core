package fr.olympa.api.sql;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.UUID;

import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.player.Gender;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.player.OlympaPlayerInformations;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.provider.OlympaPlayerInformationsObject;
import fr.olympa.api.sql.statement.OlympaStatement;
import fr.olympa.api.sql.statement.StatementType;
import fr.olympa.api.utils.GsonCustomizedObjectTypeAdapter;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.spigot.OlympaCore;

public class MySQL {

	static DbConnection dbConnection;
	static String tableName = "commun.players";

	private static OlympaStatement insertPlayerStatement = new OlympaStatement(
			"INSERT INTO " + tableName + " (`uuid_server`, `uuid_premium`, `pseudo`, `ip`, `groups`, `created`, `last_connection`, `password`) VALUES (?, ?, ?, ?, ?, ?, ?, ?)", true);

	public static long createPlayer(OlympaPlayer olympaPlayer) throws SQLException {
		PreparedStatement statement = insertPlayerStatement.getStatement();
		int i = 1;
		statement.setString(i++, Utils.getUUIDString(olympaPlayer.getUniqueId()));
		UUID premiumUuid = olympaPlayer.getPremiumUniqueId();
		if (premiumUuid != null)
			statement.setString(i++, Utils.getUUIDString(premiumUuid));
		else
			statement.setString(i++, null);
		statement.setString(i++, olympaPlayer.getName());
		statement.setString(i++, olympaPlayer.getIp());
		statement.setString(i++, olympaPlayer.getGroupsToString());
		statement.setDate(i++, new Date(olympaPlayer.getFirstConnection() * 1000L));
		statement.setTimestamp(i++, new Timestamp(olympaPlayer.getLastConnection() * 1000L));
		statement.setString(i, olympaPlayer.getPassword());

		statement.executeUpdate();
		ResultSet resultSet = statement.getGeneratedKeys();
		resultSet.next();
		long id = resultSet.getLong("id");
		resultSet.close();
		return id;
	}

	/* Idée bof TODO
	 private static OlympaStatement getPlayerNamesStatement = new OlympaStatement("SELECT `pseudo` FROM " + tableName);
	// Pour pas surcharger les requettes MySQL
	// TODO -> cache redis pour le cache multi-server
	static Set<String> allPlayersNamesCache = null;

	public static Set<String> getAllPlayersNames() {
		if (allPlayersNamesCache != null)
			return allPlayersNamesCache;
		Set<String> names = new HashSet<>();
		try {
			ResultSet resultSet = getPlayerNamesStatement.getStatement().executeQuery();
			while (resultSet.next())
				names.add(resultSet.getString(1));
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		if (!names.isEmpty())
			//			Only Spigot
			//			OlympaCore.getInstance().getTask().runTaskLater("clearAllPlayersNamesCache", () -> allPlayersNamesCache = null, 10 * 60 * 20);
			allPlayersNamesCache = names;
		return names;
	}*/

	private static OlympaStatement getNameFromUUIDStatement = new OlympaStatement("SELECT `pseudo` FROM " + tableName + " WHERE `uuid_server` = ?");

	/**
	 * Récupère le nom exacte d'un joueur dans la base de données à l'aide de son
	 * UUID
	 * @throws SQLException
	 */
	public static String getNameFromUuid(UUID uuid) throws SQLException {
		PreparedStatement statement = getNameFromUUIDStatement.getStatement();
		statement.setString(1, uuid.toString());
		ResultSet resultSet = statement.executeQuery();
		if (resultSet.first())
			return resultSet.getString(1);
		return null;
	}

	private static OlympaStatement getPlayerPluginDatas;
	private static OlympaStatement insertPlayerPluginDatas;
	private static OlympaStatement updatePlayerPluginDatas;
	private static int updatePlayerPluginDatasID;

	public static OlympaPlayer getOlympaPlayer(ResultSet resultSet) throws SQLException {
		String uuidPremiumString = resultSet.getString("uuid_premium");
		UUID uuidPremium = null;
		if (uuidPremiumString != null)
			uuidPremium = Utils.getUUID(uuidPremiumString);
		OlympaPlayer player = AccountProvider.playerProvider.create(Utils.getUUID(resultSet.getString("uuid_server")), resultSet.getString("pseudo"), resultSet.getString("ip"));
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
				resultSet.getString("ip_history"),
				resultSet.getInt("ts3_id"),
				resultSet.getInt("discord_olympa_id"),
				resultSet.getBoolean("vanish"));
		return player;
	}

	public static void setDatasTable(String tableName, Map<String, String> columns) throws SQLException {
		Statement statement = OlympaCore.getInstance().getDatabase().createStatement();

		ResultSet columnsSet = OlympaCore.getInstance().getDatabase().getMetaData().getColumns(null, null, tableName, "%");
		if (columnsSet.first()) { // la table existe : il faut vérifier si toutes les colonnes sont présentes
			Map<String, String> missingColumns = new HashMap<>(columns);
			while (columnsSet.next()) {
				String columnName = columnsSet.getString(4);
				if (missingColumns.remove(columnName) == null)
					OlympaCore.getInstance().sendMessage("§cColonne " + columnName + " présente dans la table " + tableName + " mais pas dans la déclaration des données joueurs.");
			}
			for (Entry<String, String> column : missingColumns.entrySet()) {
				String columnValue = "`" + column.getKey() + "` " + column.getValue();
				statement.executeUpdate("ALTER TABLE `" + tableName + "` ADD " + columnValue);
				OlympaCore.getInstance().sendMessage("La colonne §6" + columnValue + " §ea été créée dans la table de données joueurs §6" + tableName + "§e.");
			}
		} else { // la table n'existe pas : il faut la créer
			StringJoiner creationJoiner = new StringJoiner(", ", "CREATE TABLE IF NOT EXISTS `" + tableName + "` (", ")");
			creationJoiner.add("`player_id` BIGINT NOT NULL");
			for (Entry<String, String> column : columns.entrySet())
				creationJoiner.add("`" + column.getKey() + "` " + column.getValue());
			creationJoiner.add("PRIMARY KEY (`player_id`)");
			statement.executeUpdate(creationJoiner.toString());
			OlympaCore.getInstance().sendMessage("Table des données joueurs §6" + tableName + " §ecréée !");
		}

		StringJoiner updateJoiner = new StringJoiner(", ", "UPDATE `" + tableName + "` SET ", " WHERE `player_id` = ?");
		StringJoiner insertJoinerKeys = new StringJoiner(", ", "INSERT INTO `" + tableName + "` (", " )");
		StringJoiner insertJoinerValues = new StringJoiner(", ", " VALUES (", " )");
		for (String columnName : columns.keySet()) {
			updateJoiner.add("`" + columnName + "` = ?");
			insertJoinerKeys.add("`" + columnName + "`");
			insertJoinerValues.add("?");
		}
		insertJoinerKeys.add("`player_id`");
		insertJoinerValues.add("?");
		updatePlayerPluginDatas = new OlympaStatement(updateJoiner.toString());
		updatePlayerPluginDatasID = columns.size() + 1;
		insertPlayerPluginDatas = new OlympaStatement(insertJoinerKeys.toString() + insertJoinerValues.toString());

		getPlayerPluginDatas = new OlympaStatement("SELECT * FROM `" + tableName + "` WHERE `player_id` = ?");

		statement.close();
		OlympaCore.getInstance().sendMessage("La table §6" + tableName + " §egère les données joueurs.");
	}

	private static OlympaStatement getPlayerInformationsByIdStatement = new OlympaStatement("SELECT `pseudo`, `uuid_server` FROM " + tableName + " WHERE `id` = ?");

	/**
	 * Permet de récupérer les informations d'un joueur dans la base de données grâce à
	 * son id
	 */
	public static OlympaPlayerInformations getPlayerInformations(long id) {
		try {
			PreparedStatement statement = getPlayerInformationsByIdStatement.getStatement();
			statement.setLong(1, id);
			ResultSet resultSet = statement.executeQuery();
			if (resultSet.next())
				return new OlympaPlayerInformationsObject(id, resultSet.getString("pseudo"), Utils.getUUID(resultSet.getString("uuid_server")));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static OlympaStatement getPlayerByIdStatement = new OlympaStatement("SELECT * FROM " + tableName + " WHERE `id` = ?");

	/**
	 * Permet de récupérer les donnés d'un joueur dans la base de données grâce à
	 * son id
	 * @throws SQLException
	 */
	public static OlympaPlayer getPlayer(long id) throws SQLException {
		PreparedStatement statement = getPlayerByIdStatement.getStatement();
		statement.setLong(1, id);
		ResultSet resultSet = statement.executeQuery();
		if (resultSet.next())
			return getOlympaPlayer(resultSet);
		return null;
	}

	private static OlympaStatement getPlayerByPseudoStatement = new OlympaStatement("SELECT * FROM " + tableName + " WHERE `pseudo` = ?");

	/**
	 * Permet de récupérer les donnés d'un joueur dans la base de données grâce à
	 * son pseudo
	 */
	public static OlympaPlayer getPlayer(String playerName) {
		try {
			PreparedStatement statement = getPlayerByPseudoStatement.getStatement();
			statement.setString(1, playerName);
			ResultSet resultSet = statement.executeQuery();
			if (resultSet.next())
				return getOlympaPlayer(resultSet);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static OlympaStatement getPlayerByUUIDServerStatement = new OlympaStatement("SELECT * FROM " + tableName + " WHERE `uuid_server` = ?");

	/**
	 * Permet de récupérer les donnés d'un joueur dans la base de données grâce à
	 * son uuid
	 *
	 * @throws SQLException
	 */
	public static OlympaPlayer getPlayer(UUID playerUUID) throws SQLException {

		OlympaPlayer olympaPlayer = null;
		PreparedStatement statement = getPlayerByUUIDServerStatement.getStatement();
		statement.setString(1, playerUUID.toString());
		ResultSet resultSet = statement.executeQuery();
		if (resultSet.next())
			olympaPlayer = getOlympaPlayer(resultSet);
		return olympaPlayer;
	}

	private static OlympaStatement getPlayerByUUIDPremiumStatement = new OlympaStatement("SELECT * FROM " + tableName + " WHERE `uuid_premium` = ?");

	/**
	 * Permet de récupérer les donnés d'un joueur dans la base de données grâce à
	 * son uuid premium
	 * @throws SQLException
	 */
	public static OlympaPlayer getPlayerByPremiumUuid(UUID premiumUUID) throws SQLException {
		OlympaPlayer olympaPlayer = null;
		PreparedStatement statement = getPlayerByUUIDPremiumStatement.getStatement();
		statement.setString(1, Utils.getUUIDString(premiumUUID));
		ResultSet resultSet = statement.executeQuery();
		if (resultSet.next())
			olympaPlayer = getOlympaPlayer(resultSet);
		return olympaPlayer;
	}

	private static OlympaStatement getPlayerByTS3Statement = new OlympaStatement("SELECT * FROM " + tableName + " WHERE `ts3_id` = ?");

	/**
	 * Permet de récupérer les donnés d'un joueur dans la base de données grâce à
	 * son ts3databaseid
	 * @throws SQLException
	 */
	public static OlympaPlayer getPlayerByTs3Id(int ts3databaseid) throws SQLException {
		OlympaPlayer olympaPlayer = null;
		PreparedStatement statement = getPlayerByTS3Statement.getStatement();
		statement.setInt(1, ts3databaseid);
		ResultSet resultSet = statement.executeQuery();
		if (resultSet.next())
			olympaPlayer = getOlympaPlayer(resultSet);
		return olympaPlayer;
	}

	private static OlympaStatement getPlayersByIPStatement = new OlympaStatement("SELECT * FROM " + tableName + " WHERE `ip` = ?");

	public static List<OlympaPlayer> getPlayersByIp(String ip) {
		List<OlympaPlayer> olympaPlayers = new ArrayList<>();
		try {
			PreparedStatement statement = getPlayersByIPStatement.getStatement();
			statement.setString(1, ip);
			ResultSet resultSet = statement.executeQuery();
			while (resultSet.next())
				olympaPlayers.add(getOlympaPlayer(resultSet));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return olympaPlayers;
	}

	private static OlympaStatement getPlayersByIPHistoryStatement = new OlympaStatement("SELECT * FROM " + tableName + " WHERE `ip_history` LIKE ?");

	public static List<OlympaPlayer> getPlayersByIpHistory(String ipHistory) {
		List<OlympaPlayer> olympaPlayers = new ArrayList<>();
		try {
			PreparedStatement statement = getPlayersByIPHistoryStatement.getStatement();
			statement.setString(1, "%" + ipHistory + "%");
			ResultSet resultSet = statement.executeQuery();
			while (resultSet.next())
				olympaPlayers.add(getOlympaPlayer(resultSet));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return olympaPlayers;
	}

	private static OlympaStatement getPlayersByNameHistoryStatement = new OlympaStatement("SELECT * FROM " + tableName + " WHERE `name_history` LIKE ?");

	public static List<OlympaPlayer> getPlayersByNameHistory(String nameHistory) {
		List<OlympaPlayer> olympaPlayers = new ArrayList<>();
		try {
			PreparedStatement statement = getPlayersByNameHistoryStatement.getStatement();
			statement.setString(1, "%" + nameHistory + "%");
			ResultSet resultSet = statement.executeQuery();
			while (resultSet.next())
				olympaPlayers.add(getOlympaPlayer(resultSet));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return olympaPlayers;
	}

	private static OlympaStatement getPlayersByRegexStatement = new OlympaStatement("SELECT * FROM " + tableName + " WHERE `pseudo` REGEXP ?");

	public static Set<OlympaPlayer> getPlayersByRegex(String regex) {
		Set<OlympaPlayer> olympaPlayers = new HashSet<>();
		try {
			PreparedStatement statement = getPlayersByRegexStatement.getStatement();
			statement.setString(1, regex);
			ResultSet resultSet = statement.executeQuery();
			while (resultSet.next())
				olympaPlayers.add(getOlympaPlayer(resultSet));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return olympaPlayers;
	}

	private static OlympaStatement getPlayersBySimilarNameStatement = new OlympaStatement("SELECT * FROM " + tableName + " WHERE `pseudo` LIKE ?");

	public static Set<String> getPlayersBySimilarChars(String name) {
		return getNamesBySimilarName(Utils.insertChar(name, "%"));
	}

	public static Set<OlympaPlayer> getPlayersBySimilarName(String name) {
		if (name.charAt(name.length() - 1) != '%')
			name += "%";
		Set<OlympaPlayer> olympaPlayers = new HashSet<>();
		try {
			PreparedStatement statement = getPlayersBySimilarNameStatement.getStatement();
			statement.setString(1, name);
			ResultSet resultSet = statement.executeQuery();
			while (resultSet.next())
				olympaPlayers.add(getOlympaPlayer(resultSet));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return olympaPlayers;
	}

	private static OlympaStatement getNamesBySimilarName = new OlympaStatement("SELECT pseudo FROM " + tableName + " WHERE `pseudo` LIKE ?");

	public static Set<String> getNamesBySimilarChars(String name) {
		return getNamesBySimilarName(Utils.insertChar(name, "%"));
	}

	public static Set<String> getNamesBySimilarName(String name) {
		Set<String> names = new HashSet<>();
		if (name.length() < 3)
			return names;
		if (name.charAt(name.length() - 1) != '%')
			name += "%";
		try {
			PreparedStatement statement = getNamesBySimilarName.getStatement();
			statement.setString(1, name);
			ResultSet resultSet = statement.executeQuery();
			while (resultSet.next())
				names.add(resultSet.getString("pseudo"));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return names;
	}

	/**
	 * Récupère l'uuid d'un joueur dans la base de données à l'aide de son pseudo
	 */
	public static UUID getPlayerUniqueId(String playerName) {
		List<?> list = MySQL.selectTable("SELECT `uuid_server` FROM " + tableName + " WHERE `pseudo` = " + playerName);
		if (!list.isEmpty())
			return (UUID) list.get(0);
		return null;
	}

	public static int getRankIdSite(OlympaGroup group) {
		List<?> list = MySQL.selectTable("SELECT `rank_id` FROM" + "`olympa.ranks`" + "WHERE `pseudo` = " + group.getName());
		if (!list.isEmpty())
			return (Integer) list.get(0);
		return -1;
	}

	private static OlympaStatement playerExistStatement = new OlympaStatement("SELECT * FROM " + tableName + " WHERE `uuid_server` = ?");

	public static boolean playerExist(UUID playerUUID) {
		try {
			PreparedStatement statement = playerExistStatement.getStatement();
			statement.setString(1, playerUUID.toString());
			ResultSet resultSet = statement.executeQuery();
			return resultSet.next();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	//	private static OlympaStatement savePlayerStatement = new OlympaStatement("UPDATE " + tableName + " SET `pseudo` = ?, `uuid_server` = ?,"
	//			+ " `uuid_premium` = ?, `ip` = ?, `groups` = ?, `last_connection` = ?, `name_history` = ?, `ip_history` = ?, `gender` = ? WHERE `id` = ?");
	private static OlympaStatement savePlayerStatement = new OlympaStatement(StatementType.UPDATE, tableName,
			new String[] { "pseudo", "uuid_server", "uuid_premium", "ip", "groups", "last_connection", "name_history", "ip_history", "gender", "ts3_id", "discord_olympa_id", "vanish" }, "id");

	/**
	 * Sauvegarde les infos du olympaPlayer
	 */
	public static void savePlayer(OlympaPlayer olympaPlayer) {
		try {
			PreparedStatement pstate = savePlayerStatement.getStatement();
			int i = 1;
			pstate.setString(i++, olympaPlayer.getName());
			pstate.setString(i++, Utils.getUUIDString(olympaPlayer.getUniqueId()));
			UUID premiumUuid = olympaPlayer.getPremiumUniqueId();
			if (premiumUuid != null)
				pstate.setString(i++, Utils.getUUIDString(premiumUuid));
			else
				pstate.setString(i++, null);
			pstate.setString(i++, olympaPlayer.getIp());
			pstate.setString(i++, olympaPlayer.getGroupsToString());
			pstate.setTimestamp(i++, new Timestamp(olympaPlayer.getLastConnection() * 1000L));
			TreeMap<Long, String> histName = olympaPlayer.getHistHame();
			if (!histName.isEmpty())
				pstate.setString(i++, GsonCustomizedObjectTypeAdapter.GSON.toJson(histName));
			else
				pstate.setString(i++, null);
			TreeMap<Long, String> histIp = olympaPlayer.getHistIp();
			if (!histIp.isEmpty())
				pstate.setString(i++, GsonCustomizedObjectTypeAdapter.GSON.toJson(histIp));
			else
				pstate.setString(i++, null);
			pstate.setInt(i++, olympaPlayer.getGender().ordinal());
			if (olympaPlayer.getTeamspeakId() != 0)
				pstate.setInt(i++, olympaPlayer.getTeamspeakId());
			else
				pstate.setObject(i++, null);
			if (olympaPlayer.getDiscordOlympaId() != 0)
				pstate.setInt(i++, olympaPlayer.getDiscordOlympaId());
			else
				pstate.setObject(i++, null);
			pstate.setBoolean(i++, olympaPlayer.isVanish());
			pstate.setLong(i, olympaPlayer.getId());
			savePlayerStatement.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void savePlayerPluginDatas(OlympaPlayer olympaPlayer) throws SQLException {
		if (updatePlayerPluginDatas == null)
			return;
		PreparedStatement statement = updatePlayerPluginDatas.getStatement();
		olympaPlayer.saveDatas(statement);
		statement.setLong(updatePlayerPluginDatasID, olympaPlayer.getId());
		statement.executeUpdate();
	}

	public static void loadPlayerPluginDatas(OlympaPlayer olympaPlayer) throws SQLException {
		if (getPlayerPluginDatas == null)
			return;
		PreparedStatement statement = getPlayerPluginDatas.getStatement();
		statement.setLong(1, olympaPlayer.getId());
		ResultSet pluginSet = statement.executeQuery();
		if (pluginSet.next())
			olympaPlayer.loadDatas(pluginSet);
		else { // pas de données avant : insérer les données par défaut
			statement = insertPlayerPluginDatas.getStatement();
			olympaPlayer.saveDatas(statement);
			statement.setLong(updatePlayerPluginDatasID, olympaPlayer.getId());
			statement.executeUpdate();
			OlympaCore.getInstance().sendMessage("Données créées pour le joueur " + olympaPlayer.getName());
		}
		pluginSet.close();
	}

	private static OlympaStatement savePlayerPasswordEmail = new OlympaStatement("UPDATE " + tableName + " SET `email` = ?, `password` = ? WHERE `id` = ?");

	public static void savePlayerPassOrEmail(OlympaPlayer olympaPlayer) throws SQLException {
		PreparedStatement pstate = savePlayerPasswordEmail.getStatement();
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
		if (!paramString.contains("SELECT"))
			throw new IllegalArgumentException("\"" + paramString + "\" n'est pas le bon argument pour lire une/des valeur(s).");
		List<Object> result = new ArrayList<>();
		try {
			Statement state = dbConnection.getConnection().createStatement();
			ResultSet resultSet = state.executeQuery(paramString);
			while (resultSet.next())
				result.add(resultSet.getObject(1));
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
