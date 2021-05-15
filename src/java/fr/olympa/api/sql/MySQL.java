package fr.olympa.api.sql;

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

import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.match.RegexMatcher;
import fr.olympa.api.player.Gender;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.player.OlympaPlayerInformations;
import fr.olympa.api.player.PlayerSQL;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.provider.OlympaPlayerInformationsObject;
import fr.olympa.api.sql.statement.OlympaStatement;
import fr.olympa.api.utils.Utils;

public class MySQL implements PlayerSQL {

	DbConnection dbConnection;
	String table;

	public MySQL(DbConnection dbConnection) {
		this.dbConnection = dbConnection;
		table = String.format("`%s`.`%s`", "commun", "players");
		//		registerKeys(MySQL.class);
	}

	public String getTable() {
		return table;
	}

	public String getTableCleanName() {
		return table.replace("`", "");
	}

	private OlympaStatement getNameFromUUIDStatement = new OlympaStatement("SELECT `pseudo` FROM " + table + " WHERE `uuid_server` = ?");

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

	private OlympaStatement getPlayerInformationsByIdStatement = new OlympaStatement("SELECT `pseudo`, `uuid_server` FROM " + table + " WHERE `id` = ?");

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

	private OlympaStatement getPlayerInformationsByNameStatement = new OlympaStatement("SELECT `id`, `pseudo`, `uuid_server` FROM " + table + " WHERE `pseudo` = ?");

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

	private OlympaStatement getPlayerInformationsByUUIDStatement = new OlympaStatement("SELECT `pseudo`, `id` FROM " + table + " WHERE `uuid_server` = ?");

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

	private OlympaStatement getPlayerByIdStatement = new OlympaStatement("SELECT * FROM " + table + " WHERE `id` = ?");

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

	private OlympaStatement getPlayerByPseudoStatement = new OlympaStatement("SELECT * FROM " + table + " WHERE `pseudo` = ?");

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

	private OlympaStatement getPlayerByUUIDServerStatement = new OlympaStatement("SELECT * FROM " + table + " WHERE `uuid_server` = ?");

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

	private OlympaStatement getPlayerByUUIDPremiumStatement = new OlympaStatement("SELECT * FROM " + table + " WHERE `uuid_premium` = ?");

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

	private OlympaStatement getPlayerByTS3Statement = new OlympaStatement("SELECT * FROM " + table + " WHERE `ts3_id` = ?");

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

	private OlympaStatement getPlayersByIPStatement = new OlympaStatement("SELECT * FROM " + table + " WHERE `ip` = ?");

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

	private OlympaStatement getPlayersByIPHistoryStatement = new OlympaStatement("SELECT * FROM " + table + " WHERE `ip_history` LIKE ?");

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

	private OlympaStatement getPlayersByAllIPStatement = new OlympaStatement("SELECT * FROM " + table + " WHERE `ip` = ? OR `ip_history` LIKE ?");

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

	private OlympaStatement getPlayersByNameHistoryStatement = new OlympaStatement("SELECT * FROM " + table + " WHERE `name_history` LIKE ?");

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

	private OlympaStatement getPlayersByRegexStatement = new OlympaStatement("SELECT * FROM " + table + " WHERE `pseudo` REGEXP ?");

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

	private OlympaStatement getPlayersBySimilarNameStatement = new OlympaStatement("SELECT * FROM " + table + " WHERE `pseudo` LIKE ?");

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

	private OlympaStatement getNamesBySimilarName = new OlympaStatement("SELECT pseudo FROM " + table + " WHERE `pseudo` LIKE ?");

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

	private OlympaStatement getDuplicatePasswordStatement = new OlympaStatement("SELECT * FROM " + table + " ORDER BY password HAVING COUNT(password) > 1");

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

	private OlympaStatement playerExistStatement = new OlympaStatement("SELECT `id` FROM " + table + " WHERE `uuid_server` = ?");

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
		OlympaPlayer player = AccountProvider.pluginPlayerProvider.create(RegexMatcher.UUID.parse(resultSet.getString("uuid_server")), resultSet.getString("pseudo"), resultSet.getString("ip"));
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

	/* Idée bof TODO
	 private OlympaStatement getPlayerNamesStatement = new OlympaStatement("SELECT `pseudo` FROM " + table);
	// Pour pas surcharger les requettes MySQL
	// TODO -> cache redis pour le cache multi-server
	Set<String> allPlayersNamesCache = null;

	public Set<String> getAllPlayersNames() {
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

	//	SQLKey id = new SQLKey((Function<OlympaPlayer, Long>) x -> x.getId(), x -> x.getKey().getLong(x.getValue()));
	//	SQLKey pseudo = new SQLKey((Function<OlympaPlayer, String>) x -> x.getName(), x -> x.getKey().getString(x.getValue()));
	//	SQLKey uuid_server = new SQLKey((Function<OlympaPlayer, UUID>) x -> x.getUniqueId(), x -> (UUID) RegexMatcher.UUID.parse(x.getKey().getString(x.getValue())));
	//	SQLKey uuid_premium = new SQLKey((Function<OlympaPlayer, UUID>) x -> x.getPremiumUniqueId(), (SetFunction<Entry<ResultSet, String>, UUID>) uuid_server.set, true);
	//	SQLKey groups = new SQLKey((Function<OlympaPlayer, String>) x -> x.getGroupsToString());
	//	SQLKey email = new SQLKey((Function<OlympaPlayer, String>) x -> x.getEmail());
	//	SQLKey password = new SQLKey((Function<OlympaPlayer, String>) x -> x.getPassword());
	//	SQLKey money = new SQLKey((Function<OlympaPlayer, Integer>) x -> x.getMoney());
	//	SQLKey ip = new SQLKey((Function<OlympaPlayer, String>) x -> x.getIp());
	//	SQLKey created = new SQLKey((Function<OlympaPlayer, Long>) x -> x.getFirstConnection());
	//	SQLKey last_connection = new SQLKey((Function<OlympaPlayer, Timestamp>) x -> new Timestamp(x.getLastConnection() * 1000L));
	//	SQLKey ts3_id = new SQLKey((Function<OlympaPlayer, Integer>) x -> x.getTeamspeakId());
	//	SQLKey name_history = new SQLKey((Function<OlympaPlayer, String>) x -> GsonCustomizedObjectTypeAdapter.GSON.toJson(x.getHistHame()));
	//	SQLKey ip_history = new SQLKey((Function<OlympaPlayer, String>) x -> GsonCustomizedObjectTypeAdapter.GSON.toJson(x.getHistIp()));
	//	SQLKey gender = new SQLKey((Function<OlympaPlayer, Integer>) x -> x.getGender().ordinal());
	//	SQLKey vanish = new SQLKey((Function<OlympaPlayer, Boolean>) x -> x.isVanish());

	//	private Map<String, SQLKey> tablesKeys;
	//
	//	private void registerKeys(Class<? extends SQLClass> clazz) {
	//		for (Field field : clazz.getDeclaredFields())
	//			if (field.getType().isAssignableFrom(SQLKey.class)) {
	//				System.out.println("Field : " + field.getName());
	//				try {
	//					tablesKeys.put(field.getName(), (SQLKey) field.get(null));
	//				} catch (IllegalArgumentException | IllegalAccessException e) {
	//					e.printStackTrace();
	//				}
	//			}
	//
	//	}
}