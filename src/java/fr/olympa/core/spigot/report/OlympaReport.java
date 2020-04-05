package fr.olympa.core.spigot.report;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import fr.olympa.api.utils.Utils;
import fr.olympa.core.spigot.report.items.ReportReason;
import fr.olympa.core.spigot.report.status.ReportStatus;
import fr.olympa.core.spigot.report.status.ReportStatusInfo;

public class OlympaReport {

	long id = 0;
	long targetId;
	long authorId;
	ReportReason reason;
	List<ReportStatusInfo> statusInfo = new ArrayList<>();
	long time;

	String serverName;

	public OlympaReport(long id, long targetId, long authorId, int reasonId, String serverName, long time, String statusInfo) {
		this.authorId = id;
		this.targetId = targetId;
		this.authorId = authorId;
		reason = ReportReason.get(reasonId);
		this.serverName = serverName;
		this.time = time;
		if (statusInfo != null) {
			Type founderListType = new TypeToken<ArrayList<ReportStatusInfo>>() {
			}.getType();
			statusInfo = new Gson().fromJson(statusInfo, founderListType);
		}
	}

	public OlympaReport(long targetId, long authorId, ReportReason reason, String serverName, String note) {
		this.targetId = targetId;
		this.authorId = authorId;
		this.reason = reason;
		this.serverName = serverName;
		time = Utils.getCurrentTimeInSeconds();
		statusInfo.add(new ReportStatusInfo(ReportStatus.OPEN, note));
	}

	@Deprecated
	public UUID getAuthor() {
		return null;
	}

	public long getAuthorId() {
		return authorId;
	}

	public long getId() {
		return id;
	}

	public ReportReason getReason() {
		return reason;
	}

	public String getServerName() {
		return serverName;
	}

	public ReportStatus getStatus() {
		return statusInfo.get(0).getStatus();
	}

	public List<ReportStatusInfo> getStatusInfo() {
		return statusInfo;
	}

	public String getStatusString() {
		return new Gson().toJson(this);
	}

	@Deprecated
	public UUID getTarget() {
		return null;
	}

	public long getTargetId() {
		return targetId;
	}

	public long getTime() {
		return time;
	}

	public void setId(long id) {
		this.id = id;
	}
}
