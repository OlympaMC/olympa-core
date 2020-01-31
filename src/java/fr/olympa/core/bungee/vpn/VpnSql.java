package fr.olympa.core.bungee.vpn;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.stream.Collectors;

import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.sql.DbConnection;

public class VpnSql {
	private static String tableName = "vpn";
	private static DbConnection dbConnection;

	private static OlympaVpn getInfo(ResultSet resultSet) throws SQLException {
		return new OlympaVpn(resultSet.getLong(1), resultSet.getString(2), resultSet.getInt(3) == 1 ? true : false, resultSet.getString(4));
	}

	public static OlympaVpn getIp(OlympaPlayer olympaPlayer) throws SQLException {
		OlympaVpn info = null;
		Statement state = dbConnection.getConnection().createStatement();
		ResultSet resultSet = state.executeQuery("SELECT * FROM " + tableName + " WHERE users REGEXP '" + olympaPlayer.getId() + "($|\\W)';");
		while (resultSet.next()) {
			info = getInfo(resultSet);
			if (info != null && info.getUsers().contains(olympaPlayer.getId())) {
				info = getInfo(resultSet);
			}
		}
		state.close();
		return info;
	}

	public static OlympaVpn getIpInfo(String ip) throws SQLException {
		OlympaVpn info = null;
		Statement state = dbConnection.getConnection().createStatement();
		ResultSet resultSet = state.executeQuery("SELECT * FROM " + tableName + " WHERE ip = '" + ip + "';");
		if (resultSet.next()) {
			info = getInfo(resultSet);
		}
		state.close();
		return info;
	}

	public static void saveIp(OlympaVpn olympaVpn) throws SQLException {
		PreparedStatement pstate = dbConnection.getConnection()
				.prepareStatement("UPDATE " + tableName + " SET `is_vpn` = ?, `users` = ? WHERE `id` = ?;");
		int i = 1;
		pstate.setInt(i++, olympaVpn.isVpn() ? 1 : 0);
		List<String> strings = olympaVpn.getUsers().stream().map(i2 -> String.valueOf(i2)).collect(Collectors.toList());
		if (!strings.isEmpty()) {
			pstate.setString(i++, String.join(",", strings));
		} else {
			pstate.setString(i++, null);
		}
		pstate.setLong(i++, olympaVpn.getId());
		pstate.executeUpdate();
		pstate.close();
	}

	public static boolean setIp(OlympaPlayer olympaPlayer, String ip, boolean isVpn) throws SQLException {
		try {
			String ps = "INSERT INTO " + tableName + " (`ip`, is_vpn, users) VALUES (?, ?, ?);";
			int i = 1;
			PreparedStatement pstate = dbConnection.getConnection().prepareStatement(ps);
			pstate.setString(i++, ip);
			pstate.setInt(i++, isVpn ? 1 : 0);
			pstate.setLong(i++, olympaPlayer.getId());

			pstate.executeUpdate();
			pstate.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static boolean setIp(String ip, boolean isVpn) throws SQLException {
		try {
			String ps = "INSERT INTO " + tableName + " (`ip`, is_vpn) VALUES (?, ?);";
			int i = 1;
			PreparedStatement pstate = dbConnection.getConnection().prepareStatement(ps);
			pstate.setString(i++, ip);
			pstate.setInt(i++, isVpn ? 1 : 0);
			pstate.executeUpdate();
			pstate.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public VpnSql(DbConnection dbConnection) {
		VpnSql.dbConnection = dbConnection;
	}
}
