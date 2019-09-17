package fr.tristiisch.olympa.core.datamanagment.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.Executors;

import fr.tristiisch.olympa.api.plugin.OlympaPlugin;

public class DbConnection {

	final DbCredentials dbcredentials;
	Connection connection;

	public DbConnection(final DbCredentials dbcredentials) {
		this.dbcredentials = dbcredentials;
		this.connect();
	}

	void close() throws SQLException {
		if (this.connection != null && !this.connection.isClosed()) {
			this.connection.close();
		}
	}

	private void connect() {
		try {
			Class.forName("org.mariadb.jdbc.Driver");
			this.connection = DriverManager.getConnection(this.dbcredentials.toURI(), this.dbcredentials.getUser(), this.dbcredentials.getPassword());
			this.connection.setNetworkTimeout(Executors.newSingleThreadExecutor(), 28800);
			OlympaPlugin.getInstance().sendMessage("&aConnexion à la base de donnée établie");
		} catch (final SQLException | ClassNotFoundException e) {
			OlympaPlugin.getInstance().sendMessage("&cConnexion à la base de donnée impossible");
			e.printStackTrace();
		}
	}

	public Connection getConnection() throws SQLException {
		if (this.connection != null && !this.connection.isClosed()) {
			return this.connection;
		}
		this.connect();
		return this.connection;
	}
}
