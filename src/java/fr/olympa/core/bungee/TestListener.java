package fr.olympa.core.bungee;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class TestListener implements Listener {

	@EventHandler
	public void onJoin(LoginEvent event) {
		String eventName = event.getClass().getName();
		PendingConnection connection = event.getConnection();
		OlympaBungee.getInstance().sendMessage("EVENT : " + eventName + " UUID " + connection.getUniqueId() + " USERNAME " + connection.getName() + " IP " + connection.getAddress().getAddress().getHostAddress());
	}

	@EventHandler
	public void onJoin(PostLoginEvent event) {
		String eventName = event.getClass().getName();
		ProxiedPlayer player = event.getPlayer();
		OlympaBungee.getInstance().sendMessage("EVENT : " + eventName + " UUID " + player.getUniqueId() + " USERNAME " + player.getName() + " IP " + player.getAddress().getAddress().getHostAddress());
	}

	@EventHandler
	public void onJoin(PreLoginEvent event) {
		String eventName = event.getClass().getName();
		PendingConnection connection = event.getConnection();
		try {
			OlympaBungee.getInstance().sendMessage("UUID CRACK: " + UUID.nameUUIDFromBytes(("OfflinePlayer:" + connection.getName()).getBytes("UTF-8")));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		OlympaBungee.getInstance().sendMessage("EVENT : " + eventName + " UUID " + connection.getUniqueId() + " USERNAME " + connection.getName() + " IP " + connection.getAddress().getAddress().getHostAddress());
	}

}
