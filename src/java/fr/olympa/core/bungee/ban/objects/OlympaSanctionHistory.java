package fr.olympa.core.bungee.ban.objects;

import fr.olympa.api.player.OlympaConsole;
import fr.olympa.api.utils.GsonCustomizedObjectTypeAdapter;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.utils.BungeeUtils;

public class OlympaSanctionHistory {

	public static OlympaSanctionHistory fromJson(String json) {
		return GsonCustomizedObjectTypeAdapter.GSON.fromJson(json, OlympaSanctionHistory.class);
	}

	private long author;
	private long time;
	private OlympaSanctionStatus status;

	private String reason = null;

	public OlympaSanctionHistory(OlympaSanctionStatus status) {
		author = OlympaConsole.getId();
		time = Utils.getCurrentTimeInSeconds();
		this.status = status;
	}

	public OlympaSanctionHistory(long author, long time, OlympaSanctionStatus status) {
		this.author = author;
		this.time = time;
		this.status = status;
	}

	public OlympaSanctionHistory(long author, OlympaSanctionStatus status) {
		this.author = author;
		time = Utils.getCurrentTimeInSeconds();
		this.status = status;
	}

	public OlympaSanctionHistory(long author, OlympaSanctionStatus status, String reason) {
		this.author = author;
		time = Utils.getCurrentTimeInSeconds();
		this.status = status;
		this.reason = reason;
	}

	//	public Player getAuthor() {
	//		return Bukkit.getPlayer(author);
	//	}

	public String getAuthorName() {
		return BungeeUtils.getName(author);
	}

	public long getAuthorId() {
		return author;
	}

	public String getReason() {
		return reason != null && !reason.isEmpty() ? reason : "Raison Inconnu";
	}

	public OlympaSanctionStatus getStatus() {
		return status;
	}

	public long getTime() {
		return time;
	}

	public String toJson() {
		return GsonCustomizedObjectTypeAdapter.GSON.toJson(this);
	}

}
