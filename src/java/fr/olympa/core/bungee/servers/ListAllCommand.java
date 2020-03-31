package fr.olympa.core.bungee.servers;

import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import fr.olympa.api.maintenance.MaintenanceStatus;
import fr.olympa.api.utils.Matcher;
import fr.olympa.api.utils.TPSUtils;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.api.command.BungeeCommand;
import fr.olympa.core.bungee.servers.MonitorServers.Ping;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.ServerPing.PlayerInfo;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;

public class ListAllCommand extends BungeeCommand {

	public ListAllCommand(Plugin plugin) {
		super(plugin, "listall");
	}

	// TODO add restriction for seeing serveurs
	// Clear un peu ce code degeux
	@Override
	public void onCommand(CommandSender sender, String[] args) {
		Map<ServerInfo, Ping> servers = MonitorServers.getServers();

		TextComponent text = new TextComponent("§6Liste des joueurs et serveurs:\n");
		int size = servers.entrySet().size();
		int i = 1;
		for (Entry<ServerInfo, Ping> entry : servers.entrySet()) {
			ServerInfo info = entry.getKey();
			Ping ping = entry.getValue();
			ServerPing serverPing = ping.getServerPing();
			String tps = "";
			String players = "";
			String hover = "";
			MaintenanceStatus status = MaintenanceStatus.CLOSE;
			if (serverPing != null) {
				String[] motd = serverPing.getDescriptionComponent().toLegacyText().split(" ");
				MaintenanceStatus status2 = MaintenanceStatus.get(motd[0]);
				System.out.println("Status " + info.getName() + ": " + status2 + " MOTD: " + motd[0] + " PING: " + ping.getPing() + " ms");
				if (status2 != null) {
					if (olympaPlayer != null && status2.getPermission().hasPermission(olympaPlayer)) {
						continue;
					}
					status = status2;
				} else {
					status = MaintenanceStatus.UNKNOWN;
				}
				if (motd.length >= 2) {
					String tpsString = motd[1];
					if (Matcher.isDouble(tpsString)) {
						tps = TPSUtils.getColor(Double.parseDouble(motd[1]));
					}
				}
				players = serverPing.getPlayers().getOnline() + " connecté" + Utils.withOrWithoutS(serverPing.getPlayers().getOnline()) + "";
				if (serverPing.getPlayers().getSample() != null) {
					hover = Arrays.stream(serverPing.getPlayers().getSample()).map(PlayerInfo::getName).collect(Collectors.joining(", "));
				}
			}
			if (size > i) {
				tps += "\n";
			}
			TextComponent text2 = new TextComponent("§7[" + status.getNameColored() + "§7] " + status.getColor() + info.getName() + "§e: " + players + " " + tps);
			if (!hover.isEmpty()) {
				text2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hover).color(ChatColor.GREEN).create()));
			}
			text.addExtra(text2);
			i++;
		}
		sender.sendMessage(text);
	}

}
