package fr.olympa.core.bungee.servers;

import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.SpigotUtils;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.utils.BungeeUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ServersListener implements Listener {

	@EventHandler
	public void onServerConnect(ServerConnectEvent event) {
		ProxiedPlayer player = event.getPlayer();
		if (player.getServer() != null) {
			ServersConnection.removeTryToConnect(player);
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onServerKick(ServerKickEvent event) {
		ServerInfo serverKicked = event.getKickedFrom();
		String kickReason = ChatColor.stripColor(BaseComponent.toLegacyText(event.getKickReasonComponent()));
		ProxiedPlayer player = event.getPlayer();
		System.out.println("KICK " + player.getName() + " FOR " + kickReason + " " + event.getState());
		if (kickReason.contains("whitelist")) {
			event.setKickReasonComponent(TextComponent.fromLegacyText(BungeeUtils.connectScreen("&cTu n'a pas accès au serveur &4" + serverKicked.getName() + "&c.")));
			return;
		}
		if (kickReason.contains("restarting") || kickReason.contains("closed")) {

			ServerInfo serverInfolobby = ServersConnection.getLobby(serverKicked);
			if (serverInfolobby == null) {
				serverInfolobby = ServersConnection.getAuth(serverKicked);
			}
			if (serverInfolobby == null) {
				event.setKickReasonComponent(TextComponent.fromLegacyText(BungeeUtils.connectScreen("&eLe &6" + Utils.capitalize(serverKicked.getName()) + "&e s'est redémarré, merci de te reconnecter dans quelques secondes...")));
				return;
			}
			event.setCancelServer(serverInfolobby);
			event.setCancelled(true);
			player.sendMessage(SpigotUtils.color(Prefix.DEFAULT + "&eLe &6" + Utils.capitalize(serverKicked.getName()) + "&e s'est redémarré, tu es désormais au " + Utils.capitalize(serverInfolobby.getName()) + "."));
			return;
		}

		if (!kickReason.contains("ban")) {
			ServerInfo serverInfolobby = ServersConnection.getLobby(serverKicked);
			if (serverInfolobby == null) {
				return;
			}
			event.setCancelServer(serverInfolobby);
		}
	}
}
