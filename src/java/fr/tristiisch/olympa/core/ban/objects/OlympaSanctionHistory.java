package fr.tristiisch.olympa.core.ban.objects;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.google.gson.Gson;

import fr.tristiisch.olympa.api.utils.SpigotUtils;
import fr.tristiisch.olympa.api.utils.Utils;

public class OlympaSanctionHistory {

	public static OlympaSanctionHistory fromJson(final String json) {
		return new Gson().fromJson(json, OlympaSanctionHistory.class);
	}

	private final UUID author;
	private final long time;
	private final OlympaSanctionStatus status;

	private String reason = null;

	public OlympaSanctionHistory(final UUID author, final long time, final OlympaSanctionStatus status) {
		this.author = author;
		this.time = time;
		this.status = status;
	}

	public OlympaSanctionHistory(final UUID author, final OlympaSanctionStatus status) {
		this.author = author;
		this.time = Utils.getCurrentTimeinSeconds();
		this.status = status;
	}

	public OlympaSanctionHistory(final UUID author, final OlympaSanctionStatus status, final String reason) {
		this.author = author;
		this.time = Utils.getCurrentTimeinSeconds();
		this.status = status;
		this.reason = reason;
	}

	public Player getAuthor() {
		return Bukkit.getPlayer(this.author);
	}

	public String getAuthorName() {
		return SpigotUtils.getName(this.author);
	}

	public UUID getAuthorUUID() {
		return this.author;
	}

	public String getReason() {
		return this.reason != null && !this.reason.isEmpty() ? this.reason : "Raison Inconnu";
	}

	public OlympaSanctionStatus getStatus() {
		return this.status;
	}

	public long getTime() {
		return this.time;
	}

	public String toJson() {
		return new Gson().toJson(this);
	}

}
