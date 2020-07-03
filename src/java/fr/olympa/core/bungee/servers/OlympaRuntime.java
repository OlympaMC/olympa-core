package fr.olympa.core.bungee.servers;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import fr.olympa.api.utils.Prefix;
import fr.olympa.core.bungee.api.command.BungeeCommand;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;

public class OlympaRuntime {

	public static Thread action(String action, String serverName) {
		return action(action, serverName, null);
	}

	public static Thread action(String action, String serverName, CommandSender commandSender) {
		return action("mc " + action + " " + serverName, commandSender);
	}

	public static Thread action(String command, CommandSender commandSender) {
		return new Thread((Runnable) () -> {
			CommandSender sender;
			if (commandSender == null)
				sender = ProxyServer.getInstance().getConsole();
			else
				sender = commandSender;
			try {
				Process p = Runtime.getRuntime().exec(command);
				BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String line;
				while ((line = br.readLine()) != null && !line.isBlank())
					BungeeCommand.sendMessage(sender, Prefix.DEFAULT + line.replace("0;", "").replace("", "")
							.replace("[0m", "§f")
							.replace("[1m", "§l")
							.replace("[4m", "§n")
							.replace("[32m", "§3")
							.replace("[36m", "§b")
							.replace("[49m", "§f")
							.replace("[30m", "§0")
							.replace("[31m", "§4")
							.replace("[32m", "§2")
							.replace("[33m", "§6")
							.replace("[34m", "§1")
							.replace("[35m", "§5")
							.replace("[36m", "§3")
							.replace("[37m", "§7")
							.replace("[90m", "§8")
							.replace("[91m", "§c")
							.replace("[92m", "§a")
							.replace("[91m", "§e")
							.replace("[91m", "§9")
							.replace("[91m", "§d")
							.replace("[91m", "§b")
							.replace("[97m", "§f"));
				br.close();
				p.waitFor();
			} catch (Exception e) {
				BungeeCommand.sendMessage(sender, Prefix.DEFAULT, "&4ERROR&c " + e.getMessage());
				e.printStackTrace();
			}
		}, "Start command " + command);
	}
}
