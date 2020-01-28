package fr.olympa.core.bungee.login;

import fr.olympa.api.utils.Prefix;
import fr.olympa.core.bungee.servers.ServersConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class LoginRegister implements Listener {

	@EventHandler
	public void on(ChatEvent event) {
		if (!(event.getSender() instanceof ProxiedPlayer)) {
			return;
		}
		ProxiedPlayer player = (ProxiedPlayer) event.getSender();
		if (ServersConnection.isAuth(player)) {
			player.sendMessage(Prefix.DEFAULT_BAD + "Tu dois être connecté. Fait &4/login <mdp>&c.");
		}
	}

}
