package fr.olympa.core.bungee.servers.commands;

import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.server.OlympaServer;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.api.command.BungeeCommand;
import fr.olympa.core.bungee.datamanagment.DataHandler;
import fr.olympa.core.bungee.servers.ServersConnection;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ServerConnectEvent.Reason;
import net.md_5.bungee.api.plugin.Plugin;

public class LobbyCommand extends BungeeCommand {

	public LobbyCommand(Plugin plugin) {
		super(plugin, "lobby", OlympaCorePermissions.LOBBY_COMMAND, "hub");
		allowConsole = false;
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		String serverName = proxiedPlayer.getServer().getInfo().getName();
		if (DataHandler.isUnlogged(sender.getName())) {
			this.sendMessage(Prefix.DEFAULT_BAD, "Impossible ici.");
			return;
		} else if (serverName.startsWith("lobby")) {
			this.sendMessage(Prefix.DEFAULT_BAD, "Tu es déjà au lobby.");
			return;
		}

		ServerInfo lobby = ServersConnection.getBestServer(OlympaServer.LOBBY, null);
		if (lobby == null) {
			this.sendMessage(Prefix.DEFAULT_BAD, "Aucun serveur lobby n'est actuellement disponible.");
			return;
		}
		proxiedPlayer.connect(lobby, Reason.COMMAND);
		this.sendMessage(Prefix.DEFAULT_GOOD, "Tu es maintenant au &2" + Utils.capitalize(lobby.getName()) + "&a.");
	}
}
