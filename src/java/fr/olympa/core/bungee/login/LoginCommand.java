package fr.olympa.core.bungee.login;

import fr.olympa.api.provider.OlympaPlayerObject;
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
		HandlerHideLogin.command.add(this.command);
		for (String aliase : this.aliases) {
			HandlerHideLogin.command.add(aliase);
		}
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
			this.sendErreur("Tu n'a pas de mot de passe. Pour le crée, fait &4/register <mot de passe>&c.");
			return;
		}

		if (args.length == 0 || args.length > 2) {
			this.sendUsage();
			return;
		}

		String password = args[0];
		String newPasswordHash = ((OlympaPlayerObject) this.olympaPlayer).hashPassword(password);

		System.out.println("playerPasswordHash: " + playerPasswordHash + " newPasswordHash: " + newPasswordHash);
		if (!newPasswordHash.equals(playerPasswordHash)) {
			this.sendMessage(Prefix.DEFAULT_BAD, "Mot de passe incorrect, réessaye.");
			return;
		}
		this.sendMessage(Prefix.DEFAULT_GOOD, "Connexion effectuée, transfère en cours ...");
		ServerInfo server = ServersConnection.getLobby();
		this.proxiedPlayer.connect(server, Reason.COMMAND);
	}

}