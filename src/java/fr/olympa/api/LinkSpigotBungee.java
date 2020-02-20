package fr.olympa.api;

import java.sql.Connection;
import java.sql.SQLException;

public interface LinkSpigotBungee {

	public void launchAsync(Runnable run);

	public Connection getDatabase() throws SQLException;

	public static final class Provider {
		public static LinkSpigotBungee link;
	}

}
