package fr.olympa.core.bungee.login;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import fr.olympa.api.provider.OlympaPlayerObject;
import fr.olympa.api.utils.Passwords;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.bungee.api.command.BungeeCommand;
import fr.olympa.core.bungee.servers.ServersConnection;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ServerConnectEvent.Reason;
import net.md_5.bungee.api.plugin.Plugin;

public class LoginCommand extends BungeeCommand {

	public LoginCommand(Plugin plugin) {
		super(plugin, "login", "log", "l");
		this.usageString = "<mot de passe>";
		this.description = "Permet de se connecter à son compte Olympa";
		this.allowConsole = false;
		this.bypassAuth = true;
		HandlerHideLogin.command.add(this.command);
		for (String aliase : this.aliases) {
			HandlerHideLogin.command.add(aliase);
		}
	}

	public String get_SHA_512_SecurePassword(String passwordToHash, String salt) {
		String generatedPassword = null;
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-512");
			md.update(salt.getBytes(StandardCharsets.UTF_8));
			byte[] bytes = md.digest(passwordToHash.getBytes(StandardCharsets.UTF_8));
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < bytes.length; i++) {
				sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
			}
			generatedPassword = sb.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return generatedPassword;
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		if (!this.proxiedPlayer.getServer().getInfo().getName().startsWith("auth")) {
			this.sendMessage(Prefix.DEFAULT_BAD, "Tu es déjà connecté.");
			return;
		}
		this.olympaPlayer = this.getOlympaPlayer();
		if (this.olympaPlayer == null) {
			this.sendImpossibleWithOlympaPlayer();
		}
		String playerPasswordHash = this.olympaPlayer.getPassword();
		if (playerPasswordHash == null || playerPasswordHash.isEmpty()) {
			this.sendErreur("Tu n'a pas de mot de passe. Pour le créer, fait &4/register <mot de passe>&c.");
			return;
		}

		if (args.length == 0 || args.length > 2) {
			this.sendUsage();
			return;
		}

		String password = args[0];
		String newPasswordHash = ((OlympaPlayerObject) this.olympaPlayer).hashPassword(password);
		Passwords.getSHA512(password, "DYhG9guiRVoUubWwvn2G0Fg3b0qyJfIxfs2aC9mi".getBytes());

		System.out.println("playerPasswordHash: " + playerPasswordHash);
		System.out.println("newPasswordHash: " + newPasswordHash);
		System.out.println("newPasswordHash2: " + this.get_SHA_512_SecurePassword(password, "DYhG9guiRVoUubWwvn2G0Fg3b0qyJfIxfs2aC9mi"));
		if (!newPasswordHash.equals(playerPasswordHash)) {
			this.sendMessage(Prefix.DEFAULT_BAD, "Mot de passe incorrect, réessaye.");
			return;
		}
		this.sendMessage(Prefix.DEFAULT_GOOD, "Connexion effectuée, transfère en cours ...");
		ServerInfo server = ServersConnection.getLobby();
		this.proxiedPlayer.connect(server, Reason.COMMAND);
	}
}