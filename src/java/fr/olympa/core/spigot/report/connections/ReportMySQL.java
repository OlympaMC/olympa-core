package fr.olympa.core.spigot.report.connections;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.player.OlympaPlayerInformations;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.report.OlympaReport;
import fr.olympa.api.report.ReportStatus;
import fr.olympa.api.report.ReportStatusInfo;
import fr.olympa.api.sql.DbConnection;
import fr.olympa.api.sql.statement.OlympaStatement;
import fr.olympa.api.sql.statement.StatementType;

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
		if (statusInfo != null && !statusInfo.isEmpty())
			statement.setString(i, report.getStatusInfoToJson());
		else
			statement.setObject(i, null);
		insertPlayerStatement.execute();
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
		if (statusInfo != null && !statusInfo.isEmpty())
			statement.setString(i++, report.getStatusInfoToJson());
		else
			statement.setObject(i++, null);
		statement.setLong(i, report.getId());
		updateStatement.execute();
		statement.close();
	}

	private static OlympaStatement selectStatement = new OlympaStatement(StatementType.SELECT, tableName, "id", null);

	public static OlympaReport getReport(long id) throws SQLException {
		OlympaReport report = null;
		PreparedStatement statement = selectStatement.getStatement();
		statement.setLong(1, id);
		ResultSet resultSet = selectStatement.executeQuery();
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
		ResultSet resultSet = selectPlayerStatement.executeQuery();
		while (resultSet.next())
			report.add(get(resultSet));
		resultSet.close();
		return report;
	}

	private static OlympaStatement selectAuthorStatement = new OlympaStatement(StatementType.SELECT, tableName, "author_id", null);

	public static List<OlympaReport> getReportsByAuthor(long idAuthor) throws SQLException {
		List<OlympaReport> report = new ArrayList<>();
		PreparedStatement statement = selectAuthorStatement.getStatement();
		statement.setLong(1, idAuthor);
		ResultSet resultSet = selectAuthorStatement.executeQuery();
		while (resultSet.next())
			report.add(get(resultSet));
		resultSet.close();
		return report;
	}

	public static List<OlympaReport> getLastReports(int startNumber, int number) throws SQLException {

		OlympaStatement opStatement = new OlympaStatement(StatementType.SELECT, tableName, new String[] { "target_id" }, startNumber, number, new String[] {});
		List<OlympaReport> report = new ArrayList<>();
		ResultSet resultSet = opStatement.executeQuery();
		while (resultSet.next())
			report.add(get(resultSet));
		resultSet.close();
		return report;
	}

	public static Stream<Entry<OlympaPlayerInformations, List<OlympaReport>>> getMaxReports() throws SQLException {
		List<OlympaPlayer> players = (List<OlympaPlayer>) AccountProvider.getAll();
		Map<OlympaPlayerInformations, List<OlympaReport>> data = new HashMap<>();
		Set<String> orPlayers = new HashSet<>();
		for (int i = 1; i < players.size(); i++)
			orPlayers.add("target_id");
		OlympaStatement opStatement = new OlympaStatement(StatementType.SELECT, tableName, new String[] { "target_id" }, orPlayers.toArray(String[]::new), null, null, 0, 0, new String[] {});
		List<OlympaReport> reports = new ArrayList<>();
		PreparedStatement staterment = opStatement.getStatement();
		int i = 0;
		while (i < players.size())
			staterment.setLong(i, players.get(i++).getId());
		ResultSet resultSet = opStatement.executeQuery();
		while (resultSet.next())
			reports.add(get(resultSet));
		resultSet.close();
		OlympaPlayerInformations op;
		for (OlympaReport r : reports) {
			if (!ReportStatus.OPEN.equals(r.getStatus()))
				continue;
			op = AccountProvider.getPlayerInformations(r.getTargetId());
			List<OlympaReport> listReports = data.get(op);
			if (listReports == null) {
				listReports = new ArrayList<>();
				listReports.add(r);
				data.put(op, listReports);
			} else
				listReports.add(r);
		}
		return data.entrySet().stream().sorted((o1, o2) -> o1.getValue().size() - o2.getValue().size());
	}

	private static OlympaReport get(ResultSet resultSet) throws SQLException {
		return new OlympaReport(resultSet);
	}

	public ReportMySQL(DbConnection dbConnection) {
		ReportMySQL.dbConnection = dbConnection;
	}
}
