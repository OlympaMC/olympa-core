package fr.tristiisch.olympa.core.ban;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;

import fr.tristiisch.olympa.api.objects.OlympaConsole;
import fr.tristiisch.olympa.api.utils.Utils;
import fr.tristiisch.olympa.core.ban.objects.EmeraldBan;
import fr.tristiisch.olympa.core.ban.objects.EmeraldBanHistory;
import fr.tristiisch.olympa.core.ban.objects.EmeraldBanStatus;
import fr.tristiisch.olympa.core.ban.objects.EmeraldBanType;
import fr.tristiisch.olympa.core.datamanagment.sql.DatabaseManager;

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
	 * Ajoute un ban/mute
	 *
	 * @return
	 */
	public static boolean addSanction(final EmeraldBan emeraldban) {
		try {
			final PreparedStatement pstate = DatabaseManager.getConnection()
					.prepareStatement("INSERT INTO sanctions (id, type_id, target, reason, author_uuid, expires, created, status_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
			pstate.setInt(1, emeraldban.getId());
			pstate.setInt(2, emeraldban.getType().getInteger());
			pstate.setString(3, emeraldban.getPlayer().toString());
			pstate.setString(4, emeraldban.getReason());
			pstate.setString(5, emeraldban.getAuthor().toString());
			pstate.setLong(6, emeraldban.getExpires());
			pstate.setLong(7, emeraldban.getCreated());
			pstate.setLong(8, emeraldban.getStatus().getId());
			pstate.executeUpdate();
		} catch (final SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Change le status_id d'un ban
	 */
	public static boolean changeCurrentSanction(final EmeraldBanHistory banhistory, final int banid) {
		try {
			final PreparedStatement pstate = DatabaseManager.getConnection().prepareStatement("UPDATE sanctions SET `status_id` = ?, `history` = CONCAT_WS(';', ?, history) WHERE `id` = ?;");
			pstate.setInt(1, banhistory.getStatus().getId());
			pstate.setString(2, banhistory.toJson());
			pstate.setInt(3, banid);
			final int i = pstate.executeUpdate();
			if (i != 1) {
				throw new SQLException("An error has occurred (" + i + " row(s) affected)");
			}
		} catch (final SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static EmeraldBan expireBan(final EmeraldBan ban) {
		final EmeraldBanHistory banhistory = new EmeraldBanHistory(OlympaConsole.getUniqueId(), EmeraldBanStatus.EXPIRE);
		ban.setStatus(EmeraldBanStatus.EXPIRE);
		ban.addHistory(banhistory);
		try {
			final PreparedStatement pstate = DatabaseManager.getConnection().prepareStatement("UPDATE sanctions SET `status_id` = ?, `history` = CONCAT_WS(';', ?, history) WHERE `id` = ?;");
			pstate.setInt(1, ban.getStatus().getId());
			pstate.setString(2, banhistory.toJson());
			pstate.setInt(3, ban.getId());
			pstate.executeUpdate();
			pstate.close();
		} catch (final SQLException e) {
			e.printStackTrace();
			return ban;
		}
		return ban;

	}

	public static EmeraldBan getActiveSanction(final Object target) {
		return getActiveSanction(target, null);
	}

	public static EmeraldBan getActiveSanction(final Object target, final EmeraldBanType bantype) {
		EmeraldBan ban = null;
		try {
			String exe;
			if (bantype == null) {
				exe = "SELECT * FROM sanctions WHERE `target` = '" + target + "' AND `status_id` = " + EmeraldBanStatus.ACTIVE + ";";
			} else {
				exe = "SELECT * FROM sanctions WHERE `target` = '" + target + "' AND `status_id` = '" + EmeraldBanStatus.ACTIVE.getId() + "' AND `type_id` = '" + bantype.getInteger() + "';";
			}
			final Statement state = DatabaseManager.getConnection().createStatement();
			final ResultSet resultSet = state.executeQuery(exe);
			if (resultSet.next()) {
				ban = new EmeraldBan(
						resultSet.getInt("id"),
						EmeraldBanType.getByID(resultSet.getInt("type_id")),
						resultSet.getString("target"),
						UUID.fromString(resultSet.getString("author_uuid")),
						resultSet.getString("reason"),
						resultSet.getLong("created"),
						resultSet.getLong("expires"),
						EmeraldBanStatus.getStatus(resultSet.getInt("status_id")));
				final String history = resultSet.getString("history");
				if (history != null) {
					for (final String hist : history.split(",")) {
						ban.addHistory(EmeraldBanHistory.fromJson(hist));
					}
				}
				if (ban.getExpires() < Utils.getCurrentTimeinSeconds()) {
					BanMySQL.expireBan(ban);
					return null;
				}
			}
		} catch (final SQLException e) {
			e.printStackTrace();
			return null;
		}

		return ban;
	}

	public static List<EmeraldBan> getAllActiveSantion(final Object target) {
		final List<EmeraldBan> sanctions = new ArrayList<>();
		try {
			final Statement state = DatabaseManager.getConnection().createStatement();
			final ResultSet resultSet = state.executeQuery("SELECT * FROM sanctions WHERE `target` = '" + target + "' AND `status_id` = " + EmeraldBanStatus.ACTIVE + ";");
			while (resultSet.next()) {
				final EmeraldBan ban = new EmeraldBan(
						resultSet.getInt("id"),
						EmeraldBanType.getByID(resultSet.getInt("type_id")),
						UUID.fromString(resultSet.getString("target")),
						UUID.fromString(resultSet.getString("author_uuid")),
						resultSet.getString("reason"),
						resultSet.getLong("created"),
						resultSet.getLong("expires"),
						EmeraldBanStatus.getStatus(resultSet.getInt("status_id")));
				final String history = resultSet.getString("history");
				if (history != null) {
					for (final String hist : history.split(",")) {
						ban.addHistory(EmeraldBanHistory.fromJson(hist));
					}
				}
				if (ban.getExpires() < Utils.getCurrentTimeinSeconds()) {
					BanMySQL.expireBan(ban);
					continue;
				}
				sanctions.add(ban);
			}
		} catch (final SQLException e) {
			e.printStackTrace();
			return null;
		}
		return sanctions;
	}

	public static List<EmeraldBan> getAllSanction(final Object target) {
		return getAllSanction(target, null);
	}

	public static List<EmeraldBan> getAllSanction(final Object target, final EmeraldBanType bantype) {
		final List<EmeraldBan> sanctions = new ArrayList<>();
		try {
			String exe;
			if (bantype == null) {
				exe = "SELECT * FROM sanctions WHERE `target` = '" + target + "';";
			} else {
				exe = "SELECT * FROM sanctions WHERE `target` = '" + target + "' AND `type_id` = '" + bantype.getInteger() + "';";
			}
			final Statement state = DatabaseManager.getConnection().createStatement();
			final ResultSet resultSet = state.executeQuery(exe);
			while (resultSet.next()) {
				final EmeraldBan ban = new EmeraldBan(
						resultSet.getInt("id"),
						EmeraldBanType.getByID(resultSet.getInt("type_id")),
						UUID.fromString(resultSet.getString("target")),
						UUID.fromString(resultSet.getString("author_uuid")),
						resultSet.getString("reason"),
						resultSet.getLong("created"),
						resultSet.getLong("expires"),
						EmeraldBanStatus.getStatus(resultSet.getInt("status_id")));
				if (!ban.getStatus().equals(EmeraldBanStatus.DELETE)) {
					final String history = resultSet.getString("history");
					if (history != null) {
						for (final String hist : history.split(";")) {
							ban.addHistory(EmeraldBanHistory.fromJson(hist));
						}
					}

					if (ban.getExpires() < Utils.getCurrentTimeinSeconds()) {
						BanMySQL.expireBan(ban);
					}
					sanctions.add(ban);
				}

			}
		} catch (final SQLException e) {
			e.printStackTrace();
			return null;
		}
		return Lists.reverse(sanctions);
	}

	/**
	 * Récupère un ban
	 *
	 * @param id
	 *            ID du ban
	 */
	public static EmeraldBan getBanByID(final int id) {
		EmeraldBan ban = null;
		try {
			final Statement state = DatabaseManager.getConnection().createStatement();
			final ResultSet resultSet = state.executeQuery("SELECT * FROM sanctions WHERE `id` = '" + id + "';");
			if (resultSet.next()) {
				ban = new EmeraldBan(
						resultSet.getInt("id"),
						EmeraldBanType.getByID(resultSet.getInt("type_id")),
						UUID.fromString(resultSet.getString("target")),
						UUID.fromString(resultSet.getString("author_uuid")),
						resultSet.getString("reason"),
						resultSet.getLong("created"),
						resultSet.getLong("expires"),
						EmeraldBanStatus.getStatus(resultSet.getInt("status_id")));
				final String history = resultSet.getString("history");
				if (history != null) {
					for (final String hist : history.split(";")) {
						ban.addHistory(EmeraldBanHistory.fromJson(hist));
					}
				}
			}
		} catch (final SQLException e) {
			e.printStackTrace();
			return null;
		}
		return ban;
	}

	public static List<EmeraldBan> getSanctionByAuthor(final UUID authorUUID) {
		final List<EmeraldBan> sanctions = new ArrayList<>();
		try {
			final Statement state = DatabaseManager.getConnection().createStatement();
			final ResultSet resultSet = state.executeQuery("SELECT * FROM sanctions WHERE `author_uuid` = '" + authorUUID + "';");
			while (resultSet.next()) {
				final EmeraldBan ban = new EmeraldBan(
						resultSet.getInt("id"),
						EmeraldBanType.getByID(resultSet.getInt("type_id")),
						UUID.fromString(resultSet.getString("target")),
						UUID.fromString(resultSet.getString("author_uuid")),
						resultSet.getString("reason"),
						resultSet.getLong("created"),
						resultSet.getLong("expires"),
						EmeraldBanStatus.getStatus(resultSet.getInt("status_id")));
				final String history = resultSet.getString("history");
				if (history != null) {
					for (final String hist : history.split(",")) {
						ban.addHistory(EmeraldBanHistory.fromJson(hist));
					}
				}
				if (ban.getStatus() == EmeraldBanStatus.ACTIVE && ban.getExpires() != 0 && ban.getExpires() < Utils.getCurrentTimeinSeconds()) {
					BanMySQL.expireBan(ban);
				}
				sanctions.add(ban);
			}
		} catch (final SQLException e) {
			e.printStackTrace();
			return null;
		}

		return sanctions;
	}

	public static boolean isBanned(final String ip) {
		return isSanctionActive(ip, EmeraldBanType.BANIP);
	}

	public static boolean isBanned(final UUID targetUUID) {
		return isSanctionActive(targetUUID, EmeraldBanType.BAN);
	}

	public static boolean isMuted(final UUID targetUUID) {
		return isSanctionActive(targetUUID, EmeraldBanType.MUTE);
	}

	public static boolean isSanctionActive(final Object target, final EmeraldBanType banType) {
		return getActiveSanction(target, banType) != null;
	}
}
