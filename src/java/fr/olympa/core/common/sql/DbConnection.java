package fr.olympa.core.common.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.Executors;

import com.mysql.jdbc.Driver;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.common.module.OlympaModule;
import fr.olympa.api.common.sql.DatabaseConnection;

public class DbConnection implements DatabaseConnection {

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

	@Override
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

	@Override
	public boolean isConnected() throws SQLException {
		return connection != null && connection.isValid(0) && !connection.isClosed();
	}

	@Override
	public boolean connect() {
		try {
			Class.forName("org.mariadb.jdbc.Driver");
			Driver.class.getName();
			connection = DriverManager.getConnection(dbcredentials.toURI(), dbcredentials.getUser(), dbcredentials.getPassword());
			connection.setNetworkTimeout(Executors.newSingleThreadExecutor(), 28800);
			boolean isOpen = !connection.isClosed();
			LinkSpigotBungee.getInstance().sendMessage("&e[Database] &7Ouverture d'une nouvelle connexion à la base de données. Résultat &e%s", isOpen ? "§2Réussi" : "§cEchec");
			return isOpen;
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public Connection getConnection() throws SQLException {
		if (isConnected()) {
			if (OlympaModule.DEBUG)
				LinkSpigotBungee.getInstance().sendMessage("&e[Database] &7Récupération de la dernière connecxion restée ouverte &aRéussi");
			return connection;
		}
		connect();
		if (isConnected())
			LinkSpigotBungee.getInstance().sendMessage("&e[Database] &7Echec de la connexion à la base de données.");
		return connection;
	}
}
