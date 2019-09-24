package fr.tristiisch.olympa.core.report;

import java.util.UUID;

import fr.tristiisch.olympa.api.utils.Utils;
import fr.tristiisch.olympa.core.report.items.ReportReason;

public class OlympaReport {

	final UUID target;
	final UUID author;
	final ReportReason reason;
	final long time = Utils.getCurrentTimeinSeconds();

	final String serverName;

	public OlympaReport(UUID target, UUID author, ReportReason reason, String serverName) {
		this.target = target;
		this.author = author;
		this.reason = reason;
		this.serverName = serverName;
	}

	public UUID getAuthor() {
		return this.author;
	}

	public ReportReason getReason() {
		return this.reason;
	}

	public String getServerName() {
		return this.serverName;
	}

	public UUID getTarget() {
		return this.target;
	}

	public long getTime() {
		return this.time;
	}

}
