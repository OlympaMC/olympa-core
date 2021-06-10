package fr.olympa.core.spigot.report.connections;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.common.player.OlympaPlayerInformations;
import fr.olympa.api.common.provider.AccountProvider;
import fr.olympa.api.common.report.OlympaReport;
import fr.olympa.api.common.report.ReportStatus;
import fr.olympa.api.common.report.ReportStatusInfo;
import fr.olympa.api.common.sql.statement.OlympaStatement;
import fr.olympa.api.common.sql.statement.StatementType;
import fr.olympa.api.sql.DbConnection;

public class ReportMySQL {

	static DbConnection dbConnection;

	static String tableName = "reports";
	private static OlympaStatement insertPlayerStatement = new OlympaStatement(StatementType.INSERT, tableName, "target_id", "author_id", "reason", "time", "server", "note", "status_info").returnGeneratedKeys();

	public static void createReport(OlympaReport report) throws SQLException {
		try (PreparedStatement statement = insertPlayerStatement.createStatement()) {
			int i = 1;
			statement.setLong(i++, report.getTargetId());
			statement.setLong(i++, report.getAuthorId());
			statement.setString(i++, report.getReasonName());
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
			insertPlayerStatement.executeUpdate(statement);
			ResultSet resultSet = statement.getGeneratedKeys();
			resultSet.next();
			long id = resultSet.getLong("id");
			resultSet.close();
			report.setId(id);
		}
	}

	private static OlympaStatement updateStatement = new OlympaStatement(StatementType.UPDATE, tableName, "id", new String[] { "target_id", "author_id", "reason", "time", "server", "status_info" });

	public static void updateReport(OlympaReport report) throws SQLException {
		try (PreparedStatement statement = updateStatement.createStatement()) {
			int i = 1;
			statement.setLong(i++, report.getTargetId());
			statement.setLong(i++, report.getAuthorId());
			statement.setString(i++, report.getReasonName());
			statement.setTimestamp(i++, new Timestamp(report.getTime() * 1000L));
			statement.setString(i++, report.getServerName());
			List<ReportStatusInfo> statusInfo = report.getStatusInfo();
			if (statusInfo != null && !statusInfo.isEmpty())
				statement.setString(i++, report.getStatusInfoToJson());
			else
				statement.setObject(i++, null);
			statement.setLong(i, report.getId());
			updateStatement.executeUpdate(statement);
		}
	}

	private static OlympaStatement selectStatement = new OlympaStatement(StatementType.SELECT, tableName, "id", null);

	public static OlympaReport getReport(long id) throws SQLException {
		try (PreparedStatement statement = selectStatement.createStatement()) {
			statement.setLong(1, id);
			OlympaReport report = null;
			ResultSet resultSet = selectStatement.executeQuery(statement);
			if (resultSet.next())
				report = get(resultSet);
			resultSet.close();
			return report;
		}
	}

	private static OlympaStatement selectPlayerStatement = new OlympaStatement(StatementType.SELECT, tableName, "target_id", null);

	public static List<OlympaReport> getReportByTarget(long idTarget) throws SQLException {
		try (PreparedStatement statement = selectPlayerStatement.createStatement()) {
			statement.setLong(1, idTarget);
			List<OlympaReport> reports = new ArrayList<>();
			ResultSet resultSet = selectPlayerStatement.executeQuery(statement);
			while (resultSet.next())
				reports.add(get(resultSet));
			resultSet.close();
			Collections.reverse(reports);
			return reports;
		}
	}

	private static OlympaStatement selectAuthorStatement = new OlympaStatement(StatementType.SELECT, tableName, "author_id", null);

	public static List<OlympaReport> getReportsByAuthor(long idAuthor) throws SQLException {
		try (PreparedStatement statement = selectAuthorStatement.createStatement()) {
			statement.setLong(1, idAuthor);
			List<OlympaReport> reports = new ArrayList<>();
			ResultSet resultSet = selectAuthorStatement.executeQuery(statement);
			while (resultSet.next())
				reports.add(get(resultSet));
			resultSet.close();
			Collections.reverse(reports);
			return reports;
		}

	}

	public static List<OlympaReport> getLastReports(int startNumber, int number) throws SQLException {
		OlympaStatement opStatement = new OlympaStatement(StatementType.SELECT, tableName, null, startNumber, number);
		try (PreparedStatement statement = opStatement.createStatement()) {
			List<OlympaReport> reports = new ArrayList<>();
			ResultSet resultSet = opStatement.executeQuery(statement);
			while (resultSet.next())
				reports.add(get(resultSet));
			resultSet.close();
			Collections.reverse(reports);
			return reports;
		}
	}

	public static Stream<Entry<OlympaPlayerInformations, List<OlympaReport>>> getConnectedReports() throws SQLException {
		Collection<OlympaPlayer> players = AccountProvider.getter().getAll();
		Map<OlympaPlayerInformations, List<OlympaReport>> data = new HashMap<>();
		Set<String> orPlayers = new HashSet<>();
		for (@SuppressWarnings("unused")
		OlympaPlayer player : players)
			orPlayers.add("target_id");
		OlympaStatement opStatement = new OlympaStatement(StatementType.SELECT, tableName, new String[] { "target_id" }, orPlayers.toArray(String[]::new), null, null, 0, 0, new String[] {});
		List<OlympaReport> reports = new ArrayList<>();
		try (PreparedStatement statement = opStatement.createStatement()) {
			int i = 1;
			Iterator<OlympaPlayer> it = players.iterator();
			while (it.hasNext())
				statement.setLong(i++, it.next().getId());
			ResultSet resultSet = opStatement.executeQuery(statement);
			while (resultSet.next())
				reports.add(get(resultSet));
			resultSet.close();
			OlympaPlayerInformations op;
			for (OlympaReport r : reports) {
				if (!ReportStatus.OPEN.equals(r.getStatus())) // TODO remove open reports in OlympaStatement
					continue;
				op = AccountProvider.getter().getPlayerInformations(r.getTargetId());
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
	}

	private static OlympaReport get(ResultSet resultSet) throws SQLException {
		return new OlympaReport(resultSet);
	}

	public ReportMySQL(DbConnection dbConnection) {
		ReportMySQL.dbConnection = dbConnection;
	}
}
