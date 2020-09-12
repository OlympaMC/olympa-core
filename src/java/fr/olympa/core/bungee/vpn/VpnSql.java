package fr.olympa.core.bungee.vpn;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import fr.olympa.api.sql.DbConnection;

public class VpnSql {
	private static String tableName = "commun.vpn";
	private static DbConnection dbConnection;

	public static boolean addIp(OlympaVpn olympaVpn) throws SQLException {
		try {
			String ps = "INSERT INTO " + tableName + " (`ip`, `is_vpn`, `is_mobile`, `is_host`, `pseudo`, `country`, `city`, `org`, `as`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";
			int i = 1;
			PreparedStatement pstate = dbConnection.getConnection().prepareStatement(ps);
			pstate.setString(i++, olympaVpn.getIp());
			pstate.setInt(i++, olympaVpn.isProxy() ? 1 : 0);
			pstate.setInt(i++, olympaVpn.isMobile() ? 1 : 0);
			pstate.setInt(i++, olympaVpn.isHosting() ? 1 : 0);
			List<String> users = olympaVpn.getUsers();
			if (users.isEmpty())
				pstate.setString(i++, null);
			else
				pstate.setString(i++, String.join(";", users));
			pstate.setString(i++, olympaVpn.getCountry());
			pstate.setString(i++, olympaVpn.getCity());
			pstate.setString(i++, olympaVpn.getOrg());
			pstate.setString(i++, olympaVpn.getAs());
			pstate.executeUpdate();
			pstate.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
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
				resultSet.getInt(i++) == 1 ? true : false,
				resultSet.getInt(i++) == 1 ? true : false,
				resultSet.getInt(i++) == 1 ? true : false,
				resultSet.getString(i++),
				resultSet.getString(i++),
				resultSet.getString(i++),
				resultSet.getString(i++),
				resultSet.getString(i++));
	}

	public static OlympaVpn getIpInfo(String ip) throws SQLException {
		OlympaVpn info = null;
		Statement state = dbConnection.getConnection().createStatement();
		ResultSet resultSet = state.executeQuery("SELECT * FROM " + tableName + " WHERE ip = '" + ip + "';");
		if (resultSet.next())
			info = getInfo(resultSet);
		state.close();
		return info;
	}

	public static void saveIp(OlympaVpn olympaVpn) throws SQLException {
		int i = 1;
		PreparedStatement pstate = dbConnection.getConnection()
				.prepareStatement("UPDATE " + tableName + " SET `is_vpn` = ?, `is_mobile` = ?, `is_host` = ?, `pseudo` = ?, `country` = ?, `city` = ?, `org` = ?, `as` = ? WHERE `ip` = ?;");
		pstate.setInt(i++, olympaVpn.isProxy() ? 1 : 0);
		pstate.setInt(i++, olympaVpn.isMobile() ? 1 : 0);
		pstate.setInt(i++, olympaVpn.isHosting() ? 1 : 0);
		List<String> users = olympaVpn.getUsers();
		if (users.isEmpty())
			pstate.setString(i++, null);
		else
			pstate.setString(i++, String.join(";", users));
		pstate.setString(i++, olympaVpn.getCountry());
		pstate.setString(i++, olympaVpn.getCity());
		pstate.setString(i++, olympaVpn.getOrg());
		pstate.setString(i++, olympaVpn.getAs());
		pstate.setString(i++, olympaVpn.getIp());
		pstate.executeUpdate();
		pstate.close();
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
