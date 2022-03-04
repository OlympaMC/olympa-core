package fr.olympa.core.bungee.servers;

import java.util.UUID;

import javax.annotation.Nullable;

import fr.olympa.api.common.server.OlympaServer;
import fr.olympa.api.common.server.ServerInfoAdvancedBungee;
import net.md_5.bungee.api.scheduler.ScheduledTask;

public class WaitingConnection {

	public UUID uuid;
	@Nullable
	public OlympaServer olympaServer;
	@Nullable
	public ServerInfoAdvancedBungee specificServer;
	public ScheduledTask task;
	public boolean isChangeServer;

	public WaitingConnection(UUID uuid, OlympaServer olympaServer, ScheduledTask task, boolean isChangeServer) {
		this.uuid = uuid;
		this.olympaServer = olympaServer;
		this.task = task;
		this.isChangeServer = isChangeServer;
	}

	public WaitingConnection(UUID uuid, ServerInfoAdvancedBungee specificServer, ScheduledTask task, boolean isFromThisServer) {
		this.uuid = uuid;
		this.specificServer = specificServer;
		this.task = task;
		this.isChangeServer = isFromThisServer;
	}
	
	
}
