package fr.olympa.api.provider;

import fr.olympa.api.objects.OlympaPlayer;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.plugin.Event;

public class BungeeNewPlayerEvent extends Event {

	boolean cancelled;
	PendingConnection connection;
	OlympaPlayer olympaPlayer;

	public BungeeNewPlayerEvent(PendingConnection connection, OlympaPlayer olympaPlayer) {
		this.connection = connection;
		this.olympaPlayer = olympaPlayer;
		this.cancelled = false;
	}

	public PendingConnection getConnection() {
		return this.connection;
	}

	public OlympaPlayer getOlympaPlayer() {
		return this.olympaPlayer;
	}

	public boolean isCancelled() {
		return this.cancelled;
	}

	public void setCancelled(boolean eventCancelled) {
		this.cancelled = eventCancelled;
	}

}
