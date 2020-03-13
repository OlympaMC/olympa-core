package fr.olympa.core.bungee.login.commands;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.bungee.api.command.BungeeCommand;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Plugin;

public class PasswdCommand extends BungeeCommand {
	
	public PasswdCommand(Plugin plugin) {
		super(plugin, "passwd", "password", "mdp", "motdepasse");
		this.usageString = "<ancien mot de passe> <nouveau mot de passe>";
		this.description = "Change ton mot de passe";
		this.allowConsole = false;
		this.bypassAuth = false;
	}
	
	@Override
	public void onCommand(CommandSender sender, String[] args) {
		this.olympaPlayer = this.getOlympaPlayer();
		if (this.olympaPlayer == null) {
			this.sendImpossibleWithOlympaPlayer();
		}
		String playerPasswordHash = this.olympaPlayer.getPassword();
		if (playerPasswordHash == null) {
			this.sendErreur("Tu n'as pas encore de mot de passe, fait &4/register <mot de passe>&c pour en crée un.");
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
		account.saveToCache(this.olympaPlayer);
		account.saveToRedis(this.olympaPlayer);
		this.sendMessage(Prefix.DEFAULT_GOOD, "Bravo ! Tu peux désormais utiliser ton nouveau mdp sur le site.");
	}
}
