package fr.tristiisch.olympa.core.ban.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import fr.tristiisch.emeraldmc.api.bungee.utils.BungeeUtils;
import fr.tristiisch.emeraldmc.api.commons.Matcher;
import fr.tristiisch.emeraldmc.api.commons.Utils;
import fr.tristiisch.emeraldmc.api.commons.datamanagment.sql.MySQL;
import fr.tristiisch.emeraldmc.api.commons.object.EmeraldPlayer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class EmeraldBan {

	public static int getNextID() {
		return MySQL.getnextID("bans", "id");
	}

	private int id;
	private EmeraldBanType type;
	private final Object player;
	private final String reason;
	private final UUID author;
	private final long expires;
	private final long created;
	private EmeraldBanStatus status;
	private final List<EmeraldBanHistory> history;

	private final boolean permanant;

	/**
	 * @param id ID du ban
	 * @param player UUID du joueur qui doit être ban
	 * @param author UUID de l'auteur du ban
	 * @param reason Raison du ban
	 * @param created Timestamp de la creation du ban
	 * @param expires Timestamp du temps de ban, 0 = permanant
	 */
	public EmeraldBan(final int id, final EmeraldBanType type, final Object player, final UUID author, final String reason, final long created, final long expires) {
		this.id = id;
		this.type = type;
		this.player = player;
		this.reason = reason;
		this.author = author;
		this.created = created;
		this.expires = expires;
		this.status = EmeraldBanStatus.ACTIVE;
		this.history = new ArrayList<>();
		this.permanant = this.expires == 0 ? true : false;
	}

	//###########################################################################################################################################################

	/**
	 * @param id ID du ban
	 * @param player UUID du joueur qui doit être ban
	 * @param author UUID de l'auteur du ban
	 * @param reason Raison du ban
	 * @param created Timestamp de la creation du ban
	 * @param expires Timestamp du temps de ban, 0 = permanant
	 * @param status Status du ban
	 */
	public EmeraldBan(final int id, final EmeraldBanType type, final Object player, final UUID author, final String reason, final long created, final long expires, final EmeraldBanStatus status) {
		this.id = id;
		this.type = type;
		this.player = player;
		this.reason = reason;
		this.author = author;
		this.created = created;
		this.expires = expires;
		this.status = status;
		this.history = new ArrayList<>();
		this.permanant = this.expires == 0 ? true : false;
	}

	public void addHistory(final EmeraldBanHistory history) {
		this.history.add(history);
	}

	public UUID getAuthor() {
		return this.author;
	}

	public long getBanTime() {
		return this.expires - this.created;
	}

	public long getCreated() {
		return this.created;
	}

	public long getExpires() {
		return this.expires;
	}

	public EmeraldBanHistory getHistory() {
		return this.history.get(0);
	}

	public List<EmeraldBanHistory> getHistorys() {
		return this.history;
	}

	public int getId() {
		return this.id;
	}

	public Object getPlayer() {
		return this.player;
	}

	public String getPlayerIp() {
		return String.valueOf(this.getPlayer());
	}

	public List<EmeraldPlayer> getPlayers() {
		return MySQL.getPlayersByIp(this.getPlayerIp());
	}

	public String getPlayersName() {
		return this.getPlayers().stream().map(EmeraldPlayer::getName).collect(Collectors.joining(", "));
	}

	public UUID getPlayerUniqueId() {
		return UUID.fromString(String.valueOf(this.getPlayer()));
	}

	public String getReason() {
		return this.reason;
	}

	public EmeraldBanStatus getStatus() {
		return this.status;
	}

	public EmeraldBanType getType() {
		return this.type;
	}

	public Boolean isPermanent() {
		return this.permanant;
	}

	public void removeHistory(final EmeraldBanHistory history) {
		this.history.remove(history);
	}

	public void setId(final int id) {
		this.id = id;
	}

	public void setStatus(final EmeraldBanStatus status) {
		this.status = status;
	}

	public void setType(final EmeraldBanType type) {
		this.type = type;
	}

	//###########################################################################################################################################################

	public BaseComponent[] toBaseComplement() {
		return new ComponentBuilder(Utils.color("&6Infomation sanction n°&e" + this.getId() + "\n\n"))

				.append(Utils.color((Matcher.isUUID(this.getPlayerIp()) ? "&6Joueur: &e" + BungeeUtils.getName(this.getPlayerUniqueId()) : "&6IP de: &e" + this.getPlayersName()) + "\n"))
				.append(Utils.color("&6Auteur: &e" + BungeeUtils.getName(this.getAuthor()) + "\n"))
				.append(Utils.color("&6Type: &e" + this.getType().getName() + "\n"))
				.append(Utils.color("&6Raison: &e" + this.getReason() + "\n"))
				.append(Utils.color("&6Crée: &e" + Utils.timestampToDateAndHour(this.getCreated()) + "\n"))
				.append(Utils.color("&6Expire: &e" + (this.getExpires() != 0 ? Utils.timestampToDateAndHour(this.getExpires()) + "\n&6Durée de base: &e" + Utils
						.timestampToDuration(Utils.getCurrentTimeinSeconds() + this.getBanTime()) + (this.getExpires() >= Utils.getCurrentTimeinSeconds() ? "\n&6Durée restante: &e" + Utils
								.timestampToDuration(this.getExpires()) : "") : "permanant") + "\n"))
				.append(Utils.color("&6Status: &e" + this.getStatus().getColor() + this.getStatus().getName()))
				.create();
	}
}
