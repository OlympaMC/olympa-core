package fr.olympa.core.bungee.ban.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.sql.MySQL;
import fr.olympa.api.utils.Matcher;
import fr.olympa.api.utils.Utils;
import fr.olympa.api.utils.spigot.SpigotUtils;
import fr.olympa.core.bungee.utils.BungeeUtils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class OlympaSanction {

	public static int getNextId() {
		return 0;
		// TODO add
		// return MySQL.getNextId("sanctions", "id");
	}

	private int id;
	private OlympaSanctionType type;
	private Object player;
	private String reason;
	private UUID author;
	private long expires;
	private long created;
	private OlympaSanctionStatus status;
	private List<OlympaSanctionHistory> history;

	private boolean permanant;

	/**
	 * @param id      ID du ban
	 * @param player  UUID du joueur qui doit être ban
	 * @param author  UUID de l'auteur du ban
	 * @param reason  Raison du ban
	 * @param created Timestamp de la creation du ban
	 * @param expires Timestamp du temps de ban, 0 = permanant
	 */
	public OlympaSanction(int id, OlympaSanctionType type, Object player, UUID author, String reason, long created, long expires) {
		this.id = id;
		this.type = type;
		this.player = player;
		this.reason = reason;
		this.author = author;
		this.created = created;
		this.expires = expires;
		status = OlympaSanctionStatus.ACTIVE;
		history = new ArrayList<>();
		permanant = this.expires == 0 ? true : false;
	}

	// ###########################################################################################################################################################

	/**
	 * @param id      ID du ban
	 * @param player  UUID du joueur qui doit être ban
	 * @param author  UUID de l'auteur du ban
	 * @param reason  Raison du ban
	 * @param created Timestamp de la creation du ban
	 * @param expires Timestamp du temps de ban, 0 = permanant
	 * @param status  Status du ban
	 */
	public OlympaSanction(int id, OlympaSanctionType type, Object player, UUID author, String reason, long created, long expires, OlympaSanctionStatus status) {
		this.id = id;
		this.type = type;
		this.player = player;
		this.reason = reason;
		this.author = author;
		this.created = created;
		this.expires = expires;
		this.status = status;
		history = new ArrayList<>();
		permanant = this.expires == 0 ? true : false;
	}

	public void addHistory(OlympaSanctionHistory history) {
		this.history.add(history);
	}

	public UUID getAuthor() {
		return author;
	}

	public long getBanTime() {
		return expires - created;
	}

	public long getCreated() {
		return created;
	}

	public long getExpires() {
		return expires;
	}

	public OlympaSanctionHistory getHistory() {
		return history.get(0);
	}

	public List<OlympaSanctionHistory> getHistorys() {
		return history;
	}

	public int getId() {
		return id;
	}

	public Object getPlayer() {
		return player;
	}

	public String getPlayerIp() {
		return String.valueOf(getPlayer());
	}

	public List<OlympaPlayer> getPlayers() {
		return MySQL.getPlayersByIp(getPlayerIp());
	}

	public String getPlayersName() {
		return getPlayers().stream().map(OlympaPlayer::getName).collect(Collectors.joining(", "));
	}

	public UUID getPlayerUniqueId() {
		return UUID.fromString(String.valueOf(getPlayer()));
	}

	public String getReason() {
		return reason;
	}

	public OlympaSanctionStatus getStatus() {
		return status;
	}

	public OlympaSanctionType getType() {
		return type;
	}

	public Boolean isPermanent() {
		return permanant;
	}

	public void removeHistory(OlympaSanctionHistory history) {
		this.history.remove(history);
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setStatus(OlympaSanctionStatus status) {
		this.status = status;
	}

	public void setType(OlympaSanctionType type) {
		this.type = type;
	}

	// ###########################################################################################################################################################

	public BaseComponent[] toBaseComplement() {
		return new ComponentBuilder(BungeeUtils.color("&6Infomation sanction n°&e" + getId() + "\n\n"))

				.append(BungeeUtils.color((Matcher.isUUID(getPlayerIp()) ? "&6Joueur: &e" + SpigotUtils.getName(getPlayerUniqueId()) : "&6IP de: &e" + getPlayersName()) + "\n"))
				.append(BungeeUtils.color("&6Auteur: &e" + SpigotUtils.getName(getAuthor()) + "\n"))
				.append(BungeeUtils.color("&6Type: &e" + getType().getName() + "\n"))
				.append(BungeeUtils.color("&6Raison: &e" + getReason() + "\n"))
				.append(BungeeUtils.color("&6Crée: &e" + Utils.timestampToDateAndHour(getCreated()) + "\n"))
				.append(BungeeUtils.color("&6Expire: &e" + (getExpires() != 0 ? Utils.timestampToDateAndHour(getExpires()) + "\n&6Durée de base: &e" + Utils
						.timestampToDuration(Utils.getCurrentTimeInSeconds() + getBanTime())
						+ (getExpires() >= Utils.getCurrentTimeInSeconds() ? "\n&6Durée restante: &e" + Utils
								.timestampToDuration(getExpires()) : "")
						: "permanant") + "\n"))
				.append(BungeeUtils.color("&6Status: &e" + getStatus().getColor() + getStatus().getName()))
				.create();
	}
}
