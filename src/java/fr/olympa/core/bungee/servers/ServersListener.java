package fr.olympa.core.bungee.servers;

import fr.olympa.api.utils.SpigotUtils;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.utils.BungeeUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ServersListener implements Listener {

	@EventHandler
	public void ServerKickEvent(ServerKickEvent event) {
		ServerInfo serveurKicked = event.getKickedFrom();
		if (serveurKicked.getName().contains("auth")) {
			return;
		}
		ProxiedPlayer player = event.getPlayer();
		String kickReason = ChatColor.stripColor(BaseComponent.toLegacyText(event.getKickReasonComponent()));
		System.out.println("KICK " + player.getName() + " FOR " + kickReason + " " + event.getState());

		if (kickReason.contains("restarting") || kickReason.contains("closed")) {

			ServerInfo server = event.getKickedFrom();
			ServerInfo serverInfolobby = ProxyServer.getInstance().getServers().values().stream().filter(Sname -> server != Sname && Sname.getName().startsWith("lobby")).findFirst().orElse(null);
			if (serverInfolobby == null) {
				event.setKickReasonComponent(TextComponent.fromLegacyText(BungeeUtils.connectScreen("&eLe &6" + Utils.capitalize(server.getName()) + "&e s'est redémarré, merci de vous reconnecter.")));
				return;
			}
			event.setCancelServer(serverInfolobby);
			player.sendMessage(
					TextComponent.fromLegacyText(SpigotUtils.color("&6Olympa &7» &eLe &6" + Utils.capitalize(server.getName()) + "&e s'est redémarré, vous êtes désormais au " + Utils.capitalize(serverInfolobby.getName()) + ".")));
			return;
		} else if (!kickReason.contains("ban")) {
			ServerInfo server = event.getKickedFrom();
			ServerInfo serverInfolobby = ProxyServer.getInstance().getServers().values().stream().filter(Sname -> server != Sname && Sname.getName().startsWith("lobby")).findFirst().orElse(null);
			if (serverInfolobby == null) {
				return;
			}
			event.setCancelServer(serverInfolobby);
		} else {
			event.setKickReasonComponent(event.getKickReasonComponent());
		}
	}
}
