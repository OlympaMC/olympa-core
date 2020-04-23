package fr.olympa.core.bungee.servers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import fr.olympa.core.bungee.OlympaBungee;
import net.md_5.bungee.api.CommandSender;

public class ServerStartStop {

	@SuppressWarnings("deprecation")
	public static void action(String action, String serverName, CommandSender sender) {
		new Thread((Runnable) () -> {
			try {
				String s;
				Process p;
				p = Runtime.getRuntime().exec("mc " + action + " " + serverName);
				BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
				while ((s = br.readLine()) != null) {
					if (sender != null) {
						sender.sendMessage("§c[§4OUT§c] §c" + s);
					}
					OlympaBungee.getInstance().sendMessage("§c[§4OUT§c] §c" + s);
				}
				p.waitFor();
				if (sender != null) {
					sender.sendMessage("§c[§4OUT§c] §c" + p.exitValue());
				}
				OlympaBungee.getInstance().sendMessage("§c[§4OUT§c] §c" + p.exitValue());
				p.destroy();
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		});
	}
}
