package fr.tristiisch.olympa.core.ban.objects;

import java.util.UUID;

import com.google.gson.Gson;

import fr.tristiisch.emeraldmc.api.bungee.utils.BungeeUtils;
import fr.tristiisch.emeraldmc.api.commons.Utils;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class EmeraldBanHistory {

	public static EmeraldBanHistory fromJson(final String json) {
		return new Gson().fromJson(json, EmeraldBanHistory.class);
	}

	private final UUID author;
	private final long time;
	private final EmeraldBanStatus status;

	private String reason = null;

	public EmeraldBanHistory(final UUID author, final EmeraldBanStatus status) {
		this.author = author;
		this.time = Utils.getCurrentTimeinSeconds();
		this.status = status;
	}

	public EmeraldBanHistory(final UUID author, final EmeraldBanStatus status, final String reason) {
		this.author = author;
		this.time = Utils.getCurrentTimeinSeconds();
		this.status = status;
		this.reason = reason;
	}

	public EmeraldBanHistory(final UUID author, final long time, final EmeraldBanStatus status) {
		this.author = author;
		this.time = time;
		this.status = status;
	}

	public String getAuthorName() {
		return BungeeUtils.getName(this.author);
	}

	public UUID getAuthorUUID() {
		return this.author;
	}

	public ProxiedPlayer getProxiedAuthor() {
		return ProxyServer.getInstance().getPlayer(this.author);
	}

	public String getReason() {
		return this.reason != null ? this.reason : "Raison Inconnu";
	}

	public EmeraldBanStatus getStatus() {
		return this.status;
	}

	public long getTime() {
		return this.time;
	}

	public String toJson() {
		return new Gson().toJson(this);
	}

}
