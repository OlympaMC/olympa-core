package fr.olympa.core.bungee.commands;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import fr.olympa.api.bungee.command.BungeeCommand;
import fr.olympa.api.common.chat.TxtComponentBuilder;
import fr.olympa.api.common.permission.list.OlympaAPIPermissionsGlobal;
import fr.olympa.api.common.server.ServerInfoAdvanced;
import fr.olympa.api.common.server.ServerInfoAdvancedBungee;
import fr.olympa.core.bungee.servers.MonitorServers;
import fr.olympa.core.common.permission.list.OlympaCorePermissionsBungee;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Plugin;

public class AllPluginsCommand extends BungeeCommand {

	public AllPluginsCommand(Plugin plugin) {
		super(plugin, "allplugins", "Affiche tous les plugins par serveur", OlympaCorePermissionsBungee.ALLPLUGINS_COMMAND, "plall");
		minArg = 0;
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		TxtComponentBuilder out = new TxtComponentBuilder("&eListe non exhaustive des plugins des serveurs > ").extraSpliter("\n\n");
		Collection<ServerInfoAdvancedBungee> allServers = MonitorServers.getServersWithBungee();
		List<ServerInfoAdvanced> servers = allServers.stream().filter(mib -> mib.hasFullInfos()).collect(Collectors.toList());
		boolean withVersion = olympaPlayer != null ? OlympaAPIPermissionsGlobal.PLUGINS_SEE_VALUE_VERSION.hasPermission(olympaPlayer) : true;
		servers.stream().forEach(server -> {
			TxtComponentBuilder serverPlugins = new TxtComponentBuilder("&3" + server.getHumanName() + "&b > ");
			serverPlugins.extra(ServerInfoAdvanced.getPluginsToString(server.getPlugins(), isConsole(), withVersion));
			out.extra(serverPlugins);
		});
		sendMessage(out.build());
	}
}
