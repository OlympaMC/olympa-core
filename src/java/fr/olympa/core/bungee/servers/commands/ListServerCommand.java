package fr.olympa.core.bungee.servers.commands;

import java.util.StringJoiner;
import java.util.stream.Collectors;

import fr.olympa.api.bungee.command.BungeeCommand;
import fr.olympa.api.chat.TxtComponentBuilder;
import fr.olympa.api.permission.list.OlympaCorePermissionsBungee;
import fr.olympa.api.server.ServerDebug;
import fr.olympa.api.server.ServerStatus;
import fr.olympa.api.sort.Sorting;
import fr.olympa.api.utils.spigot.TPSUtils;
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
		//		MonitorServers.getServersSorted().forEach(serverInfo -> {
		//			ServerStatus status = serverInfo.getStatus();
		//			TxtComponentBuilder out2 = new TxtComponentBuilder("&7[%s&7]", status.getNameColored()).extraSpliter(" ");
		//			out2.extra("%s%s&e:", status.getColor(), serverInfo.getName());
		//			if (serverInfo.getOnlinePlayers() != null)
		//				out2.extra("%s/%s", serverInfo.getOnlinePlayers(), serverInfo.getMaxPlayers());
		//			if (serverInfo.getTps() != null)
		//				out2.extra("%stps", TPSUtils.getTpsColor(serverInfo.getTps()));
		//			if (serverInfo.getError() != null && !serverInfo.getError().isBlank())
		//				out2.extra("%dErreur : %s", status.getColor(), serverInfo.getError());
		//			out2.extra(serverInfo.getRangeVersion());
		//
		//			StringJoiner sb = new StringJoiner(" ", "&e", "");
		//			if (serverInfo.getRamUsage() != null)
		//				sb.add(TPSUtils.getRamUsageColor(serverInfo.getRamUsage()) + "%%RAM&7");
		//			if (serverInfo.getThreads() != null)
		//				sb.add(serverInfo.getThreads() + "threads");
		//			if (serverInfo.getPing() != null)
		//				sb.add(serverInfo.getPing() + "ms");
		//			if (serverInfo.getLastModifiedCore() != null && !serverInfo.getLastModifiedCore().isBlank())
		//				sb.add("\n&6Last up core : " + serverInfo.getLastModifiedCore());
		//
		//			out2.onHoverText(sb.toString()).console(proxiedPlayer == null);
		//			out.extra(out2);
		//		});
		MonitorServers.getServersSorted().forEach(serverInfo -> {
			ServerStatus status = serverInfo.getStatus();
			ServerDebug debugInfo = serverInfo.getServerDebugInfo();
			TxtComponentBuilder out2 = new TxtComponentBuilder("&7[%s&7]", status.getNameColored()).extraSpliter(" ");
			out2.extra("%s%s&e ", status.getColor(), serverInfo.getName());
			if (serverInfo.getOnlinePlayers() != null)
				out2.extra("%s/%s", serverInfo.getOnlinePlayers(), serverInfo.getMaxPlayers());
			if (serverInfo.getTps() != null)
				out2.extra("%stps", TPSUtils.getTpsColor(serverInfo.getTps()));
			if (debugInfo != null && debugInfo.getCPUUsage() != null)
				out2.extra("%scpu", debugInfo.getCPUUsage());
			if (serverInfo.getError() != null && !serverInfo.getError().isBlank())
				out2.extra("%sErreur : %s", status.getColor(), serverInfo.getError());
			if (!serverInfo.getRangeVersion().equals("unknown"))
				out2.extra(serverInfo.getRangeVersion());

			StringJoiner sb = new StringJoiner(" ", "&7", "");
			if (serverInfo.getRamUsage() != null)
				sb.add(TPSUtils.getRamUsageColor(serverInfo.getRamUsage()) + "% RAM");
			if (serverInfo.getPing() != null)
				sb.add(serverInfo.getPing() + "ms");
			if (debugInfo != null && debugInfo.getPlugins() != null && !debugInfo.getPlugins().isEmpty())
				sb.add("\nPlugins Maison : " + debugInfo.getPlugins().stream()
						.filter(plugin -> plugin.getName().startsWith("Olympa") || plugin.getAuthors().contains("SkytAsul") || plugin.getAuthors().contains("Tristiisch"))
						.sorted(new Sorting<>(dp -> dp.getName().startsWith("Olympa") ? 1l : 0l))
						.map(plugin -> (plugin.isEnabled() ? "&2" : "&4") + plugin.getName() + "&7 (" + plugin.getVersion() + " " + plugin.getLastModifiedTime() + ")")
						.collect(Collectors.joining(", ")));
			else if (serverInfo.getLastModifiedCore() != null && !serverInfo.getLastModifiedCore().isBlank())
				sb.add("\nLast up core : " + serverInfo.getLastModifiedCore());

			out2.onHoverText(sb.toString()).console(proxiedPlayer == null);
			out.extra(out2);
		});
		sender.sendMessage(out.build());
	}

}
