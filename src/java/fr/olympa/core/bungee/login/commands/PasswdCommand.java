package fr.olympa.core.bungee.login.commands;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import fr.olympa.api.bungee.command.BungeeCommand;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.Prefix;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

public class PasswdCommand extends BungeeCommand {

	public PasswdCommand(Plugin plugin) {
		super(plugin, "passwd", "password", "mdp", "motdepasse", "changepassword");
		usageString = "<ancien mot de passe> <nouveau mot de passe>";
		description = "Permet de changer son mot de passe";
		allowConsole = true;
		bypassAuth = false;
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		olympaPlayer = getOlympaPlayer();
		if (olympaPlayer != null && olympaPlayer.getPassword() == null) {
			sendError("Tu n'as pas encore de mot de passe, fais &4/register <mot de passe>&c pour en créer un.");
			return;
		}

		if (args.length == 0) {
			this.sendMessage(Prefix.DEFAULT_GOOD, "Cette commande permet de changer ton mot de passe Olympa: valable en jeux, sur le site et le forum.");
			return;
		}

		if (args.length < 2) {
			sendUsage();
			return;
		}
		String newPassword = args[1];
		if (sender instanceof ProxiedPlayer) {
			String oldPassword = args[0];
			if (!olympaPlayer.isSamePassword(oldPassword)) {
				this.sendMessage(Prefix.DEFAULT_BAD, "Ancien mot de passe incorrect, rééssaye.");
				return;
			}
			if (newPassword.length() < 5) {
				this.sendMessage(Prefix.DEFAULT_BAD, "Désolé, 5 caratères minimum.");
				return;
			}
			if (newPassword.length() > 100) {
				this.sendMessage(Prefix.DEFAULT_BAD, "Désolé, 100 caratères maximum.");
				return;
			}
			Set<String> disallowPassword = new HashSet<>(Arrays.asList("azerty", "qwerty", "12345", "01234"));
			if (disallowPassword.stream().anyMatch(dis -> dis.equalsIgnoreCase(newPassword) || newPassword.startsWith(dis))) {
				this.sendMessage(Prefix.DEFAULT_BAD, "Désolé, ce mot de passe n'est pas disponible car trop risqué.");
				return;
			}
		} else {
			try {
				olympaPlayer = AccountProvider.get(args[0]);
			} catch (SQLException e) {
				this.sendError(e.getMessage());
				e.printStackTrace();
				return;
			}
			if (olympaPlayer == null) {
				sendUnknownPlayer(args[0]);
				return;
			}
		}
		olympaPlayer.setPassword(newPassword);
		/*try {
			MySQL.savePlayerPassOrEmail(olympaPlayer);
		} catch (SQLException e) {
			this.sendMessage(Prefix.DEFAULT_BAD, "Impossible de sauvegarder le mot de passe. Contacte un membre de staff.");
			e.printStackTrace();
			return;
		}*/
		AccountProvider account = new AccountProvider(olympaPlayer.getUniqueId());
		account.saveToRedis(olympaPlayer);
		if (sender instanceof ProxiedPlayer)
			this.sendMessage(Prefix.DEFAULT_GOOD, "Bravo ! Tu peux désormais utiliser ton nouveau mot de passe sur le site.");
		else
			this.sendMessage(Prefix.DEFAULT_GOOD, "Le mot de passe de &2%s&a a été changé.", olympaPlayer.getName());
	}
}
