package fr.olympa.core.common.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import fr.olympa.api.common.groups.OlympaGroup;
import fr.olympa.api.common.match.RegexMatcher;
import fr.olympa.api.common.player.Gender;
import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.common.player.OlympaPlayerInformations;
import fr.olympa.api.common.player.PlayerSQL;
import fr.olympa.api.common.provider.AccountProviderAPI;
import fr.olympa.api.common.sql.statement.OlympaStatement;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.common.provider.OlympaPlayerInformationsObject;

public class MySQL implements PlayerSQL {

	DbConnection dbConnection;
	String table;

	private OlympaStatement getNameFromUUIDStatement;
	private OlympaStatement getPlayerInformationsByIdStatement;
	private OlympaStatement getPlayerInformationsByNameStatement;
	private OlympaStatement getPlayerInformationsByUUIDStatement;
	private OlympaStatement getPlayerByIdStatement;
	private OlympaStatement getPlayerByPseudoStatement;
	private OlympaStatement getPlayerByUUIDServerStatement;
	private OlympaStatement getPlayerByUUIDPremiumStatement;
	private OlympaStatement getPlayerByTS3Statement;
	private OlympaStatement getPlayersByIPStatement;
	private OlympaStatement getPlayersByIPHistoryStatement;
	private OlympaStatement getPlayersByAllIPStatement;
	private OlympaStatement getPlayersByNameHistoryStatement;
	private OlympaStatement getPlayersByRegexStatement;
	private OlympaStatement getPlayersBySimilarNameStatement;
	private OlympaStatement getNamesBySimilarName;
	private OlympaStatement getDuplicatePasswordStatement;
	private OlympaStatement playerExistStatement;

	public MySQL(DbConnection dbConnection) {
		this.dbConnection = dbConnection;
		table = String.format("`%s`.`%s`", "common", "players");
		getNameFromUUIDStatement = new OlympaStatement("SELECT `pseudo` FROM " + table + " WHERE `uuid_server` = ?");
		getPlayerInformationsByIdStatement = new OlympaStatement("SELECT `pseudo`, `uuid_server` FROM " + table + " WHERE `id` = ?");
		getPlayerInformationsByNameStatement = new OlympaStatement("SELECT `id`, `pseudo`, `uuid_server` FROM " + table + " WHERE `pseudo` = ?");
		getPlayerByIdStatement = new OlympaStatement("SELECT * FROM " + table + " WHERE `id` = ?");
		getPlayerByPseudoStatement = new OlympaStatement("SELECT * FROM " + table + " WHERE `pseudo` = ?");
		getPlayerByUUIDPremiumStatement = new OlympaStatement("SELECT * FROM " + table + " WHERE `uuid_premium` = ?");
		getPlayerInformationsByUUIDStatement = new OlympaStatement("SELECT `pseudo`, `id` FROM " + table + " WHERE `uuid_server` = ?");
		getPlayerByUUIDServerStatement = new OlympaStatement("SELECT * FROM " + table + " WHERE `uuid_server` = ?");
		getPlayerByTS3Statement = new OlympaStatement("SELECT * FROM " + table + " WHERE `ts3_id` = ?");
		getPlayersByIPStatement = new OlympaStatement("SELECT * FROM " + table + " WHERE `ip` = ?");
		getPlayersByIPHistoryStatement = new OlympaStatement("SELECT * FROM " + table + " WHERE `ip_history` LIKE ?");
		getPlayersByAllIPStatement = new OlympaStatement("SELECT * FROM " + table + " WHERE `ip` = ? OR `ip_history` LIKE ?");
		getPlayersByNameHistoryStatement = new OlympaStatement("SELECT * FROM " + table + " WHERE `name_history` LIKE ?");
		getPlayersByRegexStatement = new OlympaStatement("SELECT * FROM " + table + " WHERE `pseudo` REGEXP ?");
		getPlayersBySimilarNameStatement = new OlympaStatement("SELECT * FROM " + table + " WHERE `pseudo` LIKE ?");
		getNamesBySimilarName = new OlympaStatement("SELECT pseudo FROM " + table + " WHERE `pseudo` LIKE ?");
		getDuplicatePasswordStatement = new OlympaStatement("SELECT * FROM " + table + " ORDER BY password HAVING COUNT(password) > 1");
		playerExistStatement = new OlympaStatement("SELECT `id` FROM " + table + " WHERE `uuid_server` = ?");
	}

	public String getTable() {
		return table;
	}

	@Override
	public String getTableCleanName() {
		return table.replace("`", "");
	}

	/**
	 * Récupère le nom exacte d'un joueur dans la base de données à l'aide de son
	 * UUID
	 * @throws SQLException
	 */
	@Override
	public String getNameFromUuid(UUID uuid) throws SQLException {
		try (PreparedStatement statement = getNameFromUUIDStatement.createStatement()) {
			statement.setString(1, uuid.toString());
			ResultSet resultSet = getNameFromUUIDStatement.executeQuery(statement);
			if (resultSet.first())
				return resultSet.getString(1);
			resultSet.close();
			return null;
		}
	}

	/**
	 * Permet de récupérer les informations d'un joueur dans la base de données grâce à
	 * son id
	 * @throws SQLException
	 */
	@Override
	public OlympaPlayerInformations getPlayerInformations(long id) throws SQLException {
		try (PreparedStatement statement = getPlayerInformationsByIdStatement.createStatement()) {
			statement.setLong(1, id);
			ResultSet resultSet = getPlayerInformationsByIdStatement.executeQuery(statement);
			OlympaPlayerInformationsObject opInfo = null;
			if (resultSet.next())
				opInfo = new OlympaPlayerInformationsObject(id, resultSet.getString("pseudo"), RegexMatcher.UUID.parse(resultSet.getString("uuid_server")));
			resultSet.close();
			return opInfo;
		}
	}

	/**
	 * Permet de récupérer les informations d'un joueur dans la base de données grâce à
	 * son nom
	 * @throws SQLException
	 */
	@Override
	public OlympaPlayerInformations getPlayerInformations(String name) throws SQLException {
		try (PreparedStatement statement = getPlayerInformationsByNameStatement.createStatement()) {
			statement.setString(1, name);
			ResultSet resultSet = getPlayerInformationsByNameStatement.executeQuery(statement);
			OlympaPlayerInformationsObject opInfo = null;
			if (resultSet.next())
				opInfo = new OlympaPlayerInformationsObject(resultSet.getLong("id"), resultSet.getString("pseudo"), RegexMatcher.UUID.parse(resultSet.getString("uuid_server")));
			resultSet.close();
			return opInfo;
		}
	}

	/**
	 * Permet de récupérer les informations d'un joueur dans la base de données grâce à
	 * son id
	 * @throws SQLException
	 */
	@Override
	public OlympaPlayerInformations getPlayerInformations(UUID uuid) throws SQLException {
		try (PreparedStatement statement = getPlayerInformationsByUUIDStatement.createStatement()) {
			statement.setString(1, Utils.getUUIDString(uuid));
			ResultSet resultSet = getPlayerInformationsByUUIDStatement.executeQuery(statement);
			OlympaPlayerInformationsObject opInfo = null;
			if (resultSet.next())
				opInfo = new OlympaPlayerInformationsObject(resultSet.getLong("id"), resultSet.getString("pseudo"), uuid);
			resultSet.close();
			return opInfo;
		}
	}

	/**
	 * Permet de récupérer les donnés d'un joueur dans la base de données grâce à
	 * son id
	 * @throws SQLException
	 */
	@Override
	public OlympaPlayer getPlayer(long id) throws SQLException {
		try (PreparedStatement statement = getPlayerByIdStatement.createStatement()) {
			statement.setLong(1, id);
			ResultSet resultSet = getPlayerByIdStatement.executeQuery(statement);
			OlympaPlayer op = null;
			if (resultSet.next())
				op = getOlympaPlayer(resultSet);
			resultSet.close();
			return op;
		}
	}

	/**
	 * Permet de récupérer les donnés d'un joueur dans la base de données grâce à
	 * son pseudo
	 * @throws SQLException
	 */
	@Override
	public OlympaPlayer getPlayer(String playerName) throws SQLException {
		try (PreparedStatement statement = getPlayerByPseudoStatement.createStatement()) {
			statement.setString(1, playerName);
			ResultSet resultSet = getPlayerByPseudoStatement.executeQuery(statement);
			OlympaPlayer op = null;
			if (resultSet.next())
				op = getOlympaPlayer(resultSet);
			resultSet.close();
			return op;
		}
	}

	/**
	 * Permet de récupérer les donnés d'un joueur dans la base de données grâce à
	 * son uuid
	 *
	 * @throws SQLException
	 */
	@Override
	public OlympaPlayer getPlayer(UUID playerUUID) throws SQLException {
		try (PreparedStatement statement = getPlayerByUUIDServerStatement.createStatement()) {
			statement.setString(1, playerUUID.toString());
			ResultSet resultSet = getPlayerByUUIDServerStatement.executeQuery(statement);
			OlympaPlayer op = null;
			if (resultSet.next())
				op = getOlympaPlayer(resultSet);
			resultSet.close();
			return op;
		}
	}

	/**
	 * Permet de récupérer les donnés d'un joueur dans la base de données grâce à
	 * son uuid premium
	 * @throws SQLException
	 */
	@Override
	public OlympaPlayer getPlayerByPremiumUuid(UUID premiumUUID) throws SQLException {
		try (PreparedStatement statement = getPlayerByUUIDPremiumStatement.createStatement()) {
			statement.setString(1, Utils.getUUIDString(premiumUUID));
			ResultSet resultSet = getPlayerByUUIDPremiumStatement.executeQuery(statement);
			OlympaPlayer olympaPlayer = null;
			if (resultSet.next())
				olympaPlayer = getOlympaPlayer(resultSet);
			resultSet.close();
			return olympaPlayer;
		}
	}

	/**
	 * Permet de récupérer les donnés d'un joueur dans la base de données grâce à
	 * son ts3databaseid
	 * @throws SQLException
	 */
	@Override
	public OlympaPlayer getPlayerByTs3Id(int ts3databaseid) throws SQLException {
		try (PreparedStatement statement = getPlayerByTS3Statement.createStatement()) {
			statement.setInt(1, ts3databaseid);
			ResultSet resultSet = getPlayerByTS3Statement.executeQuery(statement);
			OlympaPlayer olympaPlayer = null;
			if (resultSet.next())
				olympaPlayer = getOlympaPlayer(resultSet);
			resultSet.close();
			return olympaPlayer;
		}
	}

	public List<OlympaPlayer> getPlayersByIp(String ip) throws SQLException {
		try (PreparedStatement statement = getPlayersByIPStatement.createStatement()) {
			statement.setString(1, ip);
			List<OlympaPlayer> olympaPlayers = new ArrayList<>();
			ResultSet resultSet = getPlayersByIPStatement.executeQuery(statement);
			while (resultSet.next())
				olympaPlayers.add(getOlympaPlayer(resultSet));
			resultSet.close();
			return olympaPlayers;
		}
	}

	public List<OlympaPlayer> getPlayersByIpHistory(String ipHistory) throws SQLException {
		try (PreparedStatement statement = getPlayersByIPHistoryStatement.createStatement()) {
			statement.setString(1, "%" + ipHistory + "%");
			List<OlympaPlayer> olympaPlayers = new ArrayList<>();
			ResultSet resultSet = getPlayersByIPHistoryStatement.executeQuery(statement);
			while (resultSet.next())
				olympaPlayers.add(getOlympaPlayer(resultSet));
			resultSet.close();
			return olympaPlayers;
		}
	}

	public Map<Boolean, List<OlympaPlayer>> getPlayersByAllIp(String ipAlreadyUsed) throws SQLException {
		try (PreparedStatement statement = getPlayersByAllIPStatement.createStatement()) {
			statement.setString(1, ipAlreadyUsed);
			statement.setString(2, "%" + ipAlreadyUsed + "%");
			List<OlympaPlayer> olympaPlayers = new ArrayList<>();
			ResultSet resultSet = getPlayersByAllIPStatement.executeQuery(statement);
			while (resultSet.next()) {
				OlympaPlayer op = getOlympaPlayer(resultSet);
				olympaPlayers.add(op);
			}
			resultSet.close();
			return olympaPlayers.stream().collect(Collectors.partitioningBy(op -> op.getIp().equals(ipAlreadyUsed)));
		}
	}

	@Override
	public List<OlympaPlayer> getPlayersByNameHistory(String nameHistory) throws SQLException {
		try (PreparedStatement statement = getPlayersByNameHistoryStatement.createStatement()) {
			statement.setString(1, "%" + nameHistory + "%");
			List<OlympaPlayer> olympaPlayers = new ArrayList<>();
			ResultSet resultSet = getPlayersByNameHistoryStatement.executeQuery(statement);
			while (resultSet.next())
				olympaPlayers.add(getOlympaPlayer(resultSet));
			resultSet.close();
			return olympaPlayers;
		}
	}

	@Override
	public Set<OlympaPlayer> getPlayersByRegex(String regex) throws SQLException {
		try (PreparedStatement statement = getPlayersByRegexStatement.createStatement()) {
			statement.setString(1, regex);
			Set<OlympaPlayer> olympaPlayers = new HashSet<>();
			ResultSet resultSet = getPlayersByRegexStatement.executeQuery(statement);
			while (resultSet.next())
				olympaPlayers.add(getOlympaPlayer(resultSet));
			resultSet.close();
			return olympaPlayers;
		}
	}

	@Override
	public Set<String> getPlayersBySimilarChars(String name) throws SQLException {
		return getNamesBySimilarName(Utils.insertChar(name, "%"));
	}

	@Override
	public Set<OlympaPlayer> getPlayersBySimilarName(String name) throws SQLException {
		if (name.charAt(name.length() - 1) != '%')
			name += "%";
		try (PreparedStatement statement = getPlayersBySimilarNameStatement.createStatement()) {
			statement.setString(1, name);
			Set<OlympaPlayer> olympaPlayers = new HashSet<>();
			ResultSet resultSet = getPlayersBySimilarNameStatement.executeQuery(statement);
			while (resultSet.next())
				olympaPlayers.add(getOlympaPlayer(resultSet));
			statement.close();
			return olympaPlayers;
		}
	}

	@Override
	public Set<OlympaPlayer> getPlayersByGroupsIds(OlympaGroup... groups) throws SQLException {
		return getPlayersByGroupsIds(Arrays.stream(groups).collect(Collectors.toList()));
	}

	@Override
	public Set<OlympaPlayer> getPlayersByGroupsIds(List<OlympaGroup> groups) throws SQLException {
		List<Integer> groupsIds = groups.stream().map(OlympaGroup::getId).collect(Collectors.toList());
		StringBuilder sb = new StringBuilder("SELECT * FROM " + table + " WHERE `groups` REGEXP ?");
		OlympaStatement newGetPlayerByGroupStatement = new OlympaStatement(sb.toString());
		try (PreparedStatement statement = newGetPlayerByGroupStatement.createStatement()) {
			statement.setString(1, String.format("\\b(%s)\\b", groups.stream().map(g -> String.valueOf(g.getId())).collect(Collectors.joining("|"))));
			Set<OlympaPlayer> olympaPlayers = new HashSet<>();
			ResultSet resultSet = newGetPlayerByGroupStatement.executeQuery(statement);
			while (resultSet.next()) {
				OlympaPlayer op = getOlympaPlayer(resultSet);
				if (op.getGroups().keySet().stream().anyMatch(gr -> groupsIds.contains(gr.getId())))
					olympaPlayers.add(op);
			}
			resultSet.close();
			return olympaPlayers;
		}
	}

	@Override
	public Set<String> getNamesBySimilarChars(String name) throws SQLException {
		return getNamesBySimilarName(Utils.insertChar(name, "%"));
	}

	@Override
	public Set<String> getNamesBySimilarName(String name) {
		Set<String> names = new HashSet<>();
		if (name == null || name.isBlank())
			return names;
		if (name.charAt(name.length() - 1) != '%')
			name += "%";
		try (PreparedStatement statement = getNamesBySimilarName.createStatement()) {
			statement.setString(1, name);
			ResultSet resultSet = getNamesBySimilarName.executeQuery(statement);
			while (resultSet.next())
				names.add(resultSet.getString("pseudo"));
			resultSet.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return names;
	}

	/**
	 * Récupère l'uuid d'un joueur dans la base de données à l'aide de son pseudo
	 * @throws SQLException
	 */
	@Override
	public UUID getPlayerUniqueId(String playerName) throws SQLException {
		List<?> list = selectTable("SELECT `uuid_server` FROM " + table + " WHERE `pseudo` = " + playerName);
		if (!list.isEmpty())
			return (UUID) list.get(0);
		return null;
	}

	public Set<OlympaPlayer> getDuplicatePassword() throws SQLException {
		try (PreparedStatement statement = getDuplicatePasswordStatement.createStatement()) {
			Set<OlympaPlayer> olympaPlayers = new HashSet<>();
			ResultSet resultSet = getDuplicatePasswordStatement.executeQuery(statement);
			while (resultSet.next())
				olympaPlayers.add(getOlympaPlayer(resultSet));
			resultSet.close();
			return olympaPlayers;
		}
	}

	@Override
	public boolean playerExist(UUID playerUUID) throws SQLException {
		try (PreparedStatement statement = playerExistStatement.createStatement()) {
			statement.setString(1, playerUUID.toString());
			ResultSet resultSet = playerExistStatement.executeQuery(statement);
			boolean b = resultSet.next();
			resultSet.close();
			return b;
		}
	}

	public OlympaPlayer getOlympaPlayer(ResultSet resultSet) throws SQLException {
		String uuidPremiumString = resultSet.getString("uuid_premium");
		UUID uuidPremium = null;
		if (uuidPremiumString != null)
			uuidPremium = RegexMatcher.UUID.parse(uuidPremiumString);
		OlympaPlayer player = AccountProviderAPI.getter().getOlympaPlayerProvider().create(RegexMatcher.UUID.parse(resultSet.getString("uuid_server")), resultSet.getString("pseudo"), resultSet.getString("ip"));
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
				resultSet.getBoolean("vanish"));
		return player;
	}

	/**
	 * Lire une/des valeur(s) dans une table
	 * @throws SQLException
	 */
	public List<Object> selectTable(String paramString) throws SQLException {
		if (!paramString.contains("SELECT"))
			throw new IllegalArgumentException("\"" + paramString + "\" n'est pas le bon argument pour lire une/des valeur(s).");
		List<Object> result = new ArrayList<>();
		Statement state = dbConnection.getConnection().createStatement();
		ResultSet resultSet = state.executeQuery(paramString);
		while (resultSet.next())
			result.add(resultSet.getObject(1));
		state.close();
		return result;
	}
}