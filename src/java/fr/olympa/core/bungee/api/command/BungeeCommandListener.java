package fr.olympa.core.bungee.api.command;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class BungeeCommandListener implements Listener {

	@EventHandler
	public void onChat(ChatEvent event) {
		String message = event.getMessage();
		if (!message.startsWith("/"))
			return;
		String[] args = message.substring(1).split(" ");
		if (args.length == 0)
			return;
		BungeeCommand command = BungeeCommand.commandPreProcess.get(args[0]);
		if (command == null)
			return;
		
		ProxiedPlayer player = (ProxiedPlayer) event.getSender();
		command.execute(player, args);
	}

}
