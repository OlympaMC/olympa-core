package fr.olympa.bungee.maintenance;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import fr.olympa.api.objects.OlympaConsole;
import fr.olympa.api.utils.SpigotUtils;
import fr.olympa.api.utils.Utils;
import fr.olympa.bungee.OlympaBungee;
import fr.olympa.bungee.utils.BungeeConfigUtils;
import fr.olympa.bungee.utils.BungeeUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.event.EventHandler;

@SuppressWarnings("deprecation")
public class ConnectionListener implements Listener {

	@EventHandler
	public void LoginEvent(LoginEvent event) {
		if (event.isCancelled()) {
			return;
		}

		UUID playerUUID = event.getConnection().getUniqueId();
		String playername = event.getConnection().getName();
		// Vérifie si le joueur n'utilise pas le joueur console.
		if (playerUUID == OlympaConsole.getUniqueId() || playername.equalsIgnoreCase(OlympaConsole.getName())) {
			event.setCancelReason(BungeeUtils.connectScreen("&cImpossible de se connecter avec ce compte."));
			event.setCancelled(true);
			return;

		}
	}

	@EventHandler
	public void PreLoginEvent(PreLoginEvent event) {
		if (event.isCancelled()) {
			return;
		}

		String playername = event.getConnection().getName();
		Configuration config = BungeeConfigUtils.getConfig("maintenance");
		int status = config.getInt("settings.status");

		// Vérifie si le serveur n'est pas en maintenance
		if (status == 1) {
			if (!config.getStringList("whitelist").contains(playername)) {
				if (config.getString("settings.message").isEmpty()) {
					event.setCancelReason(BungeeUtils.connectScreen("&cLe serveur est actuellement en maintenance."));
				} else {
					String reason = config.getString("settings.message");
					event.setCancelReason(BungeeUtils.connectScreen("&cLe serveur est actuellement en maintenance.\n\n&c&nRaison:&c " + reason));
				}
				event.setCancelled(true);
				ProxyServer.getInstance().getLogger().log(Level.INFO, SpigotUtils.color("&d" + playername + " ne peux pas se connecter (serveur en maintenance)"));
				return;
			}

		} else if (status == 2) {
			String playerName = playername;
			if (!config.getStringList("whitelist").contains(playerName)) {
				event.setCancelReason(BungeeUtils.connectScreen("&cLe serveur est actuellement en développement.\n\n&aPlus d'infos sur le twitter &n@Olympa_fr"));
				event.setCancelled(true);
				ProxyServer.getInstance().getLogger().log(Level.INFO, SpigotUtils.color("&d" + playername + " ne peux pas se connecter (serveur en dev)"));
				return;
			}
		} else if (status == 3) {
			String playerName = playername;
			if (!config.getStringList("whitelist").contains(playerName)) {
				event.setCancelReason(BungeeUtils.connectScreen("&cNous ouvrons bientôt !.\n\n&aPlus d'infos sur le twitter &n@Olympa_fr"));
				event.setCancelled(true);
				ProxyServer.getInstance().getLogger().log(Level.INFO, SpigotUtils.color("&d" + playername + " ne peux pas se connecter (serveur en dev, open soon)"));
				return;
			}
		}

		// Vérifie si l'ip est correct
		if (!Utils.getAfterFirst(event.getConnection().getVirtualHost().getHostName(), ".").equalsIgnoreCase("olympa.fr")) {
			// event.setCancelReason(BungeeUtils.connectScreen("&cMerci de vous connecter
			// avec l'adresse &nplay.olympa.fr"));
			// event.setCancelled(true);
			// return;
			ProxyServer.getInstance().getScheduler().schedule(OlympaBungee.getInstance(), () -> {
				ProxiedPlayer player = ProxyServer.getInstance().getPlayer(playername);
				if (player != null) {
					player.sendMessage("&cMerci de vous connecter avec l'adresse &nplay.olympa.fr&c la prochaine fois.");
				}

			}, 5, TimeUnit.SECONDS);
		}

		// Vérifie si le joueur n'est pas déjà connecté
		ProxiedPlayer target = ProxyServer.getInstance().getPlayer(playername);
		if (target != null) {
			if (!target.getAddress().getAddress().getHostAddress().equals(event.getConnection().getAddress().getAddress().getHostAddress())) {
				event.setCancelReason(BungeeUtils.connectScreen("&cVotre compte est déjà connecté avec une IP différente de la votre."));
				event.setCancelled(true);
				return;
			}
			event.setCancelReason(BungeeUtils.connectScreen("&cVotre compte est déjà connecté."));
		}
	}

	@EventHandler
	public void ServerConnectEvent(ServerConnectEvent event) {
		// ProxiedPlayer player = event.getPlayer();
		// ServerInfo serverTarget = event.getTarget();
		// serverTarget.getPlayers().stream().filter(p ->
		// player.getUniqueId().equals(p.getUniqueId())).forEach(p ->
		// p.getPendingConnection().disconnect());
	}

	@EventHandler
	public void ServerKickEvent(ServerKickEvent event) {

		String kickReason = ChatColor.stripColor(BaseComponent.toLegacyText(event.getKickReasonComponent()));
		if (kickReason.contains("Server is restarting") || kickReason.contains("Server closed")) {

			ProxiedPlayer player = event.getPlayer();
			ServerInfo server = event.getKickedFrom();
			ServerInfo serverInfolobby = ProxyServer.getInstance().getServers().values().stream().filter(Sname -> server != Sname && Sname.getName().startsWith("lobby")).findFirst().orElse(null);
			if (serverInfolobby == null) {
				event.setKickReason(SpigotUtils.color("&eLe &6" + Utils.capitalize(server.getName()) + "&e s'est redémarré, merci de vous reconnecter."));
				return;
			}
			event.setCancelServer(serverInfolobby);
			event.setCancelled(true);
			player.sendMessage(SpigotUtils.color("&6Olympa &7» &eLe &6" + Utils.capitalize(server.getName()) + "&e s'est redémarré, vous êtes désormais au " + Utils.capitalize(serverInfolobby.getName()) + "."));
			return;
		} else if (!kickReason.contains("ban")) {
			ServerInfo server = event.getKickedFrom();
			ServerInfo serverInfolobby = ProxyServer.getInstance().getServers().values().stream().filter(Sname -> server != Sname && Sname.getName().startsWith("lobby")).findFirst().orElse(null);
			if (serverInfolobby == null) {
				return;
			}
			event.setCancelServer(serverInfolobby);
			event.setCancelled(true);
		}
	}
}
