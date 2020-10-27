package fr.olympa.core.bungee.ban;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;

import fr.olympa.api.player.OlympaConsole;
import fr.olympa.api.sql.statement.OlympaStatement;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.ban.objects.OlympaSanction;
import fr.olympa.core.bungee.ban.objects.OlympaSanctionHistory;
import fr.olympa.core.bungee.ban.objects.OlympaSanctionStatus;
import fr.olympa.core.bungee.ban.objects.OlympaSanctionType;

public class BanMySQL {

	/**
	CREATE TABLE `server`.`sanctions` (
	`id` INT NOT NULL AUTO_INCREMENT,
	`type_id` INT NULL,
	`target` VARCHAR(36) NULL,
	`reason` VARCHAR(45) NULL,
	`author_uuid` VARCHAR(36) NULL,
	`expires` TIMESTAMP NULL,
	`created` TIMESTAMP NULL,
	`status_id` INT NULL,
	PRIMARY KEY (`id`),
	UNIQUE INDEX `id_UNIQUE` (`id` ASC) VISIBLE);

	 */
	/**
	 * Ajoute un sanction/mute
	 *
	 * @return
	 * @throws SQLException
	 */
	public static long addSanction(OlympaSanction olympaban) throws SQLException {
		OlympaStatement statement = new OlympaStatement("INSERT INTO sanctions (type_id, target, reason, author_uuid, expires, created, status_id) VALUES (?, ?, ?, ?, ?, ?, ?)", true);
		PreparedStatement pstate = statement.getStatement();
		int i = 1;
		pstate.setInt(i++, olympaban.getType().getId());
		pstate.setString(i++, olympaban.getPlayer().toString());
		pstate.setString(i++, olympaban.getReason());
		pstate.setString(i++, olympaban.getAuthor().toString());
		pstate.setTimestamp(i++, new Timestamp(olympaban.getExpires() * 1000L));
		pstate.setTimestamp(i++, new Timestamp(olympaban.getCreated() * 1000L));
		pstate.setLong(i, olympaban.getStatus().getId());
		pstate.executeUpdate();
		ResultSet resultSet = pstate.getGeneratedKeys();
		resultSet.next();
		long id = resultSet.getLong("id");
		resultSet.close();
		return id;
	}

	public static boolean changeCurrentSanction(OlympaSanctionHistory banhistory, long l) {
		OlympaStatement statement = new OlympaStatement("UPDATE sanctions SET `status_id` = ?, `history` = CONCAT_WS(`;`, ?, history) WHERE `id` = ?;");
		try {
			PreparedStatement pstate = statement.getStatement();
			pstate.setInt(1, banhistory.getStatus().getId());
			pstate.setString(2, banhistory.toJson());
			pstate.setLong(3, l);
			int i = statement.executeUpdate();
			if (i != 1)
				throw new SQLException("An error has occurred (" + i + " row(s) affected)");
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static boolean expireSanction(OlympaSanction sanction) {
		OlympaSanctionHistory banhistory = new OlympaSanctionHistory(OlympaConsole.getUniqueId(), OlympaSanctionStatus.EXPIRE);
		sanction.setStatus(OlympaSanctionStatus.EXPIRE);
		sanction.addHistory(banhistory);
		try {
			PreparedStatement pstate = OlympaBungee.getInstance().getDatabase().prepareStatement("UPDATE sanctions SET `status_id` = ?, `history` = CONCAT_WS(`;`, ?, history) WHERE `id` = ?;");
			pstate.setInt(1, sanction.getStatus().getId());
			pstate.setString(2, banhistory.toJson());
			pstate.setLong(3, sanction.getId());
			pstate.executeUpdate();
			pstate.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;

	}

	public static OlympaSanction getSanction(int id) {
		OlympaSanction sanction = null;
		try {
			Statement state = OlympaBungee.getInstance().getDatabase().createStatement();
			ResultSet resultSet = state.executeQuery("SELECT * FROM sanctions WHERE `id` = '" + id + "';");
			if (resultSet.next())
				sanction = getSanction(resultSet);
			state.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return sanction;
	}

	private static OlympaSanction getSanction(ResultSet resultSet) throws SQLException {
		OlympaSanction sanction = new OlympaSanction(
				resultSet.getInt("id"),
				OlympaSanctionType.getByID(resultSet.getInt("type_id")),
				UUID.fromString(resultSet.getString("target")),
				UUID.fromString(resultSet.getString("author_uuid")),
				resultSet.getString("reason"),
				resultSet.getTimestamp("created").getTime() / 1000L,
				resultSet.getTimestamp("expires").getTime() / 1000L,
				OlympaSanctionStatus.getStatus(resultSet.getInt("status_id")));
		String history = resultSet.getString("history");
		if (history != null)
			for (String hist : history.split(","))
				sanction.addHistory(OlympaSanctionHistory.fromJson(hist));
		if (sanction.getStatus() == OlympaSanctionStatus.ACTIVE && sanction.getExpires() != 0 && Utils.getCurrentTimeInSeconds() > sanction.getExpires())
			BanMySQL.expireSanction(sanction);
		return sanction;
	}

	public static OlympaSanction getSanctionActive(Object target) {
		return getSanctionActive(target, null);
	}

	public static OlympaSanction getSanctionActive(Object target, OlympaSanctionType bantype) {
		OlympaSanction sanction = null;
		try {
			String exe = "SELECT * FROM sanctions WHERE `target` = '" + target + "' AND `status_id` = '" + OlympaSanctionStatus.ACTIVE + "'";
			if (bantype != null)
				exe += " AND `type_id` = '" + bantype.getId() + "'";
			Statement state = OlympaBungee.getInstance().getDatabase().createStatement();
			ResultSet resultSet = state.executeQuery(exe);
			if (resultSet.next())
				sanction = getSanction(resultSet);
			state.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}

		return sanction;
	}

	public static List<OlympaSanction> getSanctionByAuthor(UUID authorUUID) {
		List<OlympaSanction> sanctions = new ArrayList<>();
		try {
			Statement state = OlympaBungee.getInstance().getDatabase().createStatement();
			ResultSet resultSet = state.executeQuery("SELECT * FROM sanctions WHERE `author_uuid` = '" + authorUUID + "';");
			while (resultSet.next()) {
				OlympaSanction sanction = getSanction(resultSet);
				if (sanction != null)
					sanctions.add(sanction);
			}
			state.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return Lists.reverse(sanctions);
	}

	public static List<OlympaSanction> getSanctions(Object target) {
		return getSanctions(target, null);
	}

	public static List<OlympaSanction> getSanctions(Object target, OlympaSanctionType bantype) {
		List<OlympaSanction> sanctions = new ArrayList<>();
		try {
			String exe = "SELECT * FROM sanctions WHERE `target` = '" + target + "' AND `status_id` <> '" + OlympaSanctionStatus.DELETE + "';";
			if (bantype != null)
				exe = " AND `type_id` = '" + bantype.getId() + "';";
			Statement state = OlympaBungee.getInstance().getDatabase().createStatement();
			ResultSet resultSet = state.executeQuery(exe);
			while (resultSet.next()) {
				OlympaSanction sanction = getSanction(resultSet);
				if (sanction != null)
					sanctions.add(sanction);
			}
			state.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return Lists.reverse(sanctions);
	}

	public static List<OlympaSanction> getSanctionsActive(Object target) {
		List<OlympaSanction> sanctions = new ArrayList<>();
		try {
			Statement state = OlympaBungee.getInstance().getDatabase().createStatement();
			ResultSet resultSet = state.executeQuery("SELECT * FROM sanctions WHERE `target` = '" + target + "' AND `status_id` = " + OlympaSanctionStatus.ACTIVE);
			while (resultSet.next()) {
				OlympaSanction sanction = getSanction(resultSet);
				if (sanction != null)
					sanctions.add(sanction);
			}
			state.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return sanctions;
	}

	public static List<OlympaSanction> getSanctionsActive(UUID targetUuid, String targetIp) throws SQLException {
		List<OlympaSanction> sanctions = new ArrayList<>();
		Connection connection = OlympaBungee.getInstance().getDatabase();
		PreparedStatement pstate = connection.prepareStatement("SELECT * FROM sanctions WHERE (`target` = ? OR `target` = ?) AND `status_id` = ?;");
		int i = 1;
		pstate.setString(i++, targetUuid.toString());
		pstate.setString(i++, targetIp);
		pstate.setInt(i++, OlympaSanctionStatus.ACTIVE.getId());
		ResultSet resultSet = pstate.executeQuery();
		while (resultSet.next())
			sanctions.add(getSanction(resultSet));
		pstate.close();
		return Lists.reverse(sanctions);
	}

	public static boolean isBanned(String ip) {
		return isSanctionActive(ip, OlympaSanctionType.BANIP);
	}

	public static boolean isBanned(UUID targetUUID) {
		return isSanctionActive(targetUUID, OlympaSanctionType.BAN);
	}

	public static boolean isMuted(UUID targetUUID) {
		return isSanctionActive(targetUUID, OlympaSanctionType.MUTE);
	}

	public static boolean isSanctionActive(Object target, OlympaSanctionType banType) {
		return getSanctionActive(target, banType) != null;
	}
}
