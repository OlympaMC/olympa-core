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
import fr.olympa.core.bungee.ban.objects.SanctionExecuteTarget;
import fr.olympa.core.bungee.utils.BungeeUtils;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class SanctionManager {

	public static int maxTimeBan = 527040;
	public static int minTimeBan = 600;

	public static int maxTimeMute;
	public static int minTimeMute;

	public static boolean addAndApply(OlympaSanctionType type, long authorId, Object target, String reason, long timestamp) throws SQLException {
		return addAndApply(type, authorId, target, reason, timestamp, OlympaSanctionStatus.ACTIVE, null);
	}

	@SuppressWarnings("deprecation")
	public static void annonce(SanctionExecuteTarget banExecuteTarget) {
		OlympaSanction sanction = banExecuteTarget.getSanction();
		String reason = SanctionUtils.formatReason(sanction.getReason());

		List<ProxiedPlayer> onlineTargets = banExecuteTarget.getPlayers();
		List<String> playersNames = new ArrayList<>();
		playersNames.addAll(banExecuteTarget.getOlympaPlayers().stream().map(OlympaPlayer::getName).collect(Collectors.toList()));
		String duration = null;
		if (!sanction.isPermanent())
			duration = Utils.timestampToDuration(sanction.getExpires());
		OlympaSanctionType type = sanction.getType();
		StringJoiner sjAnnonce = new StringJoiner(" ");
		sjAnnonce.add("&2[&c" + type.getName().toUpperCase() + "&2]");
		if (playersNames.size() > 1)
			sjAnnonce.add("&4%s&c ont été".replace("%s", String.join(", ", playersNames)));
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
		BungeeUtils.getPlayers(OlympaCorePermissions.BAN_SEEBANMSG, t -> t.forEach(p -> p.sendMessage(msgStaff)),
				t -> t.stream().filter(p -> onlineTargets.stream().anyMatch(p2 -> p2.getServer().getInfo().getName().equals(p.getServer().getInfo().getName()))).forEach(p -> p.sendMessage(msg)));
		ProxyServer.getInstance().getConsole().sendMessage(msgStaff);
	}

	public static boolean addAndApply(OlympaSanctionType type, long authorId, Object target, String reason, long timestamp, OlympaSanctionStatus status, List<OlympaPlayer> targets) throws SQLException {
		reason = SanctionUtils.formatReason(reason);

		List<ProxiedPlayer> onlineTargets = new ArrayList<>();
		List<String> playersNames = new ArrayList<>();
		if (targets != null && !targets.isEmpty()) {
			playersNames.addAll(targets.stream().map(OlympaPlayer::getName).collect(Collectors.toList()));
			onlineTargets.addAll(targets.stream().map(op -> ProxyServer.getInstance().getPlayer(op.getUniqueId())).filter(pd -> pd != null && pd.isConnected()).collect(Collectors.toList()));
		} else
			switch (type) {
			case MUTE:
			case KICK:
			case BAN:
				targets = new ArrayList<>();
				ProxiedPlayer t = null;
				if (target instanceof OlympaPlayer) {
					t = ProxyServer.getInstance().getPlayer(((OlympaPlayer) target).getUniqueId());
					targets.add((OlympaPlayer) target);
				} else if (target instanceof ProxiedPlayer) {
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
						op = AccountProvider.get((String) target);
					if (op != null) {
						targets.add(op);
						playersNames.add(op.getName());
					} else
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
		OlympaSanction sanction = null;
		if (targets == null || targets.isEmpty())
			//			sanction = SanctionExecuteTarget.add(type, author, target, reason, timestamp);
			sanction = SanctionExecuteTarget.add(type, authorId, target, reason, timestamp);
		else
			for (OlympaPlayer tr : targets)
				sanction = SanctionExecuteTarget.add(type, authorId, tr.getId(), reason, timestamp);
		String duration = null;
		if (!sanction.isPermanent())
			duration = Utils.timestampToDuration(sanction.getExpires());

		//		if (type == OlympaSanctionType.BAN || type == OlympaSanctionType.BANIP || type == OlympaSanctionType.KICK)
		//			for (ProxiedPlayer t : onlineTargets)
		//				t.disconnect(getDisconnectScreen(sanction));

		StringJoiner sjAnnonce = new StringJoiner(" ");
		sjAnnonce.add("&2[&c" + type.getName().toUpperCase() + "&2]");
		if (playersNames.size() > 1)
			sjAnnonce.add("&4%s&c ont été".replace("%s", String.join(", ", playersNames)));
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

}
