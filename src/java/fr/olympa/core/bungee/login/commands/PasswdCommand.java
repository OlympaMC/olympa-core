package fr.olympa.core.bungee.login.commands;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.sql.MySQL;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.bungee.api.command.BungeeCommand;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Plugin;

public class PasswdCommand extends BungeeCommand {
	
	public PasswdCommand(Plugin plugin) {
		super(plugin, "passwd", "password", "mdp", "motdepasse");
		usageString = "<ancien mot de passe> <nouveau mot de passe>";
		description = "Permet de changer son mot de passe";
		allowConsole = false;
		bypassAuth = false;
	}
	
	@Override
	public void onCommand(CommandSender sender, String[] args) {
		olympaPlayer = getOlympaPlayer();
		if (olympaPlayer == null)
			sendImpossibleWithOlympaPlayer();
		String playerPasswordHash = olympaPlayer.getPassword();
		if (playerPasswordHash == null) {
			sendError("Tu n'as pas encore de mot de passe, fais &4/register <mot de passe>&c pour en créer un.");
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
			this.sendMessage(Prefix.DEFAULT_BAD, "Désolé, 5 caratères minimum.");
			return;
		}
		
		if (password.length() > 100) {
			this.sendMessage(Prefix.DEFAULT_BAD, "Désolé, 100 caratères maximum.");
			return;
		}
		
		Set<String> disallowPassword = new HashSet<>(Arrays.asList("azerty", "qwerty", "12345", "01234"));
		if (disallowPassword.stream().anyMatch(dis -> dis.equalsIgnoreCase(password) || password.startsWith(dis))) {
			this.sendMessage(Prefix.DEFAULT_BAD, "Désolé, ce mot de passe n'est pas possible car trop risqué.");
			return;
		}
		
		olympaPlayer.setPassword(password);
		try {
			MySQL.savePlayerPassOrEmail(olympaPlayer);
		} catch (SQLException e) {
			this.sendMessage(Prefix.DEFAULT_BAD, "Impossible de sauvegarder le mot de passe. Contacte un membre de staff.");
			e.printStackTrace();
			return;
		}
		AccountProvider account = new AccountProvider(olympaPlayer.getUniqueId());
		account.saveToRedis(olympaPlayer);
		this.sendMessage(Prefix.DEFAULT_GOOD, "Bravo ! Tu peux désormais utiliser ton nouveau mot de passe sur le site.");
	}
}
