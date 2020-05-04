package fr.olympa.core.bungee.servers;

import java.util.concurrent.TimeUnit;

import fr.olympa.api.utils.ColorUtils;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.OlympaBungee;
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

			ServerInfo server = ServersConnection.getLobby(serverKicked);
			if (server == null) {
				server = ServersConnection.getAuth(serverKicked);
			}
			if (server == null) {
				BaseComponent[] msg = TextComponent.fromLegacyText(BungeeUtils.connectScreen("&eLe &6" + Utils.capitalize(serverKicked.getName()) + "&e s'est redémarré, merci de te reconnecter dans quelques secondes..."));
				player.sendMessage(msg);
				event.setKickReasonComponent(msg);
				return;
			}
			event.setCancelled(true);
			event.setCancelServer(server);
			player.sendMessage(ColorUtils.color(Prefix.DEFAULT_GOOD + "Le serveur &2" + Utils.capitalize(serverKicked.getName()) + "&a s'est redémarré, merci de patienter avant d'être reconnecter automatiquement."));
			ProxyServer.getInstance().getScheduler().schedule(OlympaBungee.getInstance(), () -> ServersConnection.tryConnect(player, null, serverKicked), 10, TimeUnit.SECONDS);
			return;
		}

		if (!kickReason.contains("ban")) {
			ServerInfo serverInfolobby = ServersConnection.getLobby(serverKicked);
			if (serverInfolobby == null) {
				return;
			}
			event.setCancelled(true);
			event.setCancelServer(serverInfolobby);
		}
	}
}
