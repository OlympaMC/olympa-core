package fr.olympa.core.bungee.login.commands;

import java.sql.SQLException;

import fr.olympa.api.bungee.command.BungeeCommand;
import fr.olympa.api.player.OlympaConsole;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.ban.SanctionManager;
import fr.olympa.core.bungee.ban.objects.OlympaSanctionType;
import fr.olympa.core.bungee.datamanagment.DataHandler;
import fr.olympa.core.bungee.login.HandlerLogin;
import fr.olympa.core.bungee.login.events.OlympaPlayerLoginEvent;
import fr.olympa.core.bungee.utils.BungeeUtils;
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
		if (olympaPlayer == null)
			sendImpossibleWithOlympaPlayer();
		String playerPasswordHash = olympaPlayer.getPassword();
		if (playerPasswordHash == null || playerPasswordHash.isEmpty()) {
			sendError("Tu n'as pas de mot de passe. Pour le créer, fais &4/register <mot de passe>&c.");
			return;
		}

		if (args.length == 0 || args.length > 2) {
			sendUsage();
			return;
		}

		if (olympaPlayer.getPassword() == null) {
			sendUsage();
			return;
		}
		if (!olympaPlayer.isSamePassword(args[0])) {
			this.sendMessage(Prefix.DEFAULT_BAD, "Mot de passe incorrect, rééssaye.");
			String ip = proxiedPlayer.getAddress().getAddress().getHostAddress();
			Integer timeFails = HandlerLogin.timesFails.getIfPresent(ip);
			if (timeFails == null)
				HandlerLogin.timesFails.put(ip, 1);
			else if (timeFails < 3)
				HandlerLogin.timesFails.put(ip, ++timeFails);
			else if (timeFails < 10)
				proxiedPlayer.disconnect(BungeeUtils.connectScreen("Tu as fais trop de tentatives de mot de passe."));
			else
				try {
					SanctionManager.addAndApply(OlympaSanctionType.BANIP, OlympaConsole.getId(), ip, "Trop de tentatives de mot de passe.", Utils.getCurrentTimeInSeconds() + 60 * 60 * 60);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			return;
		}
		OlympaPlayerLoginEvent olympaPlayerLoginEvent = ProxyServer.getInstance().getPluginManager().callEvent(new OlympaPlayerLoginEvent(olympaPlayer, proxiedPlayer));
		if (olympaPlayerLoginEvent.cancelIfNeeded())
			return;
		this.sendMessage(Prefix.DEFAULT_GOOD, "Connexion effectuée, transfert en cours...");
	}
}