package fr.olympa.api.commun.provider;

import fr.olympa.api.common.player.OlympaPlayer;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.plugin.Event;

public class BungeeNewPlayerEvent extends Event {

	boolean cancelled;
	PendingConnection connection;
	OlympaPlayer olympaPlayer;
	String cancelReason;

	public BungeeNewPlayerEvent(PendingConnection connection, OlympaPlayer olympaPlayer) {
		this.connection = connection;
		this.olympaPlayer = olympaPlayer;
		cancelled = false;
	}

	public String getCancelReason() {
		return cancelReason;
	}

	public PendingConnection getConnection() {
		return connection;
	}

	public OlympaPlayer getOlympaPlayer() {
		return olympaPlayer;
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public void setCancelled(boolean eventCancelled) {
		cancelled = eventCancelled;
	}

	public void setCancelReason(String cancelReason) {
		this.cancelReason = cancelReason;
	}

}
