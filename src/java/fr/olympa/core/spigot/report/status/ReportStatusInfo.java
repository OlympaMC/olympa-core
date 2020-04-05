package fr.olympa.core.spigot.report.status;

import fr.olympa.api.utils.Utils;

public class ReportStatusInfo {

	String note;
	long time;
	ReportStatus status;
	long idAuthor;

	public ReportStatusInfo(ReportStatus status, String note) {
		this.status = status;
		this.note = note;
	}

	public ReportStatusInfo(String note, long idAuthor, ReportStatus status) {
		this.note = note;
		this.idAuthor = idAuthor;
		this.status = status;
		time = Utils.getCurrentTimeInSeconds();
	}

	public String getNote() {
		return note;
	}

	public ReportStatus getStatus() {
		return status;
	}

	public long getTime() {
		return time;
	}
}
