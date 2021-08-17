package fr.olympa.core.bungee.ban.execute;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;

import javax.annotation.Nullable;

import fr.olympa.api.bungee.command.BungeeCommand;
import fr.olympa.api.common.chat.ColorUtils;
import fr.olympa.api.common.chat.TxtComponentBuilder;
import fr.olympa.api.common.match.RegexMatcher;
import fr.olympa.api.common.player.OlympaConsole;
import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.common.sanction.OlympaSanctionType;
import fr.olympa.api.common.utils.SanctionUtils;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.ban.BanMySQL;
import fr.olympa.core.bungee.ban.objects.OlympaSanction;
import fr.olympa.core.bungee.ban.objects.OlympaSanctionStatus;
import fr.olympa.core.common.permission.list.OlympaCorePermissionsBungee;
import fr.olympa.core.common.provider.AccountProvider;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;

public class SanctionExecute {

	public static SanctionExecute formatArgs(BungeeCommand bungeeCommand, String[] args) {
		SanctionExecute me;
		if (bungeeCommand.getOlympaPlayer() != null)
			me = new SanctionExecute(bungeeCommand);
		else
			try {
				me = new SanctionExecute();
			} catch (SQLException e) {
				e.printStackTrace();
				bungeeCommand.sendError(e);
				return null;
			}
		List<String> targetsString = Arrays.asList(args[0].split(","));
		for (String target : targetsString)
			if (RegexMatcher.IP.is(target))
				try {
					me.targetsRaw.add(InetAddress.getByName(target));
				} catch (UnknownHostException e) {
					e.printStackTrace();
					bungeeCommand.sendError(e);
				}
			else if (RegexMatcher.UUID.is(target))
				me.targetsRaw.add(RegexMatcher.UUID.parse(target));
			else if (RegexMatcher.LONG.is(target))
				me.targetsRaw.add(RegexMatcher.LONG.parse(target));
			else if (RegexMatcher.USERNAME.is(target))
				me.targetsRaw.add(target);
			else
				me.unknownTargetType.add(target);
		me.setTargetsString(targetsString);
		String allArgs = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
		String reason = null;
		Matcher matcherDuration = SanctionUtils.matchDuration(allArgs);
		if (matcherDuration.find()) {
			String time = matcherDuration.group(1);
			String unit = matcherDuration.group(3);
			reason = allArgs.substring(matcherDuration.group().length() + 1);
			if (reason.isBlank())
				reason = null;
			me.setExpire(SanctionUtils.toTimeStamp(Integer.parseInt(time), unit));
		} else
			reason = allArgs;
		me.setReason(reason);
		return me;
	}

	@Nullable
	BungeeCommand cmd;

	private List<String> targetsString;
	private List<Object> targetsRaw = new ArrayList<>();
	private List<String> unknownTargetType = new ArrayList<>();
	long expire = 0;
	@Nullable
	String reason;

	OlympaPlayer author;
	CommandSender authorSender;

	private List<SanctionExecuteTarget> targets = new ArrayList<>();
	OlympaSanctionType sanctionType;
	OlympaSanctionStatus newStatus;

	public List<String> getTargetsString() {
		return targetsString;
	}

	public SanctionExecute(CommandSender authorSender, OlympaPlayer author) {
		this.authorSender = authorSender;
		this.author = author;
	}

	private SanctionExecute(BungeeCommand cmd) {
		this(cmd.getSender(), cmd.getOlympaPlayer());
		this.cmd = cmd;
	}

	public SanctionExecute() throws SQLException {
		this(ProxyServer.getInstance().getConsole(), new AccountProvider(OlympaConsole.getUniqueId()).get());
	}

	public void setTargetsString(List<String> targetsString) {
		this.targetsString = targetsString;
	}

	public List<String> getUnknownTargetType() {
		return unknownTargetType;
	}

	public CommandSender getAuthorSender() {
		return authorSender;
	}

	public OlympaPlayer getAuthor() {
		return author;
	}

	public long getAuthorId() {
		return author.getId();
	}

	public boolean isPermanant() {
		return expire == 0;
	}

	/**
	 * @param expire TimeStamp in seconds
	 */
	public void setExpire(long expire) {
		this.expire = expire;
	}

	public void setReason(String reason) {
		if (reason != null)
			this.reason = Utils.capitalize(SanctionUtils.formatReason(reason));
	}

	public void setTargetsRaw(List<Object> targetsRaw) {
		this.targetsRaw = targetsRaw;
	}

	public void setUnknownTargetType(List<String> unknownTargetType) {
		this.unknownTargetType = unknownTargetType;
	}

	public void setSanctionType(OlympaSanctionType sanctionType) {
		this.sanctionType = sanctionType;
	}

	public void sendError(Throwable e) {
		if (cmd != null)
			cmd.sendError(e);
	}

	public void addTarget(InetAddress address) {
		targetsRaw.add(address);
		targetsString.add(address.getHostAddress());
	}

	public void addTarget(OlympaPlayer player) {
		targetsRaw.add(player.getId());
		targetsString.add(player.getName());
	}

	public void launchSanction(OlympaSanctionStatus newStatus) {
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
				e.printStackTrace();
				sendError(e);
			}
	}

	private boolean checkValidTarget() {
		List<String> unknownTarget = new ArrayList<>();
		if (targets.isEmpty())
			getAuthorSender().sendMessage(Prefix.DEFAULT_BAD.formatMessageB("&cCible &4%s&c introuvable.", ColorUtils.joinRed(targetsString)));
		return false;
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
		else if (newStatus == OlympaSanctionStatus.ACTIVE && expire == 0 && !OlympaCorePermissionsBungee.BAN_BANDEF_COMMAND.hasSenderPermissionBungee(getAuthorSender())) {
			String s = sanctionType.getName();
			getAuthorSender().sendMessage(Prefix.DEFAULT_BAD.formatMessageB("Tu n'as pas la permission de &4%s&c définitivement. Rajoute une durée tel que &4/%s %s 7jours %s", s, s.toLowerCase(), String.join(",", targetsString), reason));
		} else
			return false;
		return true;
	}

	private boolean getOlympaPlayersFromArgs() {
		for (Object t : targetsRaw) {
			List<OlympaPlayer> olympaPlayers = null;
			try {
				if (t instanceof InetAddress)
					olympaPlayers = AccountProvider.getter().getSQL().getPlayersByIp(((InetAddress) t).getHostAddress());
				else {
					OlympaPlayer olympaPlayer = null;
					if (t instanceof UUID)
						olympaPlayer = new AccountProvider((UUID) t).get();
					else if (t instanceof String)
						olympaPlayer = AccountProvider.getter().get((String) t);
					else if (t instanceof Long)
						olympaPlayer = AccountProvider.getter().get((Long) t);
					if (olympaPlayer != null)
						olympaPlayers = Arrays.asList(olympaPlayer);
				}
				if (olympaPlayers != null && !olympaPlayers.isEmpty())
					targets.add(new SanctionExecuteTarget(t, olympaPlayers));
			} catch (SQLException e) {
				e.printStackTrace();
				sendError(e);
			}
		}
		// Never join data
		if (targets.isEmpty())
			getAuthorSender().sendMessage(Prefix.DEFAULT_BAD.formatMessageB("&cCible &4%s&c introuvable.", ColorUtils.joinRed(targetsString)));
		return !targets.isEmpty();
	}

	private boolean detectSanctions() {
		for (Object t : targetsRaw)
			try {
				if (t instanceof InetAddress)
					targets.add(new SanctionExecuteTarget(BanMySQL.getSanctions(((InetAddress) t).getHostAddress()), t));
				else if (t instanceof UUID) {
					OlympaPlayer op = new AccountProvider((UUID) t).get();
					if (op != null)
						targets.add(new SanctionExecuteTarget(BanMySQL.getSanctions(op.getId()), t));
				} else if (t instanceof String) {
					OlympaPlayer op = AccountProvider.getter().get((String) t);
					if (op != null)
						targets.add(new SanctionExecuteTarget(BanMySQL.getSanctions(op.getId()), t));
				} else if (t instanceof Long)
					targets.add(new SanctionExecuteTarget(Arrays.asList(BanMySQL.getSanction((Long) t)), t));
			} catch (SQLException e) {
				e.printStackTrace();
				sendError(e);
			}
		return !targets.isEmpty();
	}

	public void printInfo() {
		if (targetsRaw.isEmpty())
			if (!unknownTargetType.isEmpty())
				getAuthorSender().sendMessage(Prefix.DEFAULT_BAD.formatMessageB("Le format de &4%s&c n'est pas connu. Utilise l'un des format suivants: pseudo, IP, UUID ou ID de sanction.", ColorUtils.joinRed(unknownTargetType)));
			else
				getAuthorSender().sendMessage(Prefix.DEFAULT_BAD.formatMessageB("Rajoute une cible : pseudo, IP, UUID ou sanction."));
		if (!detectSanctions())
			return;

		List<OlympaSanction> allSanctions = new ArrayList<>();
		for (SanctionExecuteTarget t : targets)
			if (t != null)
				allSanctions.addAll(t.getSanctions());
		if (allSanctions.isEmpty())
			getAuthorSender().sendMessage(Prefix.DEFAULT_BAD.formatMessageB("&cCasier de &4%s&c vierge.", ColorUtils.joinRed(targetsString)));
		else if (allSanctions.size() == 1) {
			OlympaSanction s = allSanctions.get(0);
			getAuthorSender().sendMessage(Prefix.DEFAULT_GOOD.formatMessageB("Une seule sanction trouvée pour %s.", ColorUtils.joinGreenEt(targetsString)));
			getAuthorSender().sendMessage(s.toBaseComplement());
		} else {
			TxtComponentBuilder builder = new TxtComponentBuilder(Prefix.DEFAULT_GOOD, "%d résultats pour %s.", allSanctions.size(), ColorUtils.joinGreenEt(targetsString)).extraSpliterBN();
			for (OlympaSanction sanction : allSanctions)
				builder.extra(new TxtComponentBuilder("%d - %s %s %s %s", sanction.getId(), sanction.getStatus().getNameColored(),
						sanction.getType().getName(!sanction.isPermanent()), sanction.getAuthorName(), sanction.getReason(),
						sanction.getExpires() > 0 ? Utils.timeToDuration(sanction.getBanTime()) : sanction.getType() != OlympaSanctionType.KICK ? "&cPermanant" : "")
								.onHoverText(sanction.toBaseComplement()).onClickCommand("/histban %d", sanction.getId()));
			getAuthorSender().sendMessage(builder.build());
		}
	}

}
