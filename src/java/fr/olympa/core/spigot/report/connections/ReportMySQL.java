package fr.olympa.core.spigot.report.connections;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import fr.olympa.api.report.OlympaReport;
import fr.olympa.api.report.ReportStatusInfo;
import fr.olympa.api.sql.DbConnection;
import fr.olympa.api.sql.OlympaStatement;
import fr.olympa.api.sql.StatementType;

public class ReportMySQL {

	static DbConnection dbConnection;

	static String tableName = "reports";
	private static OlympaStatement insertPlayerStatement = new OlympaStatement(StatementType.INSERT, tableName, "target_id", "author_id", "reason", "time", "server", "note", "status_info");

	public static void createReport(OlympaReport report) throws SQLException {
		PreparedStatement statement = insertPlayerStatement.getStatement();
		int i = 1;
		statement.setLong(i++, report.getTargetId());
		statement.setLong(i++, report.getAuthorId());
		statement.setInt(i++, report.getReason().getId());
		statement.setTimestamp(i++, new Timestamp(report.getTime() * 1000L));
		statement.setString(i++, report.getServerName());
		if (report.getNote() != null)
			statement.setString(i++, report.getNote());
		else
			statement.setObject(i++, null);
		List<ReportStatusInfo> statusInfo = report.getStatusInfo();
		if (statusInfo != null && statusInfo.isEmpty())
			statement.setString(i++, report.getStatusInfoToJson());
		else
			statement.setObject(i++, null);
		insertPlayerStatement.execute(statement);
		ResultSet resultSet = statement.getGeneratedKeys();
		resultSet.next();
		long id = resultSet.getLong("id");
		resultSet.close();
		statement.close();
		report.setId(id);
	}

	private static OlympaStatement updateStatement = new OlympaStatement(StatementType.UPDATE, tableName, "id", new String[] { "target_id", "author_id", "reason", "time", "server", "status_info" });

	public static void updateReport(OlympaReport report) throws SQLException {
		PreparedStatement statement = updateStatement.getStatement();
		int i = 1;
		statement.setLong(i++, report.getTargetId());
		statement.setLong(i++, report.getAuthorId());
		statement.setInt(i++, report.getReason().getId());
		statement.setTimestamp(i++, new Timestamp(report.getTime() * 1000L));
		statement.setString(i++, report.getServerName());
		List<ReportStatusInfo> statusInfo = report.getStatusInfo();
		if (statusInfo != null && statusInfo.isEmpty())
			statement.setString(i++, report.getStatusInfoToJson());
		else
			statement.setObject(i++, null);
		statement.setLong(i++, report.getId());
		insertPlayerStatement.execute(statement);
		statement.close();
	}

	private static OlympaStatement selectStatement = new OlympaStatement(StatementType.SELECT, tableName, "id", null);

	public static OlympaReport getReport(long id) throws SQLException {
		OlympaReport report = null;
		PreparedStatement statement = selectStatement.getStatement();
		statement.setLong(1, id);
		ResultSet resultSet = insertPlayerStatement.executeQuery(statement);
		if (resultSet.next())
			report = get(resultSet);
		resultSet.close();
		return report;
	}

	private static OlympaStatement selectPlayerStatement = new OlympaStatement(StatementType.SELECT, tableName, "target_id", null);

	public static List<OlympaReport> getReportByTarget(long idTarget) throws SQLException {
		List<OlympaReport> report = new ArrayList<>();
		PreparedStatement statement = selectPlayerStatement.getStatement();
		statement.setLong(1, idTarget);
		ResultSet resultSet = selectPlayerStatement.executeQuery(statement);
		while (resultSet.next())
			report.add(get(resultSet));
		resultSet.close();
		return report;
	}

	private static OlympaStatement selectTargetStatement = new OlympaStatement(StatementType.SELECT, tableName, "author_id", null);

	public static List<OlympaReport> getReportsByAuthor(long idAuthor) throws SQLException {
		List<OlympaReport> report = new ArrayList<>();
		PreparedStatement statement = selectTargetStatement.getStatement();
		statement.setLong(1, idAuthor);
		ResultSet resultSet = selectTargetStatement.executeQuery(statement);
		while (resultSet.next())
			report.add(get(resultSet));
		resultSet.close();
		return report;
	}

	private static OlympaReport get(ResultSet resultSet) throws SQLException {
		return new OlympaReport(resultSet);
	}

	public ReportMySQL(DbConnection dbConnection) {
		ReportMySQL.dbConnection = dbConnection;
	}
}
