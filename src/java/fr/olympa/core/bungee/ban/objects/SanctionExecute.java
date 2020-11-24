package fr.olympa.core.bungee.ban.objects;

import java.net.InetAddress;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import fr.olympa.api.bungee.command.BungeeCommand;
import fr.olympa.api.player.OlympaConsole;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.sql.MySQL;
import fr.olympa.api.utils.ColorUtils;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.ban.SanctionManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;

@SuppressWarnings("deprecation")
public class SanctionExecute {

	private List<String> targetsString;
	private List<Object> targetsRaw;
	private List<SanctionExecuteTarget> targets = new ArrayList<>();
	long expire = 0;
	String reason;
	OlympaSanctionType sanctionType;
	OlympaPlayer author;

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

	public void setTargets(List<Object> targetsRaw) {
		this.targetsRaw = targetsRaw;
	}

	public void setExpire(long expire) {
		this.expire = expire;
	}

	public void setReason(String reason) {
		this.reason = Utils.capitalize(reason);
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

	public void launchSanction(BungeeCommand bungeeCommand, OlympaSanctionStatus active) {
		if (printfErrorIfAny() || !getOlympaPlayersFromArgs())
			return;
		for (SanctionExecuteTarget target : targets)
			try {
				if (!target.save(this, active))
					return;
				target.execute(this);
				SanctionManager.annonce(target);
			} catch (SQLException e) {
				bungeeCommand.sendError(e);
			}
	}

	private boolean printfErrorIfAny() {
		if (reason == null)
			getAuthorSender().sendMessage(Prefix.DEFAULT_BAD + ColorUtils.color("Chaques sanctions doit contenir une raison."));
		else if (targetsRaw == null || targetsRaw.isEmpty())
			getAuthorSender().sendMessage(Prefix.DEFAULT_BAD + ColorUtils.color("La/Les cibles n'ont pas été trouvés. Utilise l'un des format suivants: pseudo, IP ou UUID."));
		else if (sanctionType == null)
			getAuthorSender().sendMessage(Prefix.DEFAULT_BAD + ColorUtils.color("Le type de sanction n'est pas connu."));
		else
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
			getAuthorSender().sendMessage(Prefix.DEFAULT_BAD + ColorUtils.color(String.format("&cCible &4%s&c inconnu.", String.join(", ", targetsString))));
		return !targets.isEmpty();
	}
}
