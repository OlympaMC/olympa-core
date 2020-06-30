package fr.olympa.core.bungee.ban;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.stream.Collectors;

import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.sql.MySQL;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.ban.objects.OlympaSanction;
import fr.olympa.core.bungee.ban.objects.OlympaSanctionStatus;
import fr.olympa.core.bungee.ban.objects.OlympaSanctionType;
import fr.olympa.core.bungee.utils.BungeeUtils;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class SanctionManager {
	
	public static int maxTimeBan = 527040;
	public static int minTimeBan = 600;

	public static int maxTimeMute;
	public static int minTimeMute;

	public static OlympaSanction add(OlympaSanctionType type, UUID author, Object target, String reason, long timestamp) throws SQLException {
		return add(type, author, target, reason, timestamp, OlympaSanctionStatus.ACTIVE);
	}
	
	public static OlympaSanction add(OlympaSanctionType type, UUID author, Object target, String reason, long timestamp, OlympaSanctionStatus status) throws SQLException {
		long actuelTime = Utils.getCurrentTimeInSeconds();
		OlympaSanction sanction = new OlympaSanction(type, target, author, reason, actuelTime, timestamp, status);
		long id = BanMySQL.addSanction(sanction);
		sanction.setId(id);
		return sanction;
	}
	
	public static boolean addAndApply(OlympaSanctionType type, UUID author, Object target, String reason, long timestamp) throws SQLException {
		return addAndApply(type, author, target, reason, timestamp, OlympaSanctionStatus.ACTIVE);
	}

	@SuppressWarnings("deprecation")
	public static boolean addAndApply(OlympaSanctionType type, UUID author, Object target, String reason, long timestamp, OlympaSanctionStatus status) throws SQLException {
		reason = SanctionUtils.formatReason(reason);
		OlympaSanction sanction = add(type, author, target, reason, timestamp);
		
		List<ProxiedPlayer> onlineTargets = new ArrayList<>();
		List<String> playersNames = new ArrayList<>();
		
		switch (type) {
		case MUTE:
		case KICK:
		case BAN:
			ProxiedPlayer t = null;
			if (target instanceof OlympaPlayer)
				t = ProxyServer.getInstance().getPlayer(((OlympaPlayer) target).getUniqueId());
			else if (target instanceof ProxiedPlayer) {
				onlineTargets.add((ProxiedPlayer) target);
				playersNames.add(((ProxiedPlayer) target).getName());
				break;
			} else if (target instanceof UUID)
				t = ProxyServer.getInstance().getPlayer(UUID.fromString((String) target));
			//			else if (target instanceof String)
			//				t = ProxyServer.getInstance().getPlayer((String) target);
			
			//			else if (target instanceof InetAddress) {
			//				onlineTargets = ProxyServer.getInstance().getPlayers().stream().filter(p -> p.getAddress().getAddress().getHostAddress().equals(((InetAddress)target).getHostAddress())).collect(Collectors.toList());
			//				playersNames = MySQL.getPlayersByIp((String) target).stream().map(OlympaPlayer::getName).collect(Collectors.toList());
			//			}
			else if (target instanceof String)
				t = ProxyServer.getInstance().getPlayer((String) target);
			
			if (t != null) {
				onlineTargets.add(t);
				playersNames.add(t.getName());
			} else {
				OlympaPlayer op = null;
				if (target instanceof UUID)
					op = new AccountProvider(UUID.fromString((String) target)).get();
				else if (target instanceof String)
					op = MySQL.getPlayer((String) target);
				if (op != null)
					playersNames.add(op.getName());
				else
					return false;
			}
			break;
		case BANIP:
			onlineTargets = ProxyServer.getInstance().getPlayers().stream().filter(t2 -> t2.getAddress().getAddress().getHostAddress().equals(target)).collect(Collectors.toList());
			playersNames = MySQL.getPlayersByIp((String) target).stream().map(OlympaPlayer::getName).collect(Collectors.toList());
			break;
		default:
			return false;
		}
		String duration = null;
		if (!sanction.isPermanent())
			duration = Utils.timestampToDuration(sanction.getExpires());
		
		if (type == OlympaSanctionType.BAN || type == OlympaSanctionType.BANIP || type == OlympaSanctionType.KICK)
			onlineTargets.forEach(t -> t.disconnect(getDisconnectScreen(sanction)));
		
		StringJoiner sjAnnonce = new StringJoiner(" ");
		sjAnnonce.add("&2[&c" + type.getName().toUpperCase() + "&2]");
		if (playersNames.size() > 1)
			sjAnnonce.add("&4%s&c ont été".replaceFirst("%s", String.join(", ", playersNames)));
		else
			sjAnnonce.add("&4" + playersNames.get(0) + "&c a été");
		sjAnnonce.add(type.getName().toLowerCase());
		if (!sanction.isPermanent())
			sjAnnonce.add("pendant &4%s&c".replaceFirst("%s", duration));
		sjAnnonce.add("pour &4" + reason + "&c.");
		
		TextComponent msg = new TextComponent(TextComponent.fromLegacyText(sjAnnonce.toString()));
		TextComponent msgStaff = msg.duplicate();
		msgStaff.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, sanction.toBaseComplement()));
		msgStaff.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/hist " + sanction.getId()));
		BungeeUtils.getPlayers(OlympaCorePermissions.BAN_SEEBANMSG, t -> t.forEach(p -> p.sendMessage(msgStaff)), t -> t.forEach(p -> p.sendMessage(msg)));
		ProxyServer.getInstance().getConsole().sendMessage(msgStaff);
		return true;
	}

	private static BaseComponent[] getDisconnectScreen(OlympaSanction sanction) {
		StringJoiner sjDisconnect = new StringJoiner("\n");
		String typeAction = sanction.getType().getName();
		if (sanction.isPermanent())
			typeAction += " &npermanent&c";
		sjDisconnect.add("&cTu a été %s1c".replaceFirst("%s1", typeAction));
		sjDisconnect.add("");
		sjDisconnect.add("&cRaison : &4%s2".replaceFirst("%s", sanction.getReason()));
		sjDisconnect.add("");
		if (!sanction.isPermanent()) {
			sjDisconnect.add("&cDurée restante : &4%s&c".replaceFirst("%s", Utils.timestampToDuration(sanction.getExpires())));
			sjDisconnect.add("");
		}
		sjDisconnect.add("&cId : &4%s&c".replaceFirst("%s", String.valueOf(sanction.getId())));
		sjDisconnect.add("");
		//			if (!sanction.isPermanent())
		//				sjDisconnect.add("&2&n&lIl est impossible de faire une demande de débannissement pour un ban temporaire.");
		BaseComponent[] msgDisconnect = TextComponent.fromLegacyText(BungeeUtils.connectScreen(sjDisconnect.toString()));
		return msgDisconnect;
	}

}
