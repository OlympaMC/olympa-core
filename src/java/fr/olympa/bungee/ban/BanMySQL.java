package fr.olympa.core.ban;

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

import fr.olympa.OlympaCore;
import fr.olympa.api.objects.OlympaConsole;
import fr.olympa.api.utils.Utils;
import fr.olympa.spigot.core.ban.objects.OlympaSanction;
import fr.olympa.spigot.core.ban.objects.OlympaSanctionHistory;
import fr.olympa.spigot.core.ban.objects.OlympaSanctionStatus;
import fr.olympa.spigot.core.ban.objects.OlympaSanctionType;

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
	 */
	public static boolean addSanction(OlympaSanction emeraldban) {
		try {
			PreparedStatement pstate = OlympaCore.getInstance().getDatabase()
					.prepareStatement("INSERT INTO sanctions (id, type_id, target, reason, author_uuid, expires, created, status_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
			pstate.setInt(1, emeraldban.getId());
			pstate.setInt(2, emeraldban.getType().getId());
			pstate.setString(3, emeraldban.getPlayer().toString());
			pstate.setString(4, emeraldban.getReason());
			pstate.setString(5, emeraldban.getAuthor().toString());
			pstate.setTimestamp(6, new Timestamp(emeraldban.getExpires() * 1000L));
			pstate.setTimestamp(7, new Timestamp(emeraldban.getCreated() * 1000L));
			pstate.setLong(8, emeraldban.getStatus().getId());
			pstate.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static boolean changeCurrentSanction(OlympaSanctionHistory banhistory, int banid) {
		try {
			PreparedStatement pstate = OlympaCore.getInstance().getDatabase().prepareStatement("UPDATE sanctions SET `status_id` = ?, `history` = CONCAT_WS(';', ?, history) WHERE `id` = ?;");
			pstate.setInt(1, banhistory.getStatus().getId());
			pstate.setString(2, banhistory.toJson());
			pstate.setInt(3, banid);
			int i = pstate.executeUpdate();
			if (i != 1) {
				throw new SQLException("An error has occurred (" + i + " row(s) affected)");
			}
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
			PreparedStatement pstate = OlympaCore.getInstance().getDatabase().prepareStatement("UPDATE sanctions SET `status_id` = ?, `history` = CONCAT_WS(';', ?, history) WHERE `id` = ?;");
			pstate.setInt(1, sanction.getStatus().getId());
			pstate.setString(2, banhistory.toJson());
			pstate.setInt(3, sanction.getId());
			pstate.executeUpdate();
			pstate.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;

	}

	public static OlympaSanction getBanActive(UUID targetUuid, String targetIp) throws SQLException {
		Connection connection = OlympaCore.getInstance().getDatabase();
		PreparedStatement pstate = connection.prepareStatement("SELECT * FROM sanctions WHERE (`target` = '?' OR `target` = '?') AND (`type_id` = '?' OR `type_id` = '?') AND `status_id` = ?;");
		int i = 1;
		pstate.setString(i++, targetUuid.toString());
		pstate.setString(i++, targetIp);
		pstate.setInt(i++, OlympaSanctionType.BAN.getId());
		pstate.setInt(i++, OlympaSanctionType.BANIP.getId());
		pstate.setInt(i++, OlympaSanctionStatus.ACTIVE.getId());
		ResultSet resultSet = pstate.executeQuery();
		while (resultSet.next()) {
			OlympaSanction sanction = getSanction(resultSet);
			if (!OlympaSanctionStatus.EXPIRE.equals(sanction.getStatus())) {
				return getSanction(resultSet);
			}
		}
		pstate.close();
		return null;
	}

	public static OlympaSanction getSanction(int id) {
		OlympaSanction sanction = null;
		try {
			Statement state = OlympaCore.getInstance().getDatabase().createStatement();
			ResultSet resultSet = state.executeQuery("SELECT * FROM sanctions WHERE `id` = '" + id + "';");
			if (resultSet.next()) {
				sanction = getSanction(resultSet);
			}
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
		if (history != null) {
			for (String hist : history.split(",")) {
				sanction.addHistory(OlympaSanctionHistory.fromJson(hist));
			}
		}
		if (sanction.getStatus() == OlympaSanctionStatus.ACTIVE && sanction.getExpires() != 0 && Utils.getCurrentTimeinSeconds() > sanction.getExpires()) {
			BanMySQL.expireSanction(sanction);
		}
		return sanction;
	}

	public static OlympaSanction getSanctionActive(Object target) {
		return getSanctionActive(target, null);
	}

	public static OlympaSanction getSanctionActive(Object target, OlympaSanctionType bantype) {
		OlympaSanction sanction = null;
		try {
			String exe = "SELECT * FROM sanctions WHERE `target` = '" + target + "' AND `status_id` = '" + OlympaSanctionStatus.ACTIVE + "'";
			if (bantype != null) {
				exe += " AND `type_id` = '" + bantype.getId() + "'";
			}
			Statement state = OlympaCore.getInstance().getDatabase().createStatement();
			ResultSet resultSet = state.executeQuery(exe);
			if (resultSet.next()) {
				sanction = getSanction(resultSet);
			}
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
			Statement state = OlympaCore.getInstance().getDatabase().createStatement();
			ResultSet resultSet = state.executeQuery("SELECT * FROM sanctions WHERE `author_uuid` = '" + authorUUID + "';");
			while (resultSet.next()) {
				OlympaSanction sanction = getSanction(resultSet);
				if (sanction != null) {
					sanctions.add(sanction);
				}
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
			if (bantype != null) {
				exe = " AND `type_id` = '" + bantype.getId() + "';";
			}
			Statement state = OlympaCore.getInstance().getDatabase().createStatement();
			ResultSet resultSet = state.executeQuery(exe);
			while (resultSet.next()) {
				OlympaSanction sanction = getSanction(resultSet);
				if (sanction != null) {
					sanctions.add(sanction);
				}
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
			Statement state = OlympaCore.getInstance().getDatabase().createStatement();
			ResultSet resultSet = state.executeQuery("SELECT * FROM sanctions WHERE `target` = '" + target + "' AND `status_id` = " + OlympaSanctionStatus.ACTIVE);
			while (resultSet.next()) {
				OlympaSanction sanction = getSanction(resultSet);
				if (sanction != null) {
					sanctions.add(sanction);
				}
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
		Statement state = OlympaCore.getInstance().getDatabase().createStatement();
		ResultSet resultSet = state.executeQuery("SELECT * FROM sanctions WHERE (`target` = '" + targetUuid + "' OR `target` = '" + targetIp + "') AND `status_id` = " + OlympaSanctionStatus.ACTIVE);
		while (resultSet.next()) {
			OlympaSanction sanction = getSanction(resultSet);
			if (sanction != null) {
				sanctions.add(sanction);
			}
		}
		state.close();
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
