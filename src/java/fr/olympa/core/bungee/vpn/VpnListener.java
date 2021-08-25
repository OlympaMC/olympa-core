package fr.olympa.core.bungee.vpn;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import fr.olympa.api.bungee.utils.BungeeUtils;
import fr.olympa.api.common.chat.Chat;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.motd.MotdListener;
import fr.olympa.core.bungee.security.SecurityHandler;
import fr.olympa.core.common.provider.AccountProvider;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class VpnListener implements Listener {

	private int lastConnected = 0;
	private long lastModif = 0;
	private LocalTime start = LocalTime.parse("08:00:00");
	private LocalTime end = LocalTime.parse("22:00:00");
	private Set<String> hostingIp = new HashSet<>();
	private Set<String> notHostingIp= new HashSet<>();
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPing(ProxyPingEvent event) {
		PendingConnection connection = event.getConnection();
		ServerPing ping = event.getResponse();
		String ip = connection.getAddress().getAddress().getHostAddress();
		if (!SecurityHandler.getInstance().isCheckVpnOnMotd())
			return;
		OlympaVpn olympaVpn;
		try {
			olympaVpn = VpnHandler.checkIP(connection);
		} catch (SQLException | IOException | NullPointerException | InterruptedException e) {
			e.printStackTrace();
			return;
		}
		if (olympaVpn == null) return;
		if (olympaVpn.isProxy() && !olympaVpn.hasWhitelistUsers())
			ping.setDescriptionComponent(new TextComponent(MotdListener.MOTD_BASE + Chat.centerMotD("§4&l⚠ §cLes VPN sont interdit.&r")));
		
		if (olympaVpn.isHosting() && !notHostingIp.contains(ip)) {
			if (!hostingIp.contains(ip)) {
				try {
					if (!AccountProvider.getter().getSQL().getPlayersByAllIp(ip).isEmpty()) {
						notHostingIp.add(ip);
						return;
					} else
						hostingIp.add(ip);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			int online = ping.getPlayers().getOnline(); 
			LocalTime target = LocalTime.now();
			boolean canUp = target.isAfter(start) && target.isBefore(end);
			if (!canUp) {
				if (lastConnected > online) {
					lastConnected--;
				} else {
					lastConnected = online;
				}
			} else {
				int objective;
				if (online < 50)
					objective = online * 3;
				else if (online < 100)
					objective = online * 2;
				else
					objective = online + 75;
				if (lastConnected < online)
					lastConnected = online;
				long time = Utils.getCurrentTimeInSeconds();
				if (time - 30 > lastModif) {
					lastModif = time;
					if (objective != lastConnected) {
						if (objective < lastConnected)
							lastConnected--;
						else if (objective / 2 > lastConnected)
							lastConnected += new Random().nextInt(5) + 1;
						else
							lastConnected++;
					}
					
				}
			}
			ping.getPlayers().setOnline(lastConnected);
			OlympaBungee.getInstance().sendMessage("&eSending fake online count to ip hosting %s > %d for %d player connected", connection.getAddress().getAddress().getHostAddress(), lastConnected, online);
		}
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
			event.setCancelReason(BungeeUtils.connectScreen("&cImpossible de se connecter au serveur avec ton IP. Contacte un Développeur ou Admin."));
			event.setCancelled(true);
			return;
		}
		SecurityHandler.getInstance().getAntibot().getCase().checkVpn(event, connection, olympaVpn);
	}
}
