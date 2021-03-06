package fr.olympa.core.bungee.servers;

import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import fr.olympa.api.bungee.utils.BungeeUtils;
import fr.olympa.api.common.chat.ColorUtils;
import fr.olympa.api.common.chat.TxtComponentBuilder;
import fr.olympa.api.common.server.OlympaServer;
import fr.olympa.api.common.server.ServerInfoAdvancedBungee;
import fr.olympa.api.spigot.utils.ProtocolAPI;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.datamanagment.AuthListener;
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
		ServerInfoAdvancedBungee servInfo = OlympaBungee.getInstance().getMonitoring().getMonitor(serverKicked);
		ProxiedPlayer player = event.getPlayer();
		Entry<OlympaServer, Integer> entryOlympaServer = OlympaServer.getOlympaServerWithId(serverKicked.getName());
		OlympaServer olympaServer = entryOlympaServer != null ? entryOlympaServer.getKey() : null;
		if (olympaServer == null || olympaServer == OlympaServer.AUTH) {
			event.setCancelled(false);
			return;
		}
		String kickReason = ColorUtils.stripColor(BaseComponent.toLegacyText(event.getKickReasonComponent()));

		if (AuthListener.containsWait(player))
			return; // il est en cours de suppression = il a quitté le serveur de lui-même

		OlympaBungee.getInstance().sendMessage("§6" + player.getName() + "§7 a été kick pour \"§e" + kickReason + "§7\" (état : " + event.getState() + ") " + event.getCause().name());
		if (kickReason.contains("whitelist")) {
			TxtComponentBuilder.of(Prefix.NONE, "Tu n'as pas accès au serveur &4%s&c.", serverKicked.getName());
			return;
		}
		if (kickReason.contains("restarting") || kickReason.contains("Server closed") || kickReason.equals("The server you were previously on went down, you have been connected to a fallback server")) {
			ServerInfo serverFallback = null;
			if (olympaServer.hasMultiServers() && olympaServer.sendToOtherServer()) {
				serverFallback = ServersConnection.getBestServer(olympaServer, serverKicked);
				if (serverFallback != null) {
					event.setCancelled(true);
					event.setCancelServer(serverFallback);
					player.sendMessage(TextComponent
							.fromLegacyText(Prefix.DEFAULT_GOOD + ColorUtils.color(
									"Le serveur &2" + Utils.capitalize(serverKicked.getName()) + "&a redémarre, tu es désormais au serveur &2" + Utils.capitalize(serverFallback.getName()) + "&a.")));
					return;
				}
			}
			serverFallback = ServersConnection.getBestServer(OlympaServer.LOBBY, serverKicked);
			if (serverFallback == null)
				serverFallback = ServersConnection.getBestServer(OlympaServer.AUTH, serverKicked);
			if (serverFallback == null) {
				String msg = BungeeUtils.connectScreen("&eLe &6%s&e redémarre, merci de te reconnecter dans quelques secondes...", Utils.capitalize(serverKicked.getName()));
				player.sendMessage(msg);
				event.setKickReason(msg);
				return;
			}
			event.setCancelled(true);
			event.setCancelServer(serverFallback);
			player.sendMessage(TextComponent.fromLegacyText(Prefix.DEFAULT_GOOD + ColorUtils.color(
					"Le serveur &2" + Utils.capitalize(serverKicked.getName()) + "&a redémarre, merci de patienter quelques secondes avant d'être reconnecté automatiquement.")));
//			if (servInfo != null) {
//				new QueueSpigotOlympaServerTask(player, servInfo);
//				ServersConnection.addConnection(new WaitingConnection(player.getUniqueId(), servInfo, null, false));
//			} else
				ServersConnection.tryConnect(player, olympaServer, true);
			return;
		}
		if (kickReason.startsWith("Outdated server! I'm still on")) {
			String serverVersion = kickReason.replaceFirst("Outdated server! I'm still on ", "");
			List<ProtocolAPI> playerVersion = ProtocolAPI.getAll(player.getPendingConnection().getVersion());
			event.setCancelled(true);
			event.setCancelServer(null);
			event.setKickReasonComponent(
					TextComponent.fromLegacyText(Prefix.BAD.formatMessage("Version du %s %s < Ta version (%s)",
							serverKicked.getName(), serverVersion, playerVersion.stream().map(ProtocolAPI::getName).collect(Collectors.joining(", ")))));
			return;
		}
		if (kickReason.startsWith("Outdated client! Please use")) {
			String serverVersion = kickReason.replaceFirst("Outdated client! Please use ", "");
			List<ProtocolAPI> playerVersion = ProtocolAPI.getAll(player.getPendingConnection().getVersion());
			event.setCancelled(true);
			event.setCancelServer(null);
			event.setKickReasonComponent(
					TextComponent.fromLegacyText(Prefix.BAD.formatMessage("Version du %s %s > Ta version (%s)",
							serverKicked.getName(), serverVersion, playerVersion.stream().map(ProtocolAPI::getName).collect(Collectors.joining(", ")))));
			return;
		}
		if (!kickReason.contains("ban")) {
			//			if (player.getServer() != null && player.getServer().getInfo().getName().equals(serverKicked.getName())) {
			//				event.setCancelled(true);
			//				event.setCancelServer(null);
			//				player.sendMessage(TextComponent.fromLegacyText(Prefix.BAD.formatMessage("Impossible de se connecter au serveur &4%s&c : &4%s&c.", serverKicked.getName(), kickReason)));
			//				return;
			//			}
			ServerInfo serverInfolobby = ServersConnection.getBestServer(OlympaServer.LOBBY, serverKicked);
			if (serverInfolobby == null || !serverInfolobby.canAccess(player))
				return;
			event.setCancelled(true);
			event.setCancelServer(serverInfolobby);
			OlympaBungee.getInstance().sendMessage("§6" + player.getName() + "§7 a envoyé au \"§e" + serverInfolobby.getName() + "§7\".");
			player.sendMessage(TextComponent.fromLegacyText(Prefix.BAD.formatMessage("Tu as été kick de &4%s&c pour &4%s&c.", serverKicked.getName(), kickReason)));
		}
	}
}
