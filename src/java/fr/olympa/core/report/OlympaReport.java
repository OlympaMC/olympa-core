package fr.olympa.core.report;

import java.util.UUID;

import fr.olympa.api.utils.Utils;
import fr.olympa.core.report.items.ReportReason;

public class OlympaReport {

	UUID target;
	UUID author;
	ReportReason reason;
	long time = Utils.getCurrentTimeinSeconds();

	String serverName;

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
