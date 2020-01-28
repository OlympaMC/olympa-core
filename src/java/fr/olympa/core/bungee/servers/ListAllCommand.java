package fr.olympa.core.bungee.servers;

import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import fr.olympa.api.maintenance.MaintenanceStatus;
import fr.olympa.api.utils.Matcher;
import fr.olympa.api.utils.TPS;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.api.command.BungeeCommand;
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
		Map<ServerInfo, ServerPing> servers = MonitorServers.getServers();

		TextComponent text = new TextComponent("§6Liste des joueurs et serveurs:\n");
		int size = servers.entrySet().size();
		int i = 1;
		for (Entry<ServerInfo, ServerPing> entry : servers.entrySet()) {
			ServerInfo info = entry.getKey();
			ServerPing ping = entry.getValue();
			String tps = "";
			String players = "";
			String hover = "";
			MaintenanceStatus status = MaintenanceStatus.CLOSE;
			if (ping != null) {
				String[] motd = ping.getDescriptionComponent().toLegacyText().split(" ");
				MaintenanceStatus status2 = MaintenanceStatus.get(motd[0]);
				System.out.println("Status " + info.getName() + ": " + status2 + " MOTD: " + motd[0]);
				if (status2 != null) {
					if (this.olympaPlayer != null && status2.getPermission().hasPermission(this.olympaPlayer)) {
						continue;
					}
					status = status2;
				} else {
					status = MaintenanceStatus.UNKNOWN;
				}
				if (motd.length >= 2) {
					String tpsString = motd[1];
					if (Matcher.isDouble(tpsString)) {
						tps = TPS.getColor(Double.parseDouble(motd[1]));
					}
				}
				players = ping.getPlayers().getOnline() + " connecté" + Utils.withOrWithoutS(ping.getPlayers().getOnline()) + "";
				if (ping.getPlayers().getSample() != null) {
					hover = Arrays.stream(ping.getPlayers().getSample()).map(PlayerInfo::getName).collect(Collectors.joining(", "));
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
