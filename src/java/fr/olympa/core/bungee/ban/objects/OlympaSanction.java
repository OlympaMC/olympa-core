package fr.olympa.core.bungee.ban.objects;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import fr.olympa.api.match.RegexMatcher;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.player.OlympaPlayerInformations;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.sql.MySQL;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.utils.BungeeUtils;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class OlympaSanction {

	public String getTarget() {
		return target;
	}

	public static int getNextId() {
		return 0;
		// TODO add
		// return MySQL.getNextId("sanctions", "id");
	}

	private long id;
	private OlympaSanctionType type;
	private OlympaSanctionTargetType targetType;
	private String target;
	private String reason;
	private long author;
	private long expires;
	private long created;
	private OlympaSanctionStatus status;
	private List<OlympaSanctionHistory> history;
	private Set<OlympaPlayerInformations> playersInformations;

	/**
	 * @param target  UUID du joueur qui doit être ban
	 * @param author  UUID de l'auteur du ban
	 * @param reason  Raison du ban
	 * @param created Timestamp de la creation du ban
	 * @param expires Timestamp du temps de ban, 0 = permanant
	 */
	public OlympaSanction(OlympaSanctionType type, String target, long author, String reason, long created, long expires) {
		this.type = type;
		this.target = target;
		this.reason = reason;
		this.author = author;
		this.created = created;
		this.expires = expires;
		status = OlympaSanctionStatus.ACTIVE;
		history = new ArrayList<>();
	}

	// ###########################################################################################################################################################

	/**
	 * @param player  UUID du joueur qui doit être ban
	 * @param author  UUID de l'auteur du ban
	 * @param reason  Raison du ban
	 * @param created Timestamp de la creation du ban
	 * @param expires Timestamp du temps de ban, 0 = permanant
	 * @param status  Status du ban
	 */
	public OlympaSanction(OlympaSanctionType type, String target, long author, String reason, long created, long expires, OlympaSanctionStatus status) {
		this.type = type;
		this.target = target;
		this.reason = reason;
		this.author = author;
		this.created = created;
		this.expires = expires;
		this.status = status;
		history = new ArrayList<>();
	}

	public OlympaSanction(int id, OlympaSanctionType type, String target, long author, String reason, long created, long expires, OlympaSanctionStatus status) {
		this.id = id;
		this.type = type;
		this.target = target;
		this.reason = reason;
		this.author = author;
		this.created = created;
		this.expires = expires;
		this.status = status;
		history = new ArrayList<>();
	}

	public void addHistory(OlympaSanctionHistory history) {
		this.history.add(history);
	}

	public long getAuthor() {
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

	public long getId() {
		return id;
	}

	public String getReason() {
		return reason;
	}

	public OlympaSanctionTargetType getTargetType() {
		if (targetType == null)
			if (RegexMatcher.LONG.is(target))
				targetType = OlympaSanctionTargetType.OLYMPA_ID;
			else if (RegexMatcher.IP.is(target))
				targetType = OlympaSanctionTargetType.IP;
			else
				throw new IllegalArgumentException(target + " is not ID or IP.");
		return targetType;
	}

	public boolean isTarget(OlympaPlayer olympaPlayer) {
		OlympaSanctionTargetType targetType2 = getTargetType();
		if (targetType2 == OlympaSanctionTargetType.OLYMPA_ID)
			return olympaPlayer.getId() == getTargetId();
		else
			return olympaPlayer.getIp().equals(getTargetIp());
	}

	public String getTargetIp() {
		if (getTargetType() != OlympaSanctionTargetType.IP)
			return null;
		return target;
	}

	public Long getTargetId() {
		if (getTargetType() != OlympaSanctionTargetType.OLYMPA_ID)
			return null;
		return Long.parseLong(target);
	}

	//	public void addPlayers(List<OlympaPlayer> olympaPlayers) {
	//		playerInformations = olympaPlayers;
	//	}

	@SuppressWarnings("deprecation")
	public Set<ProxiedPlayer> getOnlinePlayers() {
		Set<ProxiedPlayer> onlinePlayers = new HashSet<>();
		if (getTargetType() == OlympaSanctionTargetType.OLYMPA_ID)
			onlinePlayers.add(ProxyServer.getInstance().getPlayer(AccountProvider.getPlayerInformations(getTargetId()).getUUID()));
		else
			onlinePlayers = ProxyServer.getInstance().getPlayers().stream().filter(p -> p.getAddress().getAddress().getHostName().equals(getTargetIp())).collect(Collectors.toSet());
		return onlinePlayers;
	}

	public Set<OlympaPlayerInformations> getPlayersInfos() {
		if (playersInformations == null)
			if (getTargetType() == OlympaSanctionTargetType.OLYMPA_ID) {
				playersInformations = new HashSet<>();
				playersInformations.add(AccountProvider.getPlayerInformations(getTargetId()));
			} else
				playersInformations = MySQL.getPlayersByIp(target).stream().map(op -> AccountProvider.getPlayerInformations(op)).collect(Collectors.toSet());

		return playersInformations;
	}

	public String getPlayersNames() {
		return getPlayersInfos().stream().map(OlympaPlayerInformations::getName).collect(Collectors.joining(", "));
	}

	public OlympaSanctionStatus getStatus() {
		return status;
	}

	public OlympaSanctionType getType() {
		return type;
	}

	public boolean isPermanent() {
		return expires == 0 && type != OlympaSanctionType.KICK;
	}

	public void removeHistory(OlympaSanctionHistory history) {
		this.history.remove(history);
	}

	public void setId(long id2) {
		id = id2;
	}

	public void setStatus(OlympaSanctionStatus status) {
		this.status = status;
	}

	public void setType(OlympaSanctionType type) {
		this.type = type;
	}

	// ###########################################################################################################################################################

	public BaseComponent[] toBaseComplement() {
		return new ComponentBuilder(BungeeUtils.color("&6Information sanction n°&e" + getId() + "\n\n"))
				.append(BungeeUtils.color("&6Joueur: &e" + getPlayersNames()))
				.append(BungeeUtils.color("&6Auteur: &e" + BungeeUtils.getName(getAuthor()) + "\n"))
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
