package fr.olympa.core.bungee.vpn;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import fr.olympa.api.sql.DbConnection;
import fr.olympa.api.sql.statement.OlympaStatement;
import fr.olympa.api.sql.statement.StatementType;

public class VpnSql {
	private static String tableName = "commun.vpn";
	private static DbConnection dbConnection;

	public static OlympaVpn addIp(OlympaVpn olympaVpn) throws SQLException {
		OlympaStatement statement = new OlympaStatement("INSERT INTO " + tableName + " (`ip`, `is_vpn`, `is_mobile`, `is_host`, `pseudo`, `country`, `city`, `org`, `as`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);").returnGeneratedKeys();
		try (PreparedStatement pstate = statement.createStatement()) {
			int i = 1;
			pstate.setString(i++, olympaVpn.getIp());
			pstate.setBoolean(i++, olympaVpn.isProxy());
			pstate.setBoolean(i++, olympaVpn.isMobile());
			pstate.setBoolean(i++, olympaVpn.isHosting());
			List<String> users = olympaVpn.getUsers();
			if (users == null || users.isEmpty())
				pstate.setString(i++, null);
			else
				pstate.setString(i++, String.join(";", users));
			pstate.setString(i++, olympaVpn.getCountry());
			pstate.setString(i++, olympaVpn.getCity());
			pstate.setString(i++, olympaVpn.getOrg());
			pstate.setString(i, olympaVpn.getAs());
			statement.executeUpdate(pstate);
			ResultSet rs = pstate.getGeneratedKeys();
			rs.next();
			olympaVpn.id = rs.getLong("id");
			rs.close();
			olympaVpn.setUpWithDB(true);
			return olympaVpn;
		}
	}

	/*
	 * public static OlympaVpn getIp(OlympaPlayer olympaPlayer) throws SQLException
	 * { OlympaVpn info = null; Statement state =
	 * dbConnection.getConnection().createStatement(); ResultSet resultSet =
	 * state.executeQuery("SELECT * FROM " + tableName + " WHERE users REGEXP '" +
	 * olympaPlayer.getId() + "($|\\W)';"); while (resultSet.next()) { info =
	 * getInfo(resultSet); if (info != null &&
	 * info.getUsers().contains(olympaPlayer.getId())) { info = getInfo(resultSet);
	 * } } state.close(); return info; }
	 */

	private static OlympaVpn getInfo(ResultSet resultSet) throws SQLException {
		int i = 1;
		return new OlympaVpn(resultSet.getLong(i++),
				resultSet.getString(i++),
				resultSet.getBoolean(i++),
				resultSet.getBoolean(i++),
				resultSet.getBoolean(i++),
				resultSet.getString(i++),
				resultSet.getString(i++),
				resultSet.getString(i++),
				resultSet.getString(i++),
				resultSet.getString(i++),
				resultSet.getString(i));
	}

	public static OlympaVpn getIpInfo(String ip) throws SQLException {
		OlympaVpn olympaVpn = null;
		Statement state = dbConnection.getConnection().createStatement();
		ResultSet resultSet = state.executeQuery("SELECT * FROM " + tableName + " WHERE ip = '" + ip + "';");
		if (resultSet.next())
			olympaVpn = getInfo(resultSet);
		state.close();
		if (olympaVpn != null)
			olympaVpn.setUpWithDB(true);
		return olympaVpn;
	}

	public static void saveIp(OlympaVpn olympaVpn) throws SQLException {
		OlympaStatement statement = new OlympaStatement(StatementType.UPDATE, tableName, "ip", new String[] { "is_vpn", "is_mobile", "is_host", "pseudo", "whitelist_user", "country", "city", "org", "as" });
		try (PreparedStatement pstate = statement.createStatement()) {
			int i = 1;
			pstate.setBoolean(i++, olympaVpn.isProxy());
			pstate.setBoolean(i++, olympaVpn.isMobile());
			pstate.setBoolean(i++, olympaVpn.isHosting());
			List<String> users = olympaVpn.getUsers();
			if (users == null || users.isEmpty())
				pstate.setString(i++, null);
			else
				pstate.setString(i++, String.join(";", users));
			List<String> whitelistUsers = olympaVpn.getWhitelistUsers();
			if (whitelistUsers == null || whitelistUsers.isEmpty())
				pstate.setString(i++, null);
			else
				pstate.setString(i++, String.join(";", whitelistUsers));
			pstate.setString(i++, olympaVpn.getCountry());
			pstate.setString(i++, olympaVpn.getCity());
			pstate.setString(i++, olympaVpn.getOrg());
			pstate.setString(i++, olympaVpn.getAs());

			pstate.setString(i, olympaVpn.getIp());
			statement.executeUpdate(pstate);
			olympaVpn.setUpWithDB(true);
		}
	}

	/*
	 * public static boolean setIp(String ip, boolean isVpn) throws SQLException {
	 * try { String ps = "INSERT INTO " + tableName +
	 * " (`ip`, is_vpn) VALUES (?, ?);"; int i = 1; PreparedStatement pstate =
	 * dbConnection.getConnection().prepareStatement(ps); pstate.setString(i++, ip);
	 * pstate.setInt(i++, isVpn ? 1 : 0); pstate.executeUpdate(); pstate.close(); }
	 * catch (SQLException e) { e.printStackTrace(); return false; } return true; }
	 */

	public VpnSql(DbConnection dbConnection) {
		VpnSql.dbConnection = dbConnection;
	}
}
