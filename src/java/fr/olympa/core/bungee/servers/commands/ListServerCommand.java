package fr.olympa.core.bungee.servers.commands;

import java.util.StringJoiner;

import fr.olympa.api.bungee.command.BungeeCommand;
import fr.olympa.api.chat.TxtComponentBuilder;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.server.ServerStatus;
import fr.olympa.api.utils.spigot.TPSUtils;
import fr.olympa.core.bungee.servers.MonitorServers;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Plugin;

public class ListServerCommand extends BungeeCommand {

	public ListServerCommand(Plugin plugin) {
		super(plugin, "listserv", OlympaCorePermissions.SERVER_LIST_COMMAND, "listserver");
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		TxtComponentBuilder out = new TxtComponentBuilder("&6Liste des serveurs :").extraSpliterBN();
		MonitorServers.getServersSorted().forEach(serverInfo -> {
			ServerStatus status = serverInfo.getStatus();
			TxtComponentBuilder out2 = new TxtComponentBuilder().extraSpliter(" ");

			out2.extra("&7[%s&7]", status.getNameColored());
			out2.extra("%s%s&e:", status.getColor(), serverInfo.getName());
			if (serverInfo.getOnlinePlayers() != null)
				out2.extra("%s/%s", serverInfo.getOnlinePlayers(), serverInfo.getMaxPlayers());
			if (serverInfo.getTps() != null)
				out2.extra("%stps", TPSUtils.getTpsColor(serverInfo.getTps()));
			if (serverInfo.getError() != null && !serverInfo.getError().isBlank())
				out2.extra("%dErreur : %s", status.getColor(), serverInfo.getError());
			out2.extra(serverInfo.getRangeVersion());

			StringJoiner sb = new StringJoiner(" ", "&e", "");
			if (serverInfo.getRamUsage() != null)
				sb.add(TPSUtils.getRamUsageColor(serverInfo.getRamUsage()) + "%%RAM");
			if (serverInfo.getThreads() != null)
				sb.add(serverInfo.getThreads() + "threads");
			if (serverInfo.getPing() != null)
				sb.add(serverInfo.getPing() + "ms");
			if (serverInfo.getLastModifiedCore() != null && !serverInfo.getLastModifiedCore().isBlank())
				sb.add("\n&6Last up core : " + serverInfo.getLastModifiedCore());

			out2.onHoverText(sb.toString()).console(proxiedPlayer == null);
			out.extra(out2);
		});
		sender.sendMessage(out.build());
	}

}
