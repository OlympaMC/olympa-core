package fr.olympa.core.bungee.api.command;

import java.util.Arrays;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class BungeeCommandListener implements Listener {

	@EventHandler(priority = EventPriority.LOWEST)
	public void onChat(ChatEvent event) {
		String message = event.getMessage();
		if (!message.startsWith("/"))
			return;
		String[] args = message.substring(1).split(" ");
		if (args.length == 0)
			return;
		String command = args[0].toLowerCase();
		BungeeCommand cmd = BungeeCommand.commandPreProcess.entrySet().stream().filter(entry -> entry.getKey().contains(command)).map(entry -> entry.getValue()).findFirst().orElse(null);
		if (cmd == null)
			return;
		
		ProxiedPlayer player = (ProxiedPlayer) event.getSender();
		cmd.execute(player, Arrays.copyOfRange(args, 1, args.length));
		event.setCancelled(true);
	}

}
