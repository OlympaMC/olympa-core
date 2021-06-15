package fr.olympa.core.bungee.servers.commands;

import java.util.StringJoiner;

import fr.olympa.api.bungee.command.BungeeCommand;
import fr.olympa.api.common.chat.TxtComponentBuilder;
import fr.olympa.api.common.permission.list.OlympaCorePermissionsBungee;
import fr.olympa.api.common.server.ServerInfoAdvanced;
import fr.olympa.api.common.server.ServerStatus;
import fr.olympa.api.spigot.utils.TPSUtils;
import fr.olympa.core.bungee.servers.MonitorServers;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Plugin;

public class ListServerCommand extends BungeeCommand {

	public ListServerCommand(Plugin plugin) {
		super(plugin, "listserver", OlympaCorePermissionsBungee.SERVER_LIST_COMMAND);
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		TxtComponentBuilder out = new TxtComponentBuilder("&6Liste des serveurs :").extraSpliterBN();
		MonitorServers.getServersSorted().forEach(serverInfo -> {
			ServerStatus status = serverInfo.getStatus();
			ServerInfoAdvanced debugInfo = serverInfo.getServerDebugInfo();
			TxtComponentBuilder out2 = new TxtComponentBuilder("&7[%s&7]", status.getNameColored()).extraSpliter(" ");
			out2.extra("%s%s", status.getColor(), serverInfo.getName());
			if (serverInfo.getOnlinePlayers() != null)
				out2.extra("%s/%s", serverInfo.getOnlinePlayers(), serverInfo.getMaxPlayers());
			if (serverInfo.getTps() != null)
				out2.extra("%stps", TPSUtils.getTpsColor(serverInfo.getTps()));
			if (debugInfo != null && debugInfo.getCPUUsage() != null)
				out2.extra("%scpu", debugInfo.getCPUUsage());
			if (serverInfo.getError() != null && !serverInfo.getError().isBlank())
				out2.extra("%sErreur : %s", status.getColor(), serverInfo.getError());

			StringJoiner sb = new StringJoiner(" &7", "&7", "");
			sb.add(serverInfo.getHumanName());
			if (serverInfo.getPing() != null)
				sb.add(serverInfo.getPing() + "ms");
			if (serverInfo.getRamUsage() != null)
				sb.add(TPSUtils.getRamUsageColor(serverInfo.getRamUsage()) + "% RAM&7");
			if (!serverInfo.getRangeVersion().equals("unknown"))
				sb.add(serverInfo.getRangeVersion());
			if (debugInfo != null && debugInfo.getPlugins() != null && !debugInfo.getPlugins().isEmpty())
				try {
					StringJoiner sb2 = new StringJoiner(", ");
					debugInfo.getPlugins().forEach(plugin -> {
						sb2.add(plugin.getNameColored() + "&7 (" + plugin.getVersion() + " " + plugin.getLastModifiedTime() + ")");
					});
					//					sb.add("\nPlugins : " + debugInfo.getPlugins().stream()
					//							.map(plugin -> (plugin.isEnabled() ? "&2" : "&4") + plugin.getName() + "&7 (" + plugin.getVersion() + " " + plugin.getLastModifiedTime() + ")")
					//							.collect(Collectors.joining(", ")));
					sb.add("\nPlugins : " + sb2.toString());
				} catch (ClassCastException e) {
					e.printStackTrace();
					// java.lang.ClassCastException: class java.util.TreeMap cannot be cast to class fr.olympa.api.common.plugin.PluginInfoAdvanced
					// (java.util.TreeMap is in module java.base of loader 'bootstrap'; fr.olympa.api.common.plugin.PluginInfoAdvanced is in unnamed
					// module of loader net.md_5.bungee.api.plugin.PluginClassloader @742d4e15)
				}
			else if (serverInfo.getLastModifiedCore() != null && !serverInfo.getLastModifiedCore().isBlank())
				sb.add("\nLast up core : " + serverInfo.getLastModifiedCore());

			out2.onHoverText(sb.toString()).console(proxiedPlayer == null);
			out.extra(out2);
		});
		sender.sendMessage(out.build());
	}

}
