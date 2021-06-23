package fr.olympa.core.bungee.login.commands;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import fr.olympa.api.bungee.command.BungeeCommand;
import fr.olympa.api.bungee.customevent.OlympaPlayerLoginEvent;
import fr.olympa.api.bungee.player.DataHandler;
import fr.olympa.api.common.provider.AccountProvider;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.bungee.login.HandlerLogin;
import fr.olympa.core.bungee.redis.RedisBungeeSend;
import fr.olympa.core.bungee.security.SecurityHandler;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

public class RegisterCommand extends BungeeCommand {

	public RegisterCommand(Plugin plugin) {
		super(plugin, "register", "reg", "enregistrement");
		usageString = "<mot de passe>";
		description = "Crée un mot de passe";
		allowConsole = false;
		bypassAuth = true;
		HandlerLogin.command.add(command);
		for (String aliase : aliases)
			HandlerLogin.command.add(aliase);
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		olympaPlayer = getOlympaPlayer();
		if (olympaPlayer == null)
			sendImpossibleWithOlympaPlayer();
		String playerPasswordHash = olympaPlayer.getPassword();
		if (playerPasswordHash != null) {
			sendError("Tu as déjà un mot de passe. Pour le changer, fais &4/passwd <ancien mot de passe> <nouveau mot de passe>&c.");
			return;
		}
		if (args.length == 0) {
			this.sendMessage(Prefix.DEFAULT_GOOD, "Cette commande permet de choisir un mot de passe pour son compte Olympa. Valable sur notre site, forum, et minecraft (obligatoire si version non premium).");
			return;
		}
		if (args.length > 2) {
			sendUsage(command);
			return;
		}
		if (!SecurityHandler.getInstance().getAntibot().getCase().canRegisterOrLogin(proxiedPlayer, args, "/register"))
			return;
		String password = args[0];
		if (password.length() < 5) {
			this.sendMessage(Prefix.DEFAULT_BAD, "Désolé, 5 caratères minimum.");
			return;
		}
		if (password.length() > 100) {
			this.sendMessage(Prefix.DEFAULT_BAD, "Désolé, 100 caratères maximum.");
			return;
		}
		Set<String> disallowPassword = new HashSet<>(Arrays.asList("azerty", "qwerty", "12345", "01234", "123456789"));
		if (password.equals(sender.getName()) || disallowPassword.stream().anyMatch(dis -> dis.equalsIgnoreCase(password) || password.startsWith(dis))) {
			this.sendMessage(Prefix.DEFAULT_BAD, "Désolé, ce mot de passe n'est pas possible.");
			return;
		}
		olympaPlayer.setPassword(password);
		/*try {
			MySQL.savePlayerPassOrEmail(olympaPlayer);
		} catch (SQLException e) {
			this.sendMessage(Prefix.DEFAULT_BAD, "Impossible de sauvegarder le mot de passe. Contacte un membre de staff.");
			System.err.println("Password can't be save to db for %s1".replace("%s1", olympaPlayer.getName()));
			e.printStackTrace();
			return;
		}*/
		AccountProvider account = new AccountProvider(olympaPlayer.getUniqueId());
		account.saveToRedis(olympaPlayer);
		RedisBungeeSend.sendOlympaPlayer(proxiedPlayer.getServer().getInfo(), olympaPlayer);
		if (DataHandler.isUnlogged(proxiedPlayer)) {
			OlympaPlayerLoginEvent olympaPlayerLoginEvent = ProxyServer.getInstance().getPluginManager().callEvent(new OlympaPlayerLoginEvent(olympaPlayer, proxiedPlayer));
			if (olympaPlayerLoginEvent.cancelIfNeeded())
				return;
			this.sendMessage(Prefix.DEFAULT_GOOD, "Super ! Ton compte est créé.");
		} else {
			OlympaPlayerLoginEvent olympaPlayerLoginEvent = ProxyServer.getInstance().getPluginManager().callEvent(new OlympaPlayerLoginEvent(olympaPlayer, proxiedPlayer));
			if (olympaPlayerLoginEvent.cancelIfNeeded())
				return;
			this.sendMessage(Prefix.DEFAULT_GOOD, "Bravo ! Tu peux désormais utiliser ce mot de passe sur notre site et forum.");

		}
	}

}
