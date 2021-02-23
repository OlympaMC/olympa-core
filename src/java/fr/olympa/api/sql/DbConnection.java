package fr.olympa.api.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.Executors;

import com.mysql.jdbc.Driver;

public class DbConnection {

	DbCredentials dbcredentials;
	Connection connection;

	public DbConnection(DbCredentials dbcredentials) {
		this.dbcredentials = dbcredentials;
	}

	public void updateCredentials(DbCredentials dbcredentials) {
		this.dbcredentials = dbcredentials;
	}

	public boolean isSameCredentials(DbCredentials dbcredentials) {
		return this.dbcredentials.equals(dbcredentials);
	}

	public boolean close() {
		try {
			if (connection != null && !connection.isClosed()) {
				connection.close();
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean connect() {
		try {
			Class.forName("org.mariadb.jdbc.Driver");
			Driver.class.getName();
			connection = DriverManager.getConnection(dbcredentials.toURI(), dbcredentials.getUser(), dbcredentials.getPassword());
			connection.setNetworkTimeout(Executors.newSingleThreadExecutor(), 28800);
			System.out.println("Opened database connection.");
			return !connection.isClosed();
		} catch (final SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return false;
	}

	public Connection getConnection() throws SQLException {
		if (connection != null && connection.isValid(0))
			return connection;
		connect();
		return connection;
	}
}
