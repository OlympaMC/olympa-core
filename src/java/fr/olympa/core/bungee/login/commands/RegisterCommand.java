package fr.olympa.core.bungee.login.commands;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.bungee.api.command.BungeeCommand;
import fr.olympa.core.bungee.datamanagment.DataHandler;
import fr.olympa.core.bungee.login.HandlerLogin;
import fr.olympa.core.bungee.login.events.OlympaPlayerLoginEvent;
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
		for (String aliase : aliases) {
			HandlerLogin.command.add(aliase);
		}
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		olympaPlayer = getOlympaPlayer();
		if (olympaPlayer == null) {
			sendImpossibleWithOlympaPlayer();
		}
		String playerPasswordHash = olympaPlayer.getPassword();
		if (playerPasswordHash != null) {
			sendErreur("Tu as déjà un mot de passe. Pour le changer, fait &4/passwd <ancien mot de passe> <nouveau mot de passe>&c.");
			return;
		}

		if (args.length == 0) {
			this.sendMessage(Prefix.DEFAULT_GOOD, "Cette commande permet de choisir un mot de passe pour son compte Olympa. Valable sur notre site, forum, et minecraft (obligatoire si version non premium).");
			return;
		}

		if (args.length > 2) {
			sendUsage();
			return;
		}

		String password = args[0];

		if (password.length() < 5) {
			this.sendMessage(Prefix.DEFAULT_BAD, "Désolé 5 charatères minimum.");
			return;
		}

		if (password.length() > 100) {
			this.sendMessage(Prefix.DEFAULT_BAD, "Désolé 100 charatères maximum.");
			return;
		}

		Set<String> disallowPassword = new HashSet<>(Arrays.asList("azerty", "qwerty", "12345", "01234"));
		if (disallowPassword.stream().anyMatch(dis -> dis.equalsIgnoreCase(password) || password.startsWith(dis))) {
			this.sendMessage(Prefix.DEFAULT_BAD, "Désolé ce mot de passe n'est pas possible.");
			return;
		}

		olympaPlayer.setPassword(password);
		AccountProvider account = new AccountProvider(olympaPlayer.getUniqueId());
		if (DataHandler.isUnlogged(proxiedPlayer)) {
			this.sendMessage(Prefix.DEFAULT_GOOD, "Bravo ! Tu peux désormais utiliser ce mot de passe sur notre site et forum.");
		} else {
			OlympaPlayerLoginEvent olympaPlayerLoginEvent = ProxyServer.getInstance().getPluginManager().callEvent(new OlympaPlayerLoginEvent(olympaPlayer, proxiedPlayer));
			if (olympaPlayerLoginEvent.cancelIfNeeded()) {
				return;
			}
			this.sendMessage(Prefix.DEFAULT_GOOD, "Yes, ton compte est crée.");

		}
		account.saveToRedis(olympaPlayer);
	}

}
