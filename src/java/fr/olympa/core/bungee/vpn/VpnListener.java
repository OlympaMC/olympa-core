package fr.olympa.core.bungee.vpn;

import java.io.IOException;
import java.sql.SQLException;

import fr.olympa.core.bungee.security.SecurityHandler;
import fr.olympa.core.bungee.utils.BungeeUtils;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class VpnListener implements Listener {

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
		if (olympaVpn != null && (olympaVpn.isProxy() || olympaVpn.isHosting())) {
			System.out.println("§cVPN DETECTED > " + connection.getName() + " " + connection.getAddress().getAddress().getHostAddress());
			event.setCancelReason(BungeeUtils.connectScreen("&cImpossible d'utiliser un VPN.\n\n&e&lSi tu pense qu'il y a une erreur, contacte un membre du staff."));
			event.setCancelled(true);
			return;
		}
	}
}
