package fr.olympa.core.spigot;

import java.sql.Connection;
import java.sql.SQLException;

import fr.olympa.api.config.CustomConfig;
import fr.olympa.api.maintenance.MaintenanceStatus;
import fr.olympa.api.plugin.OlympaAPIPlugin;
import fr.olympa.api.sql.DbConnection;
import fr.olympa.api.sql.DbCredentials;
import fr.olympa.api.task.TaskManager;

public abstract class OlympaSpigot extends OlympaAPIPlugin {

	protected DbConnection database = null;

	public void disable() {
		if (this.database != null) {
			this.database.close();
		}
	}

	public void enable() {
		this.task = new TaskManager(this);

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
