package fr.olympa.core.bungee.vpn;

import java.io.IOException;
import java.sql.SQLException;

import fr.olympa.api.common.chat.Chat;
import fr.olympa.core.bungee.motd.MotdListener;
import fr.olympa.core.bungee.security.SecurityHandler;
import fr.olympa.core.bungee.utils.BungeeUtils;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class VpnListener implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPing(ProxyPingEvent event) {
		PendingConnection connection = event.getConnection();
		ServerPing ping = event.getResponse();

		if (!SecurityHandler.getInstance().isCheckVpnOnMotd())
			return;
		OlympaVpn olympaVpn;
		try {
			olympaVpn = VpnHandler.checkIP(connection);
		} catch (SQLException | IOException | NullPointerException | InterruptedException e) {
			e.printStackTrace();
			return;
		}
		if (olympaVpn != null && olympaVpn.isProxy() && !olympaVpn.hasWhitelistUsers())
			ping.setDescriptionComponent(new TextComponent(MotdListener.MOTD_BASE + Chat.centerMotD("§4&l⚠ §cLes VPN sont interdit.")));
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.LOWEST)
	public void onLoginEvent(LoginEvent event) {
		PendingConnection connection = event.getConnection();
		if (!SecurityHandler.getInstance().isCheckVpn())
			return;
		OlympaVpn olympaVpn;
		try {
			olympaVpn = VpnHandler.checkIP(connection);
		} catch (SQLException | NullPointerException | IOException | InterruptedException e) {
			e.printStackTrace();
			return;
		}
		if (olympaVpn == null) {
			event.setCancelReason(BungeeUtils.connectScreen("&4AntiBot Activé &c> Impossible de se connecter au serveur avec ton IP. Contacte un Développeur ou Admin."));
			event.setCancelled(true);
			return;
		}
		if ((olympaVpn.isProxy() || olympaVpn.isHosting()) && (olympaVpn.getWhitelistUsers() == null || !olympaVpn.getWhitelistUsers().contains(connection.getName()))) {
			System.out.println("§cVPN DETECTED > " + connection.getName() + " " + connection.getAddress().getAddress().getHostAddress());
			event.setCancelReason(BungeeUtils.connectScreen("&cImpossible d'utiliser un VPN.\n\n&e&lSi tu penses qu'il y a une erreur, contacte un membre du staff."));
			event.setCancelled(true);
			return;
		}
	}
}
