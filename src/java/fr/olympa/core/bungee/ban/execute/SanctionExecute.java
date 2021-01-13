package fr.olympa.core.bungee.ban.execute;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;

import fr.olympa.api.bungee.command.BungeeCommand;
import fr.olympa.api.chat.ColorUtils;
import fr.olympa.api.match.RegexMatcher;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.player.OlympaConsole;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.sql.MySQL;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.ban.SanctionUtils;
import fr.olympa.core.bungee.ban.objects.OlympaSanctionStatus;
import fr.olympa.core.bungee.ban.objects.OlympaSanctionType;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;

public class SanctionExecute {

	public static SanctionExecute formatArgs(String[] args) {
		SanctionExecute me = new SanctionExecute();
		me.setTargetsString(Arrays.asList(args[0].split(",")));
		me.getTargetsString().forEach(target -> {
			if (RegexMatcher.IP.is(target))
				try {
					me.targetsRaw.add(InetAddress.getByName(target));
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
			else if (RegexMatcher.UUID.is(target))
				me.targetsRaw.add(RegexMatcher.UUID.parse(target));
			else if (RegexMatcher.USERNAME.is(target))
				me.targetsRaw.add(target);
			else if (RegexMatcher.LONG.is(target))
				me.targetsRaw.add(RegexMatcher.LONG.parse(target));
			else
				me.unknownTargetType.add(target);
		});
		String allArgs = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
		String reason = null;
		Matcher matcherDuration = SanctionUtils.matchDuration(allArgs);
		if (matcherDuration.find()) {
			String time = matcherDuration.group(1);
			String unit = matcherDuration.group(3);
			reason = allArgs.substring(matcherDuration.group().length());
			me.setExpire(SanctionUtils.toTimeStamp(Integer.parseInt(time), unit));
		} else
			reason = allArgs;
		me.setReason(reason);
		return me;
	}

	private List<String> targetsString;
	private List<Object> targetsRaw = new ArrayList<>();
	private List<String> unknownTargetType = new ArrayList<>();
	long expire = 0;
	String reason;

	OlympaPlayer author;

	private List<SanctionExecuteTarget> targets = new ArrayList<>();
	OlympaSanctionType sanctionType;
	OlympaSanctionStatus newStatus;

	public List<String> getTargetsString() {
		return targetsString;
	}

	public SanctionExecute() {
	}

	public void setTargetsString(List<String> targetsString) {
		this.targetsString = targetsString;
	}

	public boolean isPermanant() {
		return expire == 0;
	}

	public void setTargetsRaw(List<Object> targetsRaw) {
		this.targetsRaw = targetsRaw;
	}

	public void setUnknownTargetType(List<String> unknownTargetType) {
		this.unknownTargetType = unknownTargetType;
	}

	public List<String> getUnknownTargetType() {
		return unknownTargetType;
	}

	public void setExpire(long expire) {
		this.expire = expire;
	}

	public void setReason(String reason) {
		this.reason = Utils.capitalize(SanctionUtils.formatReason(reason));
	}

	public void setSanctionType(OlympaSanctionType sanctionType) {
		this.sanctionType = sanctionType;
	}

	public CommandSender getAuthorSender() {
		return author != null ? ProxyServer.getInstance().getPlayer(author.getUniqueId()) : ProxyServer.getInstance().getConsole();
	}

	public OlympaPlayer getAuthor() {
		try {
			return author != null ? author : new AccountProvider(OlympaConsole.getUniqueId()).get();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public long getAuthorId() {
		return author != null ? author.getId() : 0L;
	}

	public void setAuthor(OlympaPlayer author) {
		this.author = author;
	}

	public void launchSanction(BungeeCommand bungeeCommand, OlympaSanctionStatus newStatus) {
		this.newStatus = newStatus;
		if (printfErrorIfAny() || !getOlympaPlayersFromArgs())
			return;
		for (SanctionExecuteTarget target : targets)
			try {
				if (!target.save(this))
					continue;
				target.execute(this);
				target.annonce(this);
			} catch (SQLException e) {
				bungeeCommand.sendError(e);
			}
	}

	private boolean printfErrorIfAny() {
		if (targetsRaw.isEmpty())
			if (!unknownTargetType.isEmpty())
				getAuthorSender().sendMessage(Prefix.DEFAULT_BAD.formatMessageB("Le format de &4%s&c n'est pas connu. Utilise l'un des format suivants: pseudo, IP ou UUID.", ColorUtils.joinRed(unknownTargetType)));
			else
				getAuthorSender().sendMessage(Prefix.DEFAULT_BAD.formatMessageB("Rajoute une cible : pseudo, IP ou UUID."));
		else if (reason == null)
			getAuthorSender().sendMessage(Prefix.DEFAULT_BAD.formatMessageB("Chaque sanction doit contenir une raison."));
		else if (sanctionType == null)
			getAuthorSender().sendMessage(Prefix.DEFAULT_BAD.formatMessageB("Le type de sanction n'est pas connu."));
		else if (newStatus != OlympaSanctionStatus.ACTIVE && expire != 0)
			getAuthorSender().sendMessage(Prefix.DEFAULT_BAD.formatMessageB("Impossible de mettre une durée dans un %s.", newStatus.getPrefix() + sanctionType.getName()));
		else if (newStatus == OlympaSanctionStatus.ACTIVE && expire == 0 && OlympaCorePermissions.BAN_BANDEF_COMMAND.hasSenderPermissionBungee(getAuthorSender())) {
			String s = sanctionType.getName();
			getAuthorSender().sendMessage(Prefix.DEFAULT_BAD.formatMessageB("Tu n'as pas la permission de &4%s&c définitivement. Rajoute une durée tel que &4/%s %s 7jours %s", s, s.toLowerCase(), String.join(",", targetsString), reason));
		} else
			return false;
		return true;
	}

	private boolean getOlympaPlayersFromArgs() {
		for (Object t : targetsRaw)
			try {
				if (t instanceof InetAddress)
					targets.add(new SanctionExecuteTarget(t, MySQL.getPlayersByIp(((InetAddress) t).getHostAddress())));
				else if (t instanceof UUID)
					targets.add(new SanctionExecuteTarget(t, Arrays.asList(new AccountProvider((UUID) t).get())));
				else if (t instanceof String)
					targets.add(new SanctionExecuteTarget(t, Arrays.asList(AccountProvider.get((String) t))));
				else if (t instanceof Integer)
					targets.add(new SanctionExecuteTarget(t, Arrays.asList(AccountProvider.get((Integer) t))));
			} catch (SQLException e) {
				e.printStackTrace();
			}
		// Never join data
		if (targets.isEmpty())
			getAuthorSender().sendMessage(Prefix.DEFAULT_BAD.formatMessageB("&cCible &4%s&c introuvable.", ColorUtils.joinRed(targetsString)));
		return !targets.isEmpty();
	}

}
