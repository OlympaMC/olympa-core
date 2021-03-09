package fr.olympa.core.spigot.report.connections;

import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import fr.olympa.api.report.ReportReason;
import fr.olympa.api.sql.SQLColumn;
import fr.olympa.api.sql.SQLTable;

public class ReportReasonSQL {

	private static final SQLColumn<ReportReason> COLUMN_ID = new SQLColumn<ReportReason>("id", "INT(20) unsigned NOT NULL AUTO_INCREMENT", Types.INTEGER).setPrimaryKey(ReportReason::getId);
	private static final SQLColumn<ReportReason> COLUMN_NAME = new SQLColumn<ReportReason>("name", "VARCHAR(32) NOT NULL", Types.VARCHAR).setUpdatable();
	private static final SQLColumn<ReportReason> COLUMN_REASON = new SQLColumn<ReportReason>("reason", "TEXT NOT NULL", Types.VARCHAR).setUpdatable();

	static final List<SQLColumn<ReportReason>> COLUMNS = Arrays.asList(COLUMN_ID, COLUMN_NAME, COLUMN_REASON);
	static SQLTable<ReportReason> table;

	static {
		try {
			table = new SQLTable<>("reports_reportreason", COLUMNS, rs -> {
				try {
					return new ReportReason(rs.getInt(COLUMN_ID.getName()), rs.getString(COLUMN_NAME.getName()), rs.getString(COLUMN_REASON.getName()));
				} catch (IllegalArgumentException | SQLException e) {
					e.printStackTrace();
				}
				return null;
			}).createOrAlter();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void update() throws SQLException {
		Collection<ReportReason> collection = ReportReason.values();
		List<ReportReason> listDb = table.selectAll();
		for (ReportReason rr : collection)
			if (listDb.stream().noneMatch(rr2 -> rr.isSame(rr2)))
				table.insert(rr.getId(), rr.getItem(), rr.getReason());
			else {
				ReportReason rr3 = listDb.stream().filter(rr2 -> rr.isSame(rr2) && rr.needUpdate(rr2)).findFirst().orElse(null);
				if (rr3 != null)
					if (rr.getName().equals(rr3.getName()))
						COLUMN_NAME.updateAsync(rr3, Map.of(COLUMN_ID, rr.getId(), COLUMN_REASON, rr.getReason()), null, null);
					else if (rr.getId() == rr3.getId())
						COLUMN_ID.updateAsync(rr3, Map.of(COLUMN_NAME, rr.getName(), COLUMN_REASON, rr.getReason()), null, null);
			}
		//		for (ReportReason rrDb : listDb)
		//			if (listDb.stream().noneMatch(rr2 -> rrDb.isSame(rr2)))
		//				table.insert(rrDb.getId(), rrDb.getItem(), rrDb.getReason());
	}
}
