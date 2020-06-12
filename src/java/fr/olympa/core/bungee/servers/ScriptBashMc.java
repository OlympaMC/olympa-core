package fr.olympa.core.bungee.servers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import fr.olympa.api.utils.Prefix;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.api.command.BungeeCommand;
import net.md_5.bungee.api.CommandSender;

public class ScriptBashMc {

	public static void action(String action, String serverName, CommandSender sender) {
		new Thread((Runnable) () -> {
			try {
				String s;
				Process p;
				p = Runtime.getRuntime().exec("mc " + action + " " + serverName);
				BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
				StringBuilder sb = new StringBuilder();
				while ((s = br.readLine()) != null)
					sb.append(s);
				String out = sb.toString().replaceAll("[]", "").replace("[0m", "&f").replace("[32m", "&3").replace("[36m", "&b").replace("[36m", "&2").replace("[0;36m", "&a").replace("[31m", "&4").replace("[0;31m", "&c");
				if (sender != null)
					BungeeCommand.sendMessage(sender, Prefix.DEFAULT, out);
				OlympaBungee.getInstance().sendMessage("§c[§4OUT§c] §c" + out);
				p.waitFor();
				p.destroy();
			} catch (IOException | InterruptedException e) {
				BungeeCommand.sendMessage(sender, Prefix.DEFAULT, "&4ERROR&c " + e.getMessage());
				e.printStackTrace();
			}
		}).start();
	}
}
