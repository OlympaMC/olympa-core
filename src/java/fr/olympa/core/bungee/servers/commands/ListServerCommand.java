package fr.olympa.core.bungee.servers.commands;

import java.util.Set;
import java.util.StringJoiner;

import fr.olympa.api.maintenance.MaintenanceStatus;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.utils.ColorUtils;
import fr.olympa.api.utils.TPSUtils;
import fr.olympa.core.bungee.api.command.BungeeCommand;
import fr.olympa.core.bungee.servers.MonitorInfo;
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
		sj.add("&6Liste des serveurs:");
		Set<MonitorInfo> info = MonitorServers.getLastServerInfo();
		for (MonitorInfo serverInfo : info) {
			MaintenanceStatus status = serverInfo.getStatus();
			StringJoiner sb = new StringJoiner(" ");
			sb.add("&7[" + status.getNameColored() + "&7]");
			sb.add(status.getColor() + serverInfo.getName() + "&e:");
			if (serverInfo.getOnlinePlayer() != null) {
				sb.add(serverInfo.getOnlinePlayer() + "/" + serverInfo.getMaxPlayers());
			}
			if (serverInfo.getTps() != null) {
				sb.add(TPSUtils.getColor(serverInfo.getTps()) + "tps");
			}
			if (serverInfo.getPing() != null) {
				sb.add(serverInfo.getPing() + "ms");
			}
			if (serverInfo.getError() != null) {
				sb.add(status.getColor() + "Erreur: " + serverInfo.getError());
			}
			sj.add(sb.toString());
		}
		sender.sendMessage(ColorUtils.color(sj.toString()));
	}

}
