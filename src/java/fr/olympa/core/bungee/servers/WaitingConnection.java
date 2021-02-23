package fr.olympa.core.bungee.servers;

import java.util.UUID;

import fr.olympa.api.server.OlympaServer;
import net.md_5.bungee.api.scheduler.ScheduledTask;

public class WaitingConnection {

	public UUID uuid;
	public OlympaServer olympaServer;
	public ScheduledTask task;
	public boolean isChangeServer;

	public WaitingConnection(UUID uuid, OlympaServer olympaServer, ScheduledTask task, boolean isChangeServer) {
		this.uuid = uuid;
		this.olympaServer = olympaServer;
		this.task = task;
		this.isChangeServer = isChangeServer;
	}
}
