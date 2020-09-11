package fr.olympa.core.bungee;

import com.google.gson.Gson;

import io.github.waterfallmc.waterfall.event.ConnectionInitEvent;
import io.github.waterfallmc.waterfall.event.ProxyDefineCommandsEvent;
import net.md_5.bungee.api.connection.Connection;
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

	// Todo use for antibot
	@EventHandler
	public void test(ConnectionInitEvent event) {

	}

	@EventHandler
	public void test(PreLoginEvent event) {
		String eventName = event.getClass().getName();
		PendingConnection connection = event.getConnection();
		OlympaBungee.getInstance().sendMessage("EVENT : " + eventName + " UUID " + connection.getUniqueId() + " USERNAME " + connection.getName() + " IP " + connection.getAddress().getAddress().getHostAddress());
	}

	@EventHandler
	public void test(ProxyDefineCommandsEvent event) {
		System.out.println(String.format("ProxyDefineCommandsEvent Sender %s Receiver %s Commands %s", get(event.getSender()), get(event.getReceiver()), new Gson().toJson(event.getCommands())));
	}

	public String get(Connection c) {
		return c == null ? "x" : c.getAddress().getAddress().getHostAddress();
	}

}
