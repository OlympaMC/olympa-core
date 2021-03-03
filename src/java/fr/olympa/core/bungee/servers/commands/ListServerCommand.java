package fr.olympa.core.bungee.servers.commands;

import java.util.StringJoiner;

import fr.olympa.api.bungee.command.BungeeCommand;
import fr.olympa.api.chat.ColorUtils;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.server.ServerStatus;
import fr.olympa.api.utils.Utils;
import fr.olympa.api.utils.spigot.TPSUtils;
import fr.olympa.core.bungee.servers.MonitorServers;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Plugin;

public class ListServerCommand extends BungeeCommand {

	public ListServerCommand(Plugin plugin) {
		super(plugin, "listserv", OlympaCorePermissions.SERVER_LIST_COMMAND, "listserver");
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onCommand(CommandSender sender, String[] args) {
		StringJoiner sj = new StringJoiner("\n");
		sj.add("&6Liste des serveurs :");
		MonitorServers.getServersSorted().forEach(serverInfo -> {
			ServerStatus status = serverInfo.getStatus();
			StringJoiner sb = new StringJoiner(" ");
			sb.add("&7[" + status.getNameColored() + "&7]");
			sb.add(status.getColor() + serverInfo.getName() + "&e:");
			sb.add(serverInfo.getRangeVersion());
			if (serverInfo.getOnlinePlayers() != null)
				sb.add(serverInfo.getOnlinePlayers() + "/" + serverInfo.getMaxPlayers());
			if (serverInfo.getTps() != null)
				sb.add(TPSUtils.getTpsColor(serverInfo.getTps()) + "tps");
			if (serverInfo.getPing() != null)
				sb.add(serverInfo.getPing() + "ms");
			if (serverInfo.getRamUsage() != null)
				sb.add(TPSUtils.getRamUsageColor(serverInfo.getRamUsage()) + "% RAM");
			if (serverInfo.getThreads() != null)
				sb.add(serverInfo.getThreads() + "/" + serverInfo.getAllThreads() + " threads");
			if (serverInfo.getLastModifiedCore() > 0)
				sb.add("&6Last up core : " + Utils.tsToShortDur(serverInfo.getLastModifiedCore()));
			if (serverInfo.getError() != null && !serverInfo.getError().isBlank())
				sb.add(status.getColor() + "Erreur : " + serverInfo.getError());
			sj.add(sb.toString());
		});
		sender.sendMessage(ColorUtils.color(sj.toString()));
	}

}
