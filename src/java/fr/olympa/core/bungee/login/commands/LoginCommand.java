package fr.olympa.core.bungee.login.commands;

import java.sql.SQLException;

import fr.olympa.api.bungee.command.BungeeCommand;
import fr.olympa.api.bungee.customevent.OlympaPlayerLoginEvent;
import fr.olympa.api.bungee.player.DataHandler;
import fr.olympa.api.bungee.utils.BungeeUtils;
import fr.olympa.api.common.sanction.OlympaSanctionType;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.ban.execute.SanctionExecute;
import fr.olympa.core.bungee.ban.objects.OlympaSanctionStatus;
import fr.olympa.core.bungee.login.HandlerLogin;
import fr.olympa.core.bungee.security.SecurityHandler;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

@SuppressWarnings("deprecation")
public class LoginCommand extends BungeeCommand {

	public LoginCommand(Plugin plugin) {
		super(plugin, "login", "log", "l");
		usageString = "<mot de passe>";
		description = "Permet de se connecter à son compte Olympa";
		allowConsole = false;
		bypassAuth = true;
		HandlerLogin.command.add(command);
		for (String aliase : aliases)
			HandlerLogin.command.add(aliase);
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		if (!DataHandler.isUnlogged(proxiedPlayer)) {
			this.sendMessage(Prefix.DEFAULT_BAD, "Tu es déjà connecté.");
			return;
		}
		olympaPlayer = getOlympaPlayer();
		if (olympaPlayer == null) {
			sendImpossibleWithOlympaPlayer();
			return;
		}
		String playerPasswordHash = olympaPlayer.getPassword();
		if (playerPasswordHash == null || playerPasswordHash.isEmpty()) {
			sendError("Tu n'as pas de mot de passe. Pour le créer, fais &4/register <mot de passe>&c.");
			return;
		}
		if (args.length == 0 || args.length > 2) {
			sendUsage();
			return;
		}
		if (!SecurityHandler.getInstance().getAntibot().getCase().canRegisterOrLogin(proxiedPlayer, args, "/login"))
			return;
		String testPassword = args[0];
		if (!olympaPlayer.isSamePassword(testPassword)) {
			String ip = proxiedPlayer.getAddress().getAddress().getHostAddress();
			Integer timeFails = HandlerLogin.timesFails.getIfPresent(ip);
			if (timeFails == null)
				HandlerLogin.timesFails.put(ip, 1);
			else if (timeFails < 3)
				HandlerLogin.timesFails.put(ip, ++timeFails);
			else if (timeFails < 5)
				proxiedPlayer.disconnect(BungeeUtils.connectScreen("Tu as fais trop de tentatives de mot de passe."));
			else if (timeFails == 5)
				proxiedPlayer.disconnect(BungeeUtils.connectScreen("Tu as fais trop de tentatives de mot de passe.\n&4&nAttention, au prochain mauvais mot de passe, tu sera ban temporairement."));
			else
				try {
					SanctionExecute banExecute = new SanctionExecute();
					banExecute.addTarget(proxiedPlayer.getAddress().getAddress());
					banExecute.setReason("Trop de tentatives de mot de passe sur le compte " + proxiedPlayer.getName() + ".");
					banExecute.setExpire(Utils.getCurrentTimeInSeconds() + 60 * 10);
					banExecute.setSanctionType(OlympaSanctionType.BAN);
					banExecute.launchSanction(OlympaSanctionStatus.ACTIVE);
					//SanctionManager.addAndApply(OlympaSanctionType.BANIP, OlympaConsole.getId(), ip, "Trop de tentatives de mot de passe.", Utils.getCurrentTimeInSeconds() + 60 * 60 * 60);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			this.sendMessage(Prefix.DEFAULT_BAD, "Mot de passe incorrect, rééssaye.");
			return;
		}
		OlympaPlayerLoginEvent olympaPlayerLoginEvent = ProxyServer.getInstance().getPluginManager().callEvent(new OlympaPlayerLoginEvent(olympaPlayer, proxiedPlayer));
		if (olympaPlayerLoginEvent.cancelIfNeeded())
			return;
		this.sendMessage(Prefix.DEFAULT_GOOD, "Connexion effectuée, transfert en cours...");
	}
}