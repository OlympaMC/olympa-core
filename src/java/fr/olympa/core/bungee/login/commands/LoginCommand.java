package fr.olympa.core.bungee.login.commands;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import fr.olympa.api.provider.OlympaPlayerObject;
import fr.olympa.api.utils.Passwords;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.bungee.api.command.BungeeCommand;
import fr.olympa.core.bungee.login.HandlerLogin;
import fr.olympa.core.bungee.login.events.OlympaPlayerLoginEvent;
import fr.olympa.core.bungee.utils.BungeeUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

public class LoginCommand extends BungeeCommand {

	public LoginCommand(Plugin plugin) {
		super(plugin, "login", "log", "l");
		usageString = "<mot de passe>";
		description = "Permet de se connecter à son compte Olympa";
		allowConsole = false;
		bypassAuth = true;
		HandlerLogin.command.add(command);
		for (String aliase : aliases) {
			HandlerLogin.command.add(aliase);
		}
	}

	public String get_SHA_512_SecurePassword(String passwordToHash, String salt) {
		String generatedPassword = null;
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-512");
			md.update(salt.getBytes(StandardCharsets.UTF_8));
			byte[] bytes = md.digest(passwordToHash.getBytes(StandardCharsets.UTF_8));
			StringBuilder sb = new StringBuilder();
			for (byte b : bytes) {
				sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
			}
			generatedPassword = sb.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return generatedPassword;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onCommand(CommandSender sender, String[] args) {
		if (HandlerLogin.isLogged(proxiedPlayer)) {
			this.sendMessage(Prefix.DEFAULT_BAD, "Tu es déjà connecté.");
			return;
		}
		olympaPlayer = getOlympaPlayer();
		if (olympaPlayer == null) {
			sendImpossibleWithOlympaPlayer();
		}
		String playerPasswordHash = olympaPlayer.getPassword();
		if (playerPasswordHash == null || playerPasswordHash.isEmpty()) {
			sendErreur("Tu n'a pas de mot de passe. Pour le créer, fait &4/register <mot de passe>&c.");
			return;
		}

		if (args.length == 0 || args.length > 2) {
			sendUsage();
			return;
		}

		String password = args[0];
		String newPasswordHash = ((OlympaPlayerObject) olympaPlayer).hashPassword(password);
		Passwords.getSHA512(password, "DYhG9guiRVoUubWwvn2G0Fg3b0qyJfIxfs2aC9mi".getBytes());

		System.out.println("playerPasswordHash: " + playerPasswordHash);
		System.out.println("newPasswordHash: " + newPasswordHash);
		System.out.println("newPasswordHash2: " + get_SHA_512_SecurePassword(password, "DYhG9guiRVoUubWwvn2G0Fg3b0qyJfIxfs2aC9mi"));
		if (!newPasswordHash.equals(playerPasswordHash)) {
			this.sendMessage(Prefix.DEFAULT_BAD, "Mot de passe incorrect, rééssaye.");
			Integer timeFails = HandlerLogin.timesFails.get(proxiedPlayer);
			if (timeFails == null) {
				HandlerLogin.timesFails.put(proxiedPlayer, 1);
			} else if (timeFails <= 3) {
				HandlerLogin.timesFails.put(proxiedPlayer, ++timeFails);
			} else {
				proxiedPlayer.disconnect(BungeeUtils.connectScreen("Tu as échoué trop de fois ton mdp"));
			}

			return;
		}
		OlympaPlayerLoginEvent olympaPlayerLoginEvent = ProxyServer.getInstance().getPluginManager().callEvent(new OlympaPlayerLoginEvent(olympaPlayer, proxiedPlayer));
		if (olympaPlayerLoginEvent.cancelIfNeeded()) {
			return;
		}
		this.sendMessage(Prefix.DEFAULT_GOOD, "Connexion effectuée, transfert en cours ...");
	}
}