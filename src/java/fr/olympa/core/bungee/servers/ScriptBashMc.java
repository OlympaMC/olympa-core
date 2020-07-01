package fr.olympa.core.bungee.servers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import fr.olympa.api.utils.Prefix;
import fr.olympa.core.bungee.api.command.BungeeCommand;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;

public class ScriptBashMc {

	public static void action(String action, String serverName, CommandSender commandSender) {
		new Thread((Runnable) () -> {
			CommandSender sender;
			if (commandSender == null)
				sender = ProxyServer.getInstance().getConsole();
			else
				sender = commandSender;
			try {
				Process p = Runtime.getRuntime().exec("mc " + action + " " + serverName);
				BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String line;
				while ((line = br.readLine()) != null)
					BungeeCommand.sendMessage(sender,
							line.replace("0;", "").replace("[]", "")
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
				/*StringBuilder sb = new StringBuilder();
				while ((s = br.readLine()) != null)
					sb.append(s);
				String out = sb.toString().replaceAll("[]", "").replace("[0m", "&f").replace("[32m", "&3").replace("[36m", "&b").replace("[36m", "&2").replace("[0;36m", "&a").replace("[31m", "&4").replace("[0;31m", "&c");
				OlympaBungee.getInstance().sendMessage("§c[§4OUT§c] §c" + out);*/
				p.waitFor();
			} catch (IOException | InterruptedException e) {
				BungeeCommand.sendMessage(sender, Prefix.DEFAULT, "&4ERROR&c " + e.getMessage());
				e.printStackTrace();
			}
		}, "Server Script").start();
	}
}
