package fr.olympa.core.bungee.servers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import fr.olympa.api.utils.Prefix;
import fr.olympa.core.bungee.api.command.BungeeCommand;
import net.md_5.bungee.api.CommandSender;

public class ScriptBashMc {

	public static void action(String action, String serverName, CommandSender sender) {
		new Thread((Runnable) () -> {
			try {
				Process p = Runtime.getRuntime().exec("mc " + action + " " + serverName);
				if (sender != null) {
					BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
					String line;
					while ((line = br.readLine()) != null) {
						BungeeCommand.sendMessage(sender, line.replace("[0m", "&f").replace("[32m", "&3").replace("[36m", "&b").replace("[36m", "&2").replace("[0;36m", "&a").replace("[31m", "&4").replace("[0;31m", "&c"));
					}
					br.close();
				}
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
