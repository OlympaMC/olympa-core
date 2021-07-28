package fr.olympa.core.bungee.servers.commands;

import java.util.StringJoiner;

import fr.olympa.api.bungee.command.BungeeCommand;
import fr.olympa.api.common.chat.TxtComponentBuilder;
import fr.olympa.api.common.server.ServerStatus;
import fr.olympa.api.spigot.utils.TPSUtils;
import fr.olympa.core.bungee.servers.MonitorServers;
import fr.olympa.core.common.permission.list.OlympaCorePermissionsBungee;
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
			try {
				boolean hasFullInfo = serverInfo.hasFullInfos();
				ServerStatus status = serverInfo.getStatus();
				TxtComponentBuilder out2 = new TxtComponentBuilder("&7[%s&7]", status.getNameColored()).extraSpliter(" ");
				out2.extra("%s%s", status.getColor(), serverInfo.getName());
				if (serverInfo.getOnlinePlayers() != null)
					out2.extra("%s/%s", serverInfo.getOnlinePlayers(), serverInfo.getMaxPlayers());
				if (serverInfo.getTpsArray() != null)
					out2.extra("%stps", TPSUtils.getTpsColor(serverInfo.getTps()));
				if (hasFullInfo)
					out2.extra("%scpu", serverInfo.getCPUUsage());
				if (serverInfo.getError() != null && !serverInfo.getError().isBlank())
					out2.extra("%sErreur : %s", status.getColor(), serverInfo.getError());

				StringJoiner sb = new StringJoiner(" &7", "&7", "");
				sb.add(serverInfo.getHumanName());
				if (serverInfo.getPing() != null)
					sb.add(serverInfo.getPing() + "ms");
				if (hasFullInfo) {
					sb.add(serverInfo.getMemUsage() + " RAM&7");
					sb.add(serverInfo.getRangeVersionMinecraft());
				}
				if (hasFullInfo && serverInfo.getPlugins() != null && !serverInfo.getPlugins().isEmpty())
					try {
						StringJoiner sb2 = new StringJoiner(", ");
						serverInfo.getPlugins().stream().forEach(plugin -> {
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
				out2.onHoverText(sb.toString()).console(proxiedPlayer == null);
				out.extra(out2);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		sender.sendMessage(out.build());
	}

}
