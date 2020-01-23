package fr.olympa.core.bungee.servers;

import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import fr.olympa.api.maintenance.MaintenanceStatus;
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
			String pingProtocol = "";
			String players = "";
			String playersNames = "";
			MaintenanceStatus status = MaintenanceStatus.CLOSE;
			if (ping != null) {
				String motd = ping.getDescriptionComponent().toLegacyText();
				MaintenanceStatus status2 = MaintenanceStatus.get(motd.substring(2));
				System.out.println("MOTD " + info.getName() + ": " + motd.substring(2) + " " + status2);
				if (status2 != null) {
					status = status2;
				} else {
					status = MaintenanceStatus.OPEN;
				}
				pingProtocol = ping.getVersion().getName();
				players = ping.getPlayers().getOnline() + " connecté" + Utils.withOrWithoutS(ping.getPlayers().getOnline()) + "";
				if (ping.getPlayers().getSample() != null) {
					playersNames = Arrays.stream(ping.getPlayers().getSample()).map(PlayerInfo::getName).collect(Collectors.joining(", "));
				}
			}
			i++;
			if (size > i) {
				pingProtocol += "\n";
			}
			TextComponent text2 = new TextComponent("§7[" + status.getNameColored() + "§7] " + status.getColor() + info.getName() + "§e: " + players + " " + pingProtocol);
			if (!playersNames.isEmpty()) {
				text2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(playersNames).color(ChatColor.GREEN).create()));
			}
			text.addExtra(text2);
		}
		sender.sendMessage(text);
	}

}
