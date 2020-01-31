package fr.olympa.api.plugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import fr.olympa.api.config.CustomConfig;
import fr.olympa.api.maintenance.MaintenanceStatus;
import fr.olympa.api.sql.DbConnection;
import fr.olympa.api.sql.DbCredentials;

public abstract class OlympaSpigot extends OlympaAPIPlugin implements OlympaCoreInterface, OlympaPluginInterface {

	protected DbConnection database = null;
	protected MaintenanceStatus status;

	public void disable() {
		if (this.database != null) {
			this.database.close();
		}
	}

	public void enable() {
		this.config = new CustomConfig(this, "config");
		if (this.config.hasResource() || this.config.getFile().exists()) {
			this.config.load();
			this.config.saveIfNotExists();

			String statusString = this.config.getString("status");
			if (statusString != null && !statusString.isEmpty()) {
				MaintenanceStatus status2 = MaintenanceStatus.get(statusString);
				if (status2 != null) {
					this.setStatus(status2);
				}
			}

			this.setupDatabase();
		} else {
			this.config = null;
		}
	}

	@Override
	public Connection getDatabase() throws SQLException {
		return this.database.getConnection();
	}

	public PreparedStatement prepareStatement(PreparedStatement previous, String statement) throws SQLException {
		if (previous != null && !previous.isClosed()) return previous;
		return getDatabase().prepareStatement(statement);
	}

	public PreparedStatement prepareStatementGeneratedKeys(PreparedStatement previous, String statement) throws SQLException {
		if (previous != null && !previous.isClosed()) return previous;
		return getDatabase().prepareStatement(statement, Statement.RETURN_GENERATED_KEYS);
	}

	@Override
	public MaintenanceStatus getStatus() {
		return this.status;
	}

	@Override
	public void setStatus(MaintenanceStatus status) {
		this.status = status;
	}

	private void setupDatabase() {
		DbCredentials dbcredentials = new DbCredentials(this.getConfig());
		if (dbcredentials.getUser() == null) {
			return;
		}
		this.database = new DbConnection(dbcredentials);
		if (this.database.connect()) {
			this.sendMessage("&aConnexion à la base de donnée &2" + dbcredentials.getDatabase() + "&a établie.");
		} else {
			this.sendMessage("&cConnexion à la base de donnée &4" + dbcredentials.getDatabase() + "&c impossible.");
		}
	}
}
