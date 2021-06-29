package fr.olympa.core.bungee.vpn;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import fr.olympa.api.common.sql.statement.OlympaStatement;
import fr.olympa.api.common.sql.statement.StatementType;

public class VpnSql {
	private static String tableName = "commun.vpn";

	private static OlympaStatement insert = new OlympaStatement(
			"INSERT INTO " + tableName + " (`ip`, `is_vpn`, `is_mobile`, `is_host`, `pseudo`, `country`, `city`, `org`, `as`, `last_update`, `date`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)").returnGeneratedKeys();
	private static OlympaStatement get = new OlympaStatement("SELECT * FROM " + tableName + " WHERE ip = ?");
	private static OlympaStatement update = new OlympaStatement(StatementType.UPDATE, tableName, "ip", new String[] { "is_vpn", "is_mobile", "is_host", "pseudo", "whitelist_user", "country", "city", "org", "as", "date", "last_update" });

	public static OlympaVpn addIp(OlympaVpn olympaVpn) throws SQLException {
		try (PreparedStatement pstate = insert.createStatement()) {
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
			pstate.setString(i++, olympaVpn.getAs());
			pstate.setTimestamp(i++, new Timestamp(olympaVpn.getLastUpdate() * 1000L));
			pstate.setDate(i, new Date(olympaVpn.getTime() * 1000L));
			insert.executeUpdate(pstate);
			ResultSet rs = pstate.getGeneratedKeys();
			rs.next();
			olympaVpn.id = rs.getLong("id");
			rs.close();
			olympaVpn.setUpWithDB(true);
			return olympaVpn;
		}
	}

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
				resultSet.getString(i++),
				resultSet.getTimestamp(i++),
				resultSet.getDate(i));
	}

	public static OlympaVpn getIpInfo(String ip) throws SQLException {
		OlympaVpn olympaVpn = null;
		try (PreparedStatement statement = get.createStatement()) {
			statement.setString(1, ip);
			ResultSet resultSet = get.executeQuery(statement);
			if (resultSet.next())
				olympaVpn = getInfo(resultSet);
		}
		if (olympaVpn != null)
			olympaVpn.setUpWithDB(true);
		return olympaVpn;
	}

	public static void saveIp(OlympaVpn olympaVpn) throws SQLException {
		try (PreparedStatement pstate = update.createStatement()) {
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
			pstate.setDate(i++, new Date(olympaVpn.getTime() * 1000L));
			pstate.setTimestamp(i++, new Timestamp(olympaVpn.getLastUpdate() * 1000L));
			pstate.setString(i, olympaVpn.getIp());
			update.executeUpdate(pstate);
			olympaVpn.setUpWithDB(true);
		}
	}
}
