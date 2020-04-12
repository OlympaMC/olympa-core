package fr.olympa.core.bungee.servers;

import fr.olympa.api.utils.Prefix;
import fr.olympa.core.bungee.api.command.BungeeCommand;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ServerConnectEvent.Reason;
import net.md_5.bungee.api.plugin.Plugin;

public class LobbyCommand extends BungeeCommand {

	public LobbyCommand(Plugin plugin) {
		super(plugin, "lobby", "hub");
		allowConsole = false;
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		String serverName = proxiedPlayer.getServer().getInfo().getName();
		if (serverName.startsWith("auth")) {
			this.sendMessage(Prefix.DEFAULT_BAD, "Impossible ici.");
			return;
		} else if (serverName.startsWith("lobby")) {
			this.sendMessage(Prefix.DEFAULT_BAD, "Tu es déjà au lobby.");
			return;
		}

		ServerInfo lobby = ServersConnection.getLobby();
		if (lobby == null) {
			this.sendMessage(Prefix.DEFAULT_BAD, "Aucun serveur lobby n'est actuellement disponible.");
			return;
		}
		proxiedPlayer.connect(lobby, Reason.COMMAND);
		this.sendMessage(Prefix.DEFAULT_GOOD, "Tu es maintenant au &2" + lobby.getName() + "&a.");
	}
}
