package fr.olympa.core.bungee.commands;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import fr.olympa.api.bungee.command.BungeeCommand;
import fr.olympa.api.common.chat.TxtComponentBuilder;
import fr.olympa.api.common.server.ServerInfoAdvanced;
import fr.olympa.api.common.server.ServerInfoAdvancedBungee;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.servers.MonitorServers;
import fr.olympa.core.common.permission.list.OlympaCorePermissionsBungee;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Plugin;

public class AllPluginsCommand extends BungeeCommand {

	public AllPluginsCommand(Plugin plugin) {
		super(plugin, "allplugins", "Affiche tous les plugins par serveur", OlympaCorePermissionsBungee.ALLPLUGINS_COMMAND, "plall", "allpl");
		minArg = 0;
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		TxtComponentBuilder out = new TxtComponentBuilder("&eListe non exhaustive des plugins des serveurs > ").extraSpliter("\n\n");
		Collection<ServerInfoAdvancedBungee> allServers = MonitorServers.getServers();
		List<ServerInfoAdvanced> servers = allServers.stream().filter(mib -> mib.hasFullInfos()).collect(Collectors.toList());
		ServerInfoAdvancedBungee bungeeServerInfo = new ServerInfoAdvancedBungee(OlympaBungee.getInstance());
		if (bungeeServerInfo.getPlugins() != null && !bungeeServerInfo.getPlugins().isEmpty()) {
			TxtComponentBuilder serverPlugins = new TxtComponentBuilder("&3" + bungeeServerInfo.getHumanName() + "&b > ");
			serverPlugins.extra(ServerInfoAdvanced.getPluginsToString(bungeeServerInfo.getPlugins(), isConsole(), true));
			out.extra(serverPlugins);
		}
		servers.stream().forEach(server -> {
			TxtComponentBuilder serverPlugins = new TxtComponentBuilder("&3" + server.getHumanName() + "&b > ");
			serverPlugins.extra(ServerInfoAdvanced.getPluginsToString(server.getPlugins(), isConsole(), true));
			out.extra(serverPlugins);
		});
		sendMessage(out.build());
	}
}
