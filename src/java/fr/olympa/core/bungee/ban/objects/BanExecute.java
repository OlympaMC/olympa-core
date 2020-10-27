package fr.olympa.core.bungee.ban.objects;

import java.net.InetAddress;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import fr.olympa.api.player.OlympaConsole;
import fr.olympa.api.utils.ColorUtils;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.bungee.ban.SanctionManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

@SuppressWarnings("deprecation")
public class BanExecute {

	List<Object> targets;
	long expire = 0;
	String reason;
	OlympaSanctionType sanctionType;
	ProxiedPlayer author;

	public BanExecute() {
	}

	public boolean isPermanant() {
		return expire == 0;
	}

	public void setTargets(List<Object> targets) {
		this.targets = targets;
	}

	public void setExpire(long expire) {
		this.expire = expire;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public void setSanctionType(OlympaSanctionType sanctionType) {
		this.sanctionType = sanctionType;
	}

	public CommandSender getAuthor() {
		return author != null ? author : ProxyServer.getInstance().getConsole();
	}

	public UUID getAuthorUuid() {
		return author != null ? author.getUniqueId() : OlympaConsole.getUniqueId();
	}

	public void setAuthor(ProxiedPlayer author) {
		this.author = author;
	}

	public void execute() {
		if (printfErrorIfAny())
			return;
		for (Object t : targets) {
			boolean hasWork;
			OlympaSanctionType tSanctionType = sanctionType;
			if (sanctionType == OlympaSanctionType.BAN && t instanceof InetAddress) {
				t = ((InetAddress) t).getHostAddress();
				tSanctionType = OlympaSanctionType.BANIP;
			}
			try {
				hasWork = SanctionManager.addAndApply(tSanctionType, getAuthorUuid(), t, reason, expire);
				if (!hasWork)
					getAuthor().sendMessage(Prefix.DEFAULT_BAD + ColorUtils.color("'" + t + "' est inconnu dans la base de données."));
			} catch (SQLException e) {
				e.printStackTrace();
				getAuthor().sendMessage(Prefix.DEFAULT_BAD + ColorUtils.color("Une erreur SQL est survenue lors de la sanction de '" + t + "'."));
			}
		}
	}

	private boolean printfErrorIfAny() {
		if (reason == null)
			getAuthor().sendMessage(Prefix.DEFAULT_BAD + ColorUtils.color("Chaques sanctions doit contenir une raison."));
		else if (targets == null)
			getAuthor().sendMessage(Prefix.DEFAULT_BAD + ColorUtils.color("La/Les cibles n'ont pas été trouvés. Utilise l'un des format suivants: pseudo, IP ou UUID."));
		else if (sanctionType == null)
			getAuthor().sendMessage(Prefix.DEFAULT_BAD + ColorUtils.color("Le type de sanction n'est pas connu."));
		else
			return false;
		return true;
	}
}
