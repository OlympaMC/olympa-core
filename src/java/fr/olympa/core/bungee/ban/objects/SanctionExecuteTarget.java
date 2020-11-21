package fr.olympa.core.bungee.ban.objects;

import java.net.InetAddress;
import java.sql.SQLException;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.ban.BanMySQL;
import fr.olympa.core.bungee.ban.SanctionUtils;
import fr.olympa.core.bungee.utils.BungeeUtils;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;

public class SanctionExecuteTarget {

	// UUID, NAME, ID or IP
	Object identifier;
	// ID or IP
	Object banIdentifier;
	List<OlympaPlayer> olympaPlayers;
	List<ProxiedPlayer> players;
	OlympaSanction sanction;

	public Object getBanIdentifier() {
		return banIdentifier;
	}

	public List<OlympaPlayer> getOlympaPlayers() {
		return olympaPlayers;
	}

	public List<ProxiedPlayer> getPlayers() {
		return players;
	}

	public OlympaSanction getSanction() {
		return sanction;
	}

	public SanctionExecuteTarget(Object identifier, List<OlympaPlayer> olympaPlayers) {
		this.identifier = identifier;
		this.olympaPlayers = olympaPlayers;
	}

	public boolean save(SanctionExecute banExecute) throws SQLException {
		if (identifier instanceof InetAddress) {
			if (banExecute.sanctionType == OlympaSanctionType.BANIP)
				banExecute.sanctionType = OlympaSanctionType.BANIP;
			else if (banExecute.sanctionType != OlympaSanctionType.BANIP) {
				new Exception("IP can be only banned.").printStackTrace();
				return false;
			}
			banIdentifier = ((InetAddress) identifier).getHostAddress();
		} else if (olympaPlayers.size() == 1)
			banIdentifier = olympaPlayers.get(0).getId();
		else {
			new Exception("More than 1 OlympaPlayer for UUID, String or ID.").printStackTrace();
			return false;
		}

		Configuration config = OlympaBungee.getInstance().getConfig();
		OlympaSanction alreadyban = BanMySQL.getSanctionActive(banIdentifier, banExecute.sanctionType);
		if (alreadyban != null) {
			TextComponent msg = new TextComponent(String.format(config.getString("ban.alreadysanctionned"), banIdentifier, banExecute.sanctionType.getName()));
			msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, alreadyban.toBaseComplement()));
			msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/banhist " + alreadyban.getId()));
			banExecute.getAuthorSender().sendMessage(msg);
			return false;
		}
		players = olympaPlayers.stream().map(op -> ProxyServer.getInstance().getPlayer(op.getUniqueId())).filter(p -> p != null && p.isConnected()).collect(Collectors.toList());
		banExecute.reason = SanctionUtils.formatReason(banExecute.reason);
		sanction = add(banExecute.sanctionType, banExecute.getAuthorId(), banIdentifier, banExecute.reason, banExecute.expire, OlympaSanctionStatus.ACTIVE);
		sanction.addPlayers(olympaPlayers);
		return true;
	}

	private static BaseComponent[] getDisconnectScreen(OlympaSanction sanction) {
		StringJoiner sjDisconnect = new StringJoiner("\n");
		String typeAction = sanction.getType().getNameForPlayer();
		boolean permanant = false;
		if (sanction.isPermanent()) {
			permanant = sanction.getType().equals(OlympaSanctionType.BAN) && sanction.isPermanent();
			typeAction += " &npermanent&c";
		}
		sjDisconnect.add(String.format("&cTu a été %s", typeAction));
		sjDisconnect.add("");
		sjDisconnect.add(String.format("&cRaison : &4%s", sanction.getReason()));
		sjDisconnect.add("");
		if (permanant) {
			sjDisconnect.add(String.format("&cDurée restante : &4%s&c", Utils.timestampToDuration(sanction.getExpires())));
			sjDisconnect.add("");
		}
		sjDisconnect.add(String.format("&cID : &4%s&c", String.valueOf(sanction.getId())));
		sjDisconnect.add("");
		BaseComponent[] msgDisconnect = TextComponent.fromLegacyText(BungeeUtils.connectScreen(sjDisconnect.toString()));
		return msgDisconnect;
	}

	public void execute(SanctionExecute banExecute) {
		if (banIdentifier == null) {
			new Exception("Do SanctionExcecuteTarget#save first.").printStackTrace();
			return;
		}
		OlympaSanctionType type = banExecute.sanctionType;
		if (type == OlympaSanctionType.BAN || type == OlympaSanctionType.BANIP || type == OlympaSanctionType.KICK)
			for (ProxiedPlayer t : getPlayers())
				t.disconnect(getDisconnectScreen(sanction));
		else if (type == OlympaSanctionType.MUTE)
			new Exception("TODO: Mute").printStackTrace();
		// TODO
	}

	public static OlympaSanction add(OlympaSanctionType type, long author, Object target, String reason, long timestamp) throws SQLException {
		return add(type, author, target, reason, timestamp, OlympaSanctionStatus.ACTIVE);
	}

	public static OlympaSanction add(OlympaSanctionType type, long author, Object target, String reason, long timestamp, OlympaSanctionStatus status) throws SQLException {
		long actuelTime = Utils.getCurrentTimeInSeconds();
		OlympaSanction sanction = new OlympaSanction(type, target, author, reason, actuelTime, timestamp, status);
		long id = BanMySQL.addSanction(sanction);
		sanction.setId(id);
		return sanction;
	}
}
