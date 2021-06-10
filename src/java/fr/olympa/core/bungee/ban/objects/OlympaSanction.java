package fr.olympa.core.bungee.ban.objects;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.annotations.Expose;

import fr.olympa.api.common.chat.ColorUtils;
import fr.olympa.api.common.match.RegexMatcher;
import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.common.player.OlympaPlayerInformations;
import fr.olympa.api.common.provider.AccountProvider;
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

	@Expose
	private long id;
	@Expose
	private OlympaSanctionType type;
	@Expose
	private OlympaSanctionTargetType targetType;
	@Expose
	private String target;
	@Expose
	private String reason;
	@Expose
	private long author;
	@Expose
	private long expires;
	@Expose
	private long created;
	@Expose
	private OlympaSanctionStatus status;
	@Expose
	private List<OlympaSanctionHistory> history;
	@Expose
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

	public String getAuthorName() {
		return BungeeUtils.getName(getAuthor());
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
		if (getTargetType() == OlympaSanctionTargetType.OLYMPA_ID) {
			ProxiedPlayer player = ProxyServer.getInstance().getPlayer(AccountProvider.getter().getPlayerInformations(getTargetId()).getUUID());
			if (player != null)
				onlinePlayers.add(player);
		} else
			onlinePlayers = ProxyServer.getInstance().getPlayers().stream().filter(p -> p.getAddress().getAddress().getHostName().equals(getTargetIp())).collect(Collectors.toSet());
		return onlinePlayers;
	}

	public Set<OlympaPlayerInformations> getPlayersInfos() throws SQLException {
		if (playersInformations == null)
			if (getTargetType() == OlympaSanctionTargetType.OLYMPA_ID) {
				playersInformations = new HashSet<>();
				playersInformations.add(AccountProvider.getter().getPlayerInformations(getTargetId()));
			} else
				playersInformations = AccountProvider.getter().getSQL().getPlayersByIp(target).stream().map(op -> AccountProvider.getter().getPlayerInformations(op)).collect(Collectors.toSet());

		return playersInformations;
	}

	public String getPlayersNames() throws SQLException {
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
		String playerNames = "null";
		try {
			playerNames = getPlayersNames();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return new ComponentBuilder(ColorUtils.color("&6Information sanction n°&e" + getId() + "\n"))
				.append(ColorUtils.color("&6Joueur: &e" + playerNames + "\n"))
				.append(ColorUtils.color("&6Auteur: &e" + getAuthorName() + "\n"))
				.append(ColorUtils.color("&6Type: &e" + getType().getName(!isPermanent()) + "\n"))
				.append(ColorUtils.color("&6Raison: &e" + getReason() + "\n"))
				.append(ColorUtils.color("&6Crée: &e" + Utils.timestampToDateAndHour(getCreated()) + "\n"))
				.append(ColorUtils.color("&6Expire: &e" + (getExpires() != 0 ? Utils.timestampToDateAndHour(getExpires()) + "\n&6Durée de base: &e" + Utils
						.timestampToDuration(Utils.getCurrentTimeInSeconds() + getBanTime())
						+ (getExpires() >= Utils.getCurrentTimeInSeconds() ? "\n&6Durée restante: &e" + Utils
								.timestampToDuration(getExpires()) : "")
						: "permanant") + "\n"))
				.append(ColorUtils.color("&6Statut: &e" + getStatus().getColor() + getStatus().getName()))
				.create();
	}

}
