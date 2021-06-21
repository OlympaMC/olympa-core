package fr.olympa.core.bungee.commands;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import fr.olympa.api.bungee.command.BungeeCommand;
import fr.olympa.api.common.chat.TxtComponentBuilder;
import fr.olympa.api.common.permission.list.OlympaCorePermissionsBungee;
import fr.olympa.api.common.server.ServerInfoAdvanced;
import fr.olympa.api.common.server.ServerInfoAdvancedBungee;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.servers.MonitorInfoBungee;
import fr.olympa.core.bungee.servers.MonitorServers;
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
		Collection<MonitorInfoBungee> allServers = MonitorServers.getServers();
		List<MonitorInfoBungee> servers = allServers.stream().filter(mib -> mib.getServerDebugInfo() != null).collect(Collectors.toList());
		ServerInfoAdvancedBungee bungeeServerInfo = new ServerInfoAdvancedBungee(OlympaBungee.getInstance());
		if (bungeeServerInfo.getPlugins() != null && !bungeeServerInfo.getPlugins().isEmpty()) {
			TxtComponentBuilder serverPlugins = new TxtComponentBuilder("&3" + bungeeServerInfo.getHumanName() + "&b > ");
			serverPlugins.extra(ServerInfoAdvanced.getPluginsToString(bungeeServerInfo.getPlugins(), isConsole(), true));
			out.extra(serverPlugins);
		}
		servers.forEach(server -> {
			TxtComponentBuilder serverPlugins = new TxtComponentBuilder("&3" + server.getHumanName() + "&b > ");
			serverPlugins.extra(ServerInfoAdvanced.getPluginsToString(server.getServerDebugInfo().getPlugins(), isConsole(), true));
			out.extra(serverPlugins);
		});
		sendMessage(out.build());
	}
}
