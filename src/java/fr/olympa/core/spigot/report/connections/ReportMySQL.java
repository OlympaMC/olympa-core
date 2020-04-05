package fr.olympa.core.spigot.report.connections;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import fr.olympa.api.sql.DbConnection;
import fr.olympa.api.sql.OlympaStatement;
import fr.olympa.core.spigot.report.OlympaReport;

public class ReportMySQL {

	static DbConnection dbConnection;

	static String tableName = "server.reports";
	private static OlympaStatement insertPlayerStatement = new OlympaStatement("INSERT INTO " + tableName + " (`target_id`, `author_id` `reason`, `time`, `server`, `status_info`) VALUES (?, ?, ?, ?, ?)", true);

	private static OlympaStatement selectPlayerStatement = new OlympaStatement("SELECT * FROM " + tableName + " WHERE `target_id` = ?", true);
	private static OlympaStatement selectTargetStatement = new OlympaStatement("SELECT * FROM " + tableName + " WHERE `author_id` = ?", true);
	private static OlympaStatement selectStatement = new OlympaStatement("SELECT * FROM " + tableName + " WHERE `id` = ?", true);

	public static long createReport(OlympaReport report) throws SQLException {
		PreparedStatement statement = insertPlayerStatement.getStatement();
		int i = 1;
		statement.setLong(i++, report.getTargetId());
		statement.setLong(i++, report.getAuthorId());
		statement.setInt(i++, report.getReason().getId());
		statement.setDate(i++, new Date(report.getTime() * 1000L));
		statement.setString(i++, report.getServerName());

		statement.executeUpdate();
		ResultSet resultSet = statement.getGeneratedKeys();
		resultSet.next();
		long id = resultSet.getLong("id");
		resultSet.close();
		return id;
	}

	private static OlympaReport get(ResultSet resultSet) throws SQLException {
		return new OlympaReport(resultSet.getLong("id"),
				resultSet.getLong("target_id"),
				resultSet.getLong("author_id"),
				resultSet.getInt("reason"),
				resultSet.getString("server"),
				resultSet.getLong("time"),
				resultSet.getString("status_info"));
	}

	public static OlympaReport getReport(long id) throws SQLException {
		OlympaReport report = null;
		PreparedStatement statement = selectStatement.getStatement();
		statement.setLong(1, id);
		statement.executeUpdate();
		ResultSet resultSet = statement.getGeneratedKeys();
		if (resultSet.next()) {
			report = get(resultSet);
		}
		resultSet.close();
		return report;
	}

	public static List<OlympaReport> getReportByTarget(long idTarget) throws SQLException {
		List<OlympaReport> report = new ArrayList<>();
		PreparedStatement statement = selectPlayerStatement.getStatement();
		statement.setLong(1, idTarget);
		ResultSet resultSet = statement.executeQuery();
		while (resultSet.next()) {
			report.add(get(resultSet));
		}
		resultSet.close();
		return report;
	}

	public static List<OlympaReport> getReportsByAuthor(long idAuthor) throws SQLException {
		List<OlympaReport> report = new ArrayList<>();
		PreparedStatement statement = selectTargetStatement.getStatement();
		statement.setLong(1, idAuthor);
		ResultSet resultSet = statement.executeQuery();
		while (resultSet.next()) {
			report.add(get(resultSet));
		}
		resultSet.close();
		return report;
	}

	public ReportMySQL(DbConnection dbConnection) {
		ReportMySQL.dbConnection = dbConnection;
	}
}
