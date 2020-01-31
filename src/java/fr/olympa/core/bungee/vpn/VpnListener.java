package fr.olympa.core.bungee.vpn;

import java.sql.SQLException;

import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.core.bungee.login.events.OlympaPlayerLoginEvent;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class VpnListener implements Listener {

	@EventHandler
	public void onOlympaPlayerLogin(OlympaPlayerLoginEvent event) {
		Connection connection = event.getPlayer();
		OlympaPlayer olympaPlayer = event.getOlympaPlayer();
		String ip = event.getIp();
		if (olympaPlayer == null || !olympaPlayer.hasPermission(OlympaCorePermissions.VPN_BYPASS)) {
			boolean isVpn = false;
			try {
				OlympaVpn olympaVpn = VpnSql.getIpInfo(ip);
				if (olympaVpn == null) {
					isVpn = OlympaVpn.isVPN(connection);
					if (olympaPlayer != null) {
						VpnSql.setIp(olympaPlayer, ip, isVpn);
					} else {
						VpnSql.setIp(ip, isVpn);
					}
				} else {
					isVpn = olympaVpn.isVpn();
					if (!olympaVpn.hasUser(olympaPlayer.getId())) {
						olympaVpn.addUser(olympaPlayer);
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			if (isVpn) {
				event.setCancelReason("&cImpossible d'utiliser un VPN. \n\n&e&lSi tu pense qu'il y a une erreur, contacte un membre du staff.");
				event.setCancelled(true);
				return;
			}
		}
	}
}
