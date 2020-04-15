package fr.olympa.core.bungee.servers;

import fr.olympa.api.utils.ColorUtils;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.utils.BungeeUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ServersListener implements Listener {

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
				event.setKickReason(BungeeUtils.connectScreen("&eLe &6" + Utils.capitalize(serverKicked.getName()) + "&e s'est redémarré, merci de te reconnecter dans quelques secondes..."));
				return;
			}
			event.setCancelServer(serverInfolobby);
			player.sendMessage(ColorUtils.color(Prefix.DEFAULT + "&eLe &6" + Utils.capitalize(serverKicked.getName()) + "&e s'est redémarré, tu es désormais au " + Utils.capitalize(serverInfolobby.getName()) + "."));
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
