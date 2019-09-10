package fr.tristiisch.olympa.core.datamanagment.sql;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseManager {

	protected static DbConnection connection;

	public static void close() {
		try {
			connection.close();
		} catch (final SQLException e) {
			e.printStackTrace();
		}
	}

	public static void connect() {
		connection = new DbConnection(new DbCredentials("localhost", "olympa", "hwTyJqzj7wqxNrDtZyyA", "olympa"));
	}

	public static Connection getConnection() throws SQLException {
		return connection.getConnection();
	}
}
