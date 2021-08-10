package fr.olympa.core.bungee.ban.execute;

import java.net.InetAddress;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import fr.olympa.api.bungee.utils.BungeeUtils;
import fr.olympa.api.common.chat.ColorUtils;
import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.ban.BanMySQL;
import fr.olympa.core.bungee.ban.SanctionHandler;
import fr.olympa.core.bungee.ban.SanctionUtils;
import fr.olympa.core.bungee.ban.objects.OlympaSanction;
import fr.olympa.core.bungee.ban.objects.OlympaSanctionHistory;
import fr.olympa.core.bungee.ban.objects.OlympaSanctionStatus;
import fr.olympa.core.bungee.ban.objects.OlympaSanctionType;
import fr.olympa.core.common.permission.list.OlympaCorePermissionsBungee;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;

public class SanctionExecuteTarget {

	// UUID, NAME, ID or IP
	Object identifier;
	// ID or IP
	String banIdentifier;
	List<OlympaPlayer> olympaPlayers;
	List<OlympaSanction> sanctions;
	List<ProxiedPlayer> players;
	//	OlympaSanction sanction;

	public SanctionExecuteTarget(Object identifier, List<OlympaPlayer> olympaPlayers) {
		this.identifier = identifier;
		this.olympaPlayers = olympaPlayers;
	}

	public SanctionExecuteTarget(List<OlympaSanction> sanctions, Object identifier) {
		this.identifier = identifier;
		this.sanctions = sanctions;
	}

	public String getBanIdentifier() {
		return banIdentifier;
	}

	public List<OlympaPlayer> getOlympaPlayers() {
		return olympaPlayers;
	}

	public List<ProxiedPlayer> getPlayers() {
		return players;
	}

	public OlympaSanction getFirstSanction() {
		return sanctions.get(0);
	}

	public List<OlympaSanction> getSanctions() {
		return sanctions;
	}

	@SuppressWarnings("deprecation")
	public boolean save(SanctionExecute banExecute) throws SQLException {
		OlympaSanctionStatus newStatus = banExecute.newStatus;
		boolean isCasualKick = banExecute.sanctionType == OlympaSanctionType.KICK && newStatus == OlympaSanctionStatus.END;
		if (identifier instanceof InetAddress) {
			if (banExecute.sanctionType == OlympaSanctionType.BAN)
				banExecute.sanctionType = OlympaSanctionType.BANIP;
			else if (banExecute.sanctionType != OlympaSanctionType.BANIP) {
				new Exception("IP can be only banned.").printStackTrace();
				return false;
			}
			banIdentifier = ((InetAddress) identifier).getHostAddress();
		} else if (olympaPlayers.size() == 1)
			banIdentifier = String.valueOf(olympaPlayers.get(0).getId());
		else {
			new Exception("More than 1 OlympaPlayer for UUID, String or ID.").printStackTrace();
			return false;
		}
		players = olympaPlayers.stream().map(op -> ProxyServer.getInstance().getPlayer(op.getUniqueId())).filter(p -> p != null && p.isConnected()).collect(Collectors.toList());

		Configuration config = OlympaBungee.getInstance().getConfig();
		OlympaSanction alreadyban = null;
		if (!isCasualKick)
			alreadyban = BanMySQL.getSanctionActive(banIdentifier, banExecute.sanctionType);
		OlympaSanction sanction;
		if (newStatus == OlympaSanctionStatus.ACTIVE || isCasualKick) {
			if (alreadyban != null) {
				TextComponent msg = new TextComponent(Prefix.DEFAULT_BAD.formatMessageB(config.getString("ban.alreadysanctionned"), identifier, banExecute.sanctionType.getName(!alreadyban.isPermanent()), alreadyban.getReason()));
				msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, alreadyban.toBaseComplement()));
				msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/banhist " + alreadyban.getId()));
				banExecute.getAuthorSender().sendMessage(msg);
				return false;
			}
			if (banExecute.expire != 0 && !OlympaCorePermissionsBungee.BAN_BYPASSTIME.hasPermission(banExecute.getAuthor())) {
				long mins = (banExecute.expire - Utils.getCurrentTimeInSeconds()) / 60;
				if (banExecute.sanctionType.isBanType() && mins < SanctionHandler.minTimeBan || banExecute.sanctionType.isMuteType() && mins < SanctionHandler.minTimeMute) {
					banExecute.getAuthorSender().sendMessage(Prefix.DEFAULT_BAD.formatMessageB(config.getString("ban.cantbypasstime"), "minimal",
							banExecute.sanctionType.getName(!banExecute.isPermanant()), Utils.timeToDuration((banExecute.sanctionType.isMuteType() ? SanctionHandler.minTimeMute : SanctionHandler.minTimeBan) * 60)));
					return false;
				}
				if (banExecute.sanctionType.isBanType() && mins > SanctionHandler.maxTimeBan || banExecute.sanctionType.isMuteType() && mins > SanctionHandler.maxTimeMute) {
					banExecute.getAuthorSender().sendMessage(Prefix.DEFAULT_BAD.formatMessageB(config.getString("ban.cantbypasstime"), "maximal",
							banExecute.sanctionType.getName(!banExecute.isPermanant()), Utils.timeToDuration((banExecute.sanctionType.isMuteType() ? SanctionHandler.maxTimeMute : SanctionHandler.maxTimeBan) * 60)));
					return false;
				}
				if (banExecute.getAuthorId() != 0 && banExecute.sanctionType != OlympaSanctionType.BANIP && OlympaCorePermissionsBungee.STAFF.hasPermission(olympaPlayers.get(0))
						&& !OlympaCorePermissionsBungee.BAN_BYPASS_SANCTION_STAFF.hasPermission(banExecute.getAuthor())) {
					banExecute.getAuthorSender().sendMessage(Prefix.DEFAULT_BAD.formatMessageB(config.getString("ban.cantmutestaffmembers")));
					return false;
				}
			}
			sanction = add(banExecute.sanctionType, banExecute.getAuthorId(), banIdentifier, banExecute.reason, banExecute.expire, newStatus);

			//		} else if (newStatus == OlympaSanctionStatus.END && banExecute.sanctionType == OlympaSanctionType.MUTE) {
			//			sanction = add(banExecute.sanctionType, banExecute.getAuthorId(), banIdentifier, banExecute.reason, banExecute.expire, newStatus);
		} else {
			if (alreadyban != null)
				sanction = alreadyban;
			else {
				banExecute.getAuthorSender()
						.sendMessage(Prefix.DEFAULT_BAD.formatMessageB(config.getString("ban.bannotfound"), identifier, banExecute.sanctionType.getName(!banExecute.isPermanant()), OlympaSanctionStatus.ACTIVE.getNameColored()));
				return false;
			}
			sanction.setStatus(newStatus);
			if (!BanMySQL.changeStatus(new OlympaSanctionHistory(banExecute.getAuthorId(), newStatus, banExecute.reason), sanction.getId())) {
				banExecute.getAuthorSender().sendMessage(ColorUtils.color(config.getString("ban.errordb")));
				return false;
			}
		}
		sanctions = Arrays.asList(sanction);
		return true;
	}

	public void execute(SanctionExecute banExecute) {
		OlympaSanction sanction = getFirstSanction();
		if (banIdentifier == null) {
			new Exception("Do SanctionExcecuteTarget#save first.").printStackTrace();
			return;
		}
		OlympaSanctionType type = banExecute.sanctionType;
		boolean isKick = type == OlympaSanctionType.KICK;
		if (sanction.getStatus() == OlympaSanctionStatus.ACTIVE || isKick) {
			if (type == OlympaSanctionType.BAN || type == OlympaSanctionType.BANIP || isKick)
				for (ProxiedPlayer t : getPlayers())
					t.disconnect(SanctionUtils.getDisconnectScreen(sanction));
			else if (type == OlympaSanctionType.MUTE)
				SanctionHandler.addMute(sanction);
		} else if (sanction.getStatus() == OlympaSanctionStatus.CANCEL)
			if (type == OlympaSanctionType.MUTE) {
				SanctionHandler.removeMute(sanction);
				sanction.getOnlinePlayers().forEach(p -> p.sendMessage(Prefix.DEFAULT_GOOD.formatMessageB("Tu as été unmute.")));
			}
	}

	@SuppressWarnings("deprecation")
	public void annonce(SanctionExecute banExecute) {
		OlympaSanctionStatus newStatus = banExecute.newStatus;
		OlympaSanction sanction = getFirstSanction();
		String reason = banExecute.reason;
		OlympaSanctionType type = sanction.getType();
		String actionName = type.getName().toLowerCase();
		if (newStatus != OlympaSanctionStatus.END && type != OlympaSanctionType.KICK)
			actionName = newStatus.getPrefix() + actionName;
		String timeToExpire = Utils.timestampToDuration(sanction.getExpires(), 2, sanction.getCreated());

		List<ProxiedPlayer> onlineTargets = getPlayers();
		List<String> playersNames = new ArrayList<>();

		playersNames.addAll(getOlympaPlayers().stream().map(OlympaPlayer::getName).collect(Collectors.toList()));
		StringJoiner sjAnnonce = new StringJoiner(" ");
		sjAnnonce.add(String.format("&2[&c%s&2]", actionName.toUpperCase()));
		if (playersNames.size() > 1)
			sjAnnonce.add(String.format("%s ont été", ColorUtils.joinGoldEt(playersNames)));
		else
			sjAnnonce.add(String.format("&4%s&c a été", playersNames.get(0)));
		sjAnnonce.add(actionName);
		if (newStatus == OlympaSanctionStatus.ACTIVE && !sanction.isPermanent())
			sjAnnonce.add(String.format("pendant &4%s&c", timeToExpire));
		sjAnnonce.add(String.format("pour &4%s&c.", reason));
		TextComponent msg = new TextComponent(TextComponent.fromLegacyText(ColorUtils.color(sjAnnonce.toString())));
		TextComponent msgStaff = msg.duplicate();
		msgStaff.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, sanction.toBaseComplement()));
		msgStaff.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/hist " + sanction.getId()));
		ProxyServer.getInstance().getConsole().sendMessage(msgStaff);
		switch (newStatus) {
		case ACTIVE:
			BungeeUtils.getPlayers(OlympaCorePermissionsBungee.BAN_SEEBANMSG, t -> t.forEach(p -> p.sendMessage(msgStaff)),
					t -> t.stream().filter(p -> onlineTargets.stream().anyMatch(p2 -> p2.getServer().getInfo().getName().equals(p.getServer().getInfo().getName()))).forEach(p -> p.sendMessage(msg)));
			if (type == OlympaSanctionType.MUTE)
				sanction.getOnlinePlayers().forEach(p -> p.sendMessage(Prefix.DEFAULT_BAD.formatMessageB("Tu as été mute pour &4%s&c pendant &4%s&c.", reason, timeToExpire)));
			break;
		case CANCEL:
		case DELETE:
		case EXPIRE:
		case END:
			BungeeUtils.getPlayers(OlympaCorePermissionsBungee.BAN_SEEBANMSG, t -> t.forEach(p -> p.sendMessage(msgStaff)), null);
			break;
		}
	}

	public static OlympaSanction add(OlympaSanctionType type, long author, String target, String reason, long timestamp) throws SQLException {
		return add(type, author, target, reason, timestamp, OlympaSanctionStatus.ACTIVE);
	}

	public static OlympaSanction add(OlympaSanctionType type, long author, String target, String reason, long timestamp, OlympaSanctionStatus status) throws SQLException {
		long actuelTime = Utils.getCurrentTimeInSeconds();
		OlympaSanction sanction = new OlympaSanction(type, target, author, reason, actuelTime, timestamp, status);
		long id = BanMySQL.addSanction(sanction);
		sanction.setId(id);
		return sanction;
	}
}
