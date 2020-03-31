package fr.olympa.core.bungee.vpn;

import java.sql.SQLException;

import fr.olympa.core.bungee.utils.BungeeUtils;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class VpnListener implements Listener {

//	@EventHandler
//	public void onOlympaPlayerLogin(OlympaPlayerLoginEvent event) {
//		Connection connection = event.getPlayer();
//		OlympaPlayer olympaPlayer = event.getOlympaPlayer();
//		String ip = event.getIp();
//		if (olympaPlayer == null
//				|| !olympaPlayer.hasPermission(OlympaCorePermissions.VPN_BYPASS)) {
//			boolean isVpn = false;
//			try {
//				OlympaVpn olympaVpn = VpnSql.getIpInfo(ip);
//				if (olympaVpn == null) {
//					isVpn = OlympaVpn.isVPN(connection);
//					if (olympaPlayer != null) {
//						VpnSql.setIp(olympaPlayer, ip, isVpn);
//					} else {
//						VpnSql.setIp(ip,
//								isVpn);
//					}
//				} else {
//					isVpn = olympaVpn.isVpn();
//					if (!olympaVpn.hasUser(olympaPlayer.getId())) {
//						olympaVpn.addUser(olympaPlayer);
//					}
//				}
//			} catch (SQLException e) {
//				e.printStackTrace();
//			}
//			if (isVpn) {
//				event.setCancelReason("&cImpossible d'utiliser un VPN. \n\n&e&lSi tu pense qu'il y a une erreur, contacte un membre du staff.");
//				event.setCancelled(true);
//				return;
//			}
//		}
//	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPreLogin(PreLoginEvent event) {
		PendingConnection connection = event.getConnection();
		String username = connection.getName();
		String ip = connection.getAddress().getAddress().getHostAddress();
		boolean isVpn = false;
		try {
			OlympaVpn olympaVpn = VpnSql.getIpInfo(ip);
			if (olympaVpn == null) {
				isVpn = OlympaVpn.isVPN(event.getConnection());
				VpnSql.setIp(username, ip, isVpn);
			} else {
				isVpn = olympaVpn.isVpn();
				if (!olympaVpn.hasUser(username)) {
					olympaVpn.addUser(username);
					VpnSql.saveIp(olympaVpn);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if (isVpn) {
			event.setCancelReason(BungeeUtils.connectScreen("&cImpossible d'utiliser un VPN. \n\n&e&lSi tu pense qu'il y a une erreur, contacte un membre du staff."));
			event.setCancelled(true);
			return;
		}
	}
}
