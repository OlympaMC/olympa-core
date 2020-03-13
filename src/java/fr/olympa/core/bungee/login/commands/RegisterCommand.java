package fr.olympa.core.bungee.login.commands;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.bungee.api.command.BungeeCommand;
import fr.olympa.core.bungee.login.HandlerLogin;
import fr.olympa.core.bungee.login.events.OlympaPlayerLoginEvent;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

public class RegisterCommand extends BungeeCommand {

	public RegisterCommand(Plugin plugin) {
		super(plugin, "register", "reg", "enregistrement");
		this.usageString = "<mot de passe>";
		this.description = "Crée un mot de passe";
		this.allowConsole = false;
		this.bypassAuth = true;
		HandlerLogin.command.add(this.command);
		for (String aliase : this.aliases) {
			HandlerLogin.command.add(aliase);
		}
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		this.olympaPlayer = this.getOlympaPlayer();
		if (this.olympaPlayer == null) {
			this.sendImpossibleWithOlympaPlayer();
		}
		String playerPasswordHash = this.olympaPlayer.getPassword();
		if (playerPasswordHash != null) {
			this.sendErreur("Tu as déjà un mot de passe. Pour le changer, fait &4/passwd <ancien mot de passe> <nouveau mot de passe>&c.");
			return;
		}

		if (args.length == 0) {
			this.sendMessage(Prefix.DEFAULT_GOOD, "Cette commande permet de choisir un mot de passe pour son compte Olympa. Valable sur notre site, forum, et minecraft (obligatoire si version non premium).");
			return;
		}

		if (args.length > 2) {
			this.sendUsage();
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

		this.olympaPlayer.setPassword(password);
		AccountProvider account = new AccountProvider(this.olympaPlayer.getUniqueId());
		if (HandlerLogin.isLogged(this.proxiedPlayer)) {
			this.sendMessage(Prefix.DEFAULT_GOOD, "Bravo ! Tu peux désormais utiliser ce mot de passe sur notre site et forum.");
		} else {
			// aucune idée de pourquoi j'ai fais ça
			/*if (account.getFromCache() != null) {
				account.saveToCache(this.olympaPlayer);
			}*/
			OlympaPlayerLoginEvent olympaPlayerLoginEvent = ProxyServer.getInstance().getPluginManager().callEvent(new OlympaPlayerLoginEvent(this.olympaPlayer, this.proxiedPlayer));
			if (olympaPlayerLoginEvent.cancelIfNeeded()) {
				return;
			}
			this.sendMessage(Prefix.DEFAULT_GOOD, "Yes, ton compte est crée.");

		}
		account.saveToRedis(this.olympaPlayer);
	}

}
