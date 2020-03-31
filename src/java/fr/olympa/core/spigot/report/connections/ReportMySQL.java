package fr.olympa.core.spigot.report.connections;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import fr.olympa.api.sql.DbConnection;
import fr.olympa.api.sql.OlympaStatement;
import fr.olympa.core.spigot.report.OlympaReport;

public class ReportMySQL {

	public ReportMySQL(DbConnection dbConnection) {
		ReportMySQL.dbConnection = dbConnection;
	}

	static DbConnection dbConnection;
	static String tableName = "server.reports";

	private static OlympaStatement insertPlayerStatement = new OlympaStatement("INSERT INTO " + tableName + " (`author`, `target`, `reason`, `time`, `server`) VALUES (?, ?, ?, ?, ?)",
			true);

	public static long createReport(OlympaReport report) throws SQLException {
		PreparedStatement statement = insertPlayerStatement.getStatement();
		int i = 1;
		statement.setString(i++, report.getAuthor().toString());
		statement.setString(i++, report.getTarget().toString());
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
}
