package fr.olympa.core.bungee.vpn;

import java.io.IOException;
import java.sql.SQLException;

import fr.olympa.api.chat.Chat;
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

	@EventHandler(priority = EventPriority.HIGH)
	public void onPing(ProxyPingEvent event) {
		PendingConnection connection = event.getConnection();
		ServerPing ping = event.getResponse();

		if (!SecurityHandler.CHECK_VPN_ON_MOTD)
			return;
		OlympaVpn olympaVpn;
		try {
			olympaVpn = VpnHandler.checkIP(connection);
		} catch (SQLException | IOException | NullPointerException e) {
			e.printStackTrace();
			return;
		}
		if (olympaVpn != null && olympaVpn.isProxy())
			ping.setDescriptionComponent(new TextComponent(MotdListener.MOTD_BASE + Chat.centerMotD("§4&l⚠ §cLes VPN sont interdit.")));
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.LOWEST)
	public void onLoginEvent(LoginEvent event) {
		PendingConnection connection = event.getConnection();
		if (!SecurityHandler.CHECK_VPN)
			return;
		OlympaVpn olympaVpn;
		try {
			olympaVpn = VpnHandler.checkIP(connection);
		} catch (SQLException | IOException | NullPointerException e) {
			e.printStackTrace();
			return;
		}
		if (olympaVpn == null) {
			event.setCancelReason(BungeeUtils.connectScreen("&4AntiBot Activé &c> Tu dois t'inscrire sur le site pour te connecter\n&e&nwww.olympa.fr"));
			event.setCancelled(true);
			return;
		}

		if (olympaVpn.isProxy() || olympaVpn.isHosting()) {
			System.out.println("§cVPN DETECTED > " + connection.getName() + " " + connection.getAddress().getAddress().getHostAddress());
			event.setCancelReason(BungeeUtils.connectScreen("&cImpossible d'utiliser un VPN.\n\n&e&lSi tu pense qu'il y a une erreur, contacte un membre du staff."));
			event.setCancelled(true);
			return;
		}
	}
}
