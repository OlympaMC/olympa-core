package fr.olympa.core.bungee.login.events;

import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.core.bungee.utils.BungeeUtils;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Event;

public class OlympaPlayerLoginEvent extends Event {

	private boolean cancelled = false;
	private OlympaPlayer olympaPlayer;
	private ProxiedPlayer player;
	private String reason;

	public OlympaPlayerLoginEvent(OlympaPlayer olympaPlayer, ProxiedPlayer player) {
		this.olympaPlayer = olympaPlayer;
		this.player = player;
	}

	@SuppressWarnings("deprecation")
	public boolean cancelIfNeeded() {
		if (!this.cancelled) {
			return false;
		}
		if (this.reason != null && !this.reason.isEmpty()) {
			this.player.disconnect(this.reason);
		} else {
			this.player.disconnect();
		}
		return true;
	}

	@SuppressWarnings("deprecation")
	public String getIp() {
		return this.player.getAddress().getAddress().getHostAddress();
	}

	public OlympaPlayer getOlympaPlayer() {
		return this.olympaPlayer;
	}

	public ProxiedPlayer getPlayer() {
		return this.player;
	}

	public String getReason() {
		return this.reason;
	}

	public boolean isCancelled() {
		return this.cancelled;
	}

	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	public void setCancelReason(String reason) {
		this.reason = BungeeUtils.connectScreen(reason);
	}

}
