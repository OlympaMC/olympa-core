package fr.olympa.core.spigot.report.connections;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.common.player.OlympaPlayerInformations;
import fr.olympa.api.common.report.OlympaReport;
import fr.olympa.api.common.report.ReportStatus;
import fr.olympa.api.common.report.ReportStatusInfo;
import fr.olympa.api.common.sql.SQLColumn;
import fr.olympa.api.common.sql.SQLTable;
import fr.olympa.api.common.sql.statement.OlympaStatement;
import fr.olympa.api.common.sql.statement.StatementType;
import fr.olympa.core.common.provider.AccountProvider;
import fr.olympa.core.common.sql.DbConnection;

public class ReportMySQL {

	public static final SQLColumn<OlympaReport> COLUMN_ID = new SQLColumn<OlympaReport>("id", "INT(20) unsigned NOT NULL AUTO_INCREMENT", Types.INTEGER).setPrimaryKey(OlympaReport::getId);
	public static final SQLColumn<OlympaReport> COLUMN_AUTHOR_ID = new SQLColumn<>("author_id", "INT(20) UNSIGNED NOT NULL", Types.INTEGER);
	public static final SQLColumn<OlympaReport> COLUMN_TARGET_ID = new SQLColumn<>("target_id", "INT(20) UNSIGNED NOT NULL", Types.INTEGER);
	public static final SQLColumn<OlympaReport> COLUMN_REASON = new SQLColumn<>("reason", "TEXT NOT NULL", Types.VARCHAR);
	public static final SQLColumn<OlympaReport> COLUMN_TIME = new SQLColumn<>("time", "TIMESTAMP NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()", Types.TIMESTAMP);
	public static final SQLColumn<OlympaReport> COLUMN_SERVER = new SQLColumn<>("server", "VARCHAR(45) NULL DEFAULT NULL", Types.VARCHAR);
	public static final SQLColumn<OlympaReport> COLUMN_STATUS_INFO = new SQLColumn<OlympaReport>("status_info", "TEXT NULL DEFAULT NULL", Types.VARCHAR).setUpdatable();
	public static final SQLColumn<OlympaReport> COLUMN_NOTE = new SQLColumn<>("note", "NULL DEFAULT NULL", Types.VARCHAR);
	private static final List<SQLColumn<OlympaReport>> COLUMNS = Arrays.asList(COLUMN_ID, COLUMN_AUTHOR_ID, COLUMN_TARGET_ID, COLUMN_REASON, COLUMN_TIME, COLUMN_SERVER, COLUMN_STATUS_INFO, COLUMN_NOTE);

	static String tableName = "reports";

	public static SQLTable<OlympaReport> TABLE;
	static {
		try {
			TABLE = new SQLTable<>(tableName, COLUMNS, resultSet -> new OlympaReport(resultSet)).createOrAlter();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	static DbConnection dbConnection;

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

	public static List<OlympaReport> getReportsByAuthor(long idAuthor, int limit) throws SQLException {
		OlympaStatement selectAuthorStatement = new OlympaStatement(StatementType.SELECT, tableName, new String[] { "author_id" }, limit, (String[]) null);
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

	public static List<OlympaReport> getLastReports(int max) throws SQLException {
		OlympaStatement opStatement = new OlympaStatement(StatementType.SELECT, tableName, null, "id", false, 0, max);
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
		OlympaStatement opStatement = new OlympaStatement(StatementType.SELECT, tableName, new String[] { "target_id" }, players.stream().limit(players.size() - 1).toArray(String[]::new), null, null, 0, 0,
				new String[] {});
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
