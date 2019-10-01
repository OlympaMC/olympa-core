package fr.tristiisch.olympa.core.ban.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import fr.tristiisch.olympa.api.objects.OlympaPlayer;
import fr.tristiisch.olympa.api.utils.Matcher;
import fr.tristiisch.olympa.api.utils.SpigotUtils;
import fr.tristiisch.olympa.api.utils.Utils;
import fr.tristiisch.olympa.core.datamanagment.sql.MySQL;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class OlympaSanction {

	public static int getNextId() {
		return MySQL.getnextID("sanctions", "id");
	}

	private int id;
	private OlympaSanctionType type;
	private final Object player;
	private final String reason;
	private final UUID author;
	private final long expires;
	private final long created;
	private OlympaSanctionStatus status;
	private final List<OlympaSanctionHistory> history;

	private final boolean permanant;

	/**
	 * @param id ID du ban
	 * @param player UUID du joueur qui doit être ban
	 * @param author UUID de l'auteur du ban
	 * @param reason Raison du ban
	 * @param created Timestamp de la creation du ban
	 * @param expires Timestamp du temps de ban, 0 = permanant
	 */
	public OlympaSanction(final int id, final OlympaSanctionType type, final Object player, final UUID author, final String reason, final long created, final long expires) {
		this.id = id;
		this.type = type;
		this.player = player;
		this.reason = reason;
		this.author = author;
		this.created = created;
		this.expires = expires;
		this.status = OlympaSanctionStatus.ACTIVE;
		this.history = new ArrayList<>();
		this.permanant = this.expires == 0 ? true : false;
	}

	// ###########################################################################################################################################################

	/**
	 * @param id ID du ban
	 * @param player UUID du joueur qui doit être ban
	 * @param author UUID de l'auteur du ban
	 * @param reason Raison du ban
	 * @param created Timestamp de la creation du ban
	 * @param expires Timestamp du temps de ban, 0 = permanant
	 * @param status Status du ban
	 */
	public OlympaSanction(final int id, final OlympaSanctionType type, final Object player, final UUID author, final String reason, final long created, final long expires, final OlympaSanctionStatus status) {
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

	public void addHistory(final OlympaSanctionHistory history) {
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

	public OlympaSanctionHistory getHistory() {
		return this.history.get(0);
	}

	public List<OlympaSanctionHistory> getHistorys() {
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

	public List<OlympaPlayer> getPlayers() {
		return MySQL.getPlayersByIp(this.getPlayerIp());
	}

	public String getPlayersName() {
		return this.getPlayers().stream().map(OlympaPlayer::getName).collect(Collectors.joining(", "));
	}

	public UUID getPlayerUniqueId() {
		return UUID.fromString(String.valueOf(this.getPlayer()));
	}

	public String getReason() {
		return this.reason;
	}

	public OlympaSanctionStatus getStatus() {
		return this.status;
	}

	public OlympaSanctionType getType() {
		return this.type;
	}

	public Boolean isPermanent() {
		return this.permanant;
	}

	public void removeHistory(final OlympaSanctionHistory history) {
		this.history.remove(history);
	}

	public void setId(final int id) {
		this.id = id;
	}

	public void setStatus(final OlympaSanctionStatus status) {
		this.status = status;
	}

	public void setType(final OlympaSanctionType type) {
		this.type = type;
	}

	// ###########################################################################################################################################################

	public BaseComponent[] toBaseComplement() {
		return new ComponentBuilder(SpigotUtils.color("&6Infomation sanction n°&e" + this.getId() + "\n\n"))

				.append(SpigotUtils.color((Matcher.isUUID(this.getPlayerIp()) ? "&6Joueur: &e" + SpigotUtils.getName(this.getPlayerUniqueId()) : "&6IP de: &e" + this.getPlayersName()) + "\n"))
				.append(SpigotUtils.color("&6Auteur: &e" + SpigotUtils.getName(this.getAuthor()) + "\n"))
				.append(SpigotUtils.color("&6Type: &e" + this.getType().getName() + "\n"))
				.append(SpigotUtils.color("&6Raison: &e" + this.getReason() + "\n"))
				.append(SpigotUtils.color("&6Crée: &e" + Utils.timestampToDateAndHour(this.getCreated()) + "\n"))
				.append(SpigotUtils.color("&6Expire: &e" + (this.getExpires() != 0 ? Utils.timestampToDateAndHour(this.getExpires()) + "\n&6Durée de base: &e" + Utils
						.timestampToDuration(Utils.getCurrentTimeinSeconds() + this.getBanTime())
						+ (this.getExpires() >= Utils.getCurrentTimeinSeconds() ? "\n&6Durée restante: &e" + Utils
								.timestampToDuration(this.getExpires()) : "")
						: "permanant") + "\n"))
				.append(SpigotUtils.color("&6Status: &e" + this.getStatus().getColor() + this.getStatus().getName()))
				.create();
	}
}
