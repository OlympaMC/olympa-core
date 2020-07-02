package fr.olympa.api;

import java.sql.Connection;
import java.sql.SQLException;

public interface LinkSpigotBungee {

	public static final class Provider {
		public static LinkSpigotBungee link;
	}
	
	Connection getDatabase() throws SQLException;

	void launchAsync(Runnable run);

}
