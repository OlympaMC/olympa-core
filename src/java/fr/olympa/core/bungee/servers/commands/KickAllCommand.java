package fr.olympa.core.bungee.servers.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import fr.olympa.api.bungee.command.BungeeCommand;
import fr.olympa.api.common.server.OlympaServer;
import fr.olympa.api.common.server.ServerInfoAdvancedBungee;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.servers.MonitorServers;
import fr.olympa.core.common.permission.list.OlympaCorePermissionsBungee;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

public class KickAllCommand extends BungeeCommand {
	
	public KickAllCommand(Plugin plugin) {
		super(plugin, "kickall", "Envoie tous les joueurs d'un serveur sur des lobbies.", OlympaCorePermissionsBungee.SERVER_KICKALL_COMMAND);
		minArg = 1;
		usageString = "<server>";
	}
	
	@Override
	public void onCommand(CommandSender sender, String[] args) {
		ServerInfo server = ProxyServer.getInstance().getServerInfo(args[0]);
		if (server == null) {
			sendError("Serveur introuvable.");
			return;
		}
		List<ServerInfo> lobbies = MonitorServers.getServers(OlympaServer.LOBBY).values().stream()
				.filter(x -> x.hasMinimalInfo() && x.getStatus().canConnect() && !x.getName().equals(server.getName())
						&& x.getMaxPlayers() * 0.9 > x.getOnlinePlayers())/*.sorted((o1, o2) -> o1.get)*/.map(ServerInfoAdvancedBungee::getServerInfo).toList();
		if (lobbies.isEmpty()) {
			sendError("Il n'y a aucun serveur lobby disponible.");
			return;
		}
		List<ProxiedPlayer> players = new ArrayList<>(server.getPlayers());
		sendInfo("Envoi de %d joueurs sur %d lobbies.", players.size(), lobbies.size());
		int index = 0;
		for (ProxiedPlayer player : players) {
			ServerInfo info = lobbies.get(index++);
			if (index == lobbies.size()) index = 0;
			player.connect(info, (x, error) -> {
				if (Boolean.FALSE.equals(x) || error != null) {
					sendMessage(sender, Prefix.ERROR, "Impossible d'envoyer le joueur %s au lobby. Erreur : §c", player.getName(), Objects.toString(error));
				}
			});
		}
	}
	
	@Override
	public Iterable<String> onTabComplete(CommandSender sender, BungeeCommand command, String[] args) {
		if (args.length == 0)
			return OlympaBungee.getInstance().getProxy().getServersCopy().keySet();
		else if (args.length == 1)
			return Utils.startWords(args[0], OlympaBungee.getInstance().getProxy().getServersCopy().keySet());
		return null;
	}
	
}
