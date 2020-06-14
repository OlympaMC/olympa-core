package fr.olympa.core.bungee.servers.commands;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.core.bungee.api.command.BungeeCommand;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Plugin;

public class RestartBungeeCommand extends BungeeCommand {

	public RestartBungeeCommand(Plugin plugin) {
		super(plugin, "restartbungee", OlympaCorePermissions.SERVER_RESTART_COMMAND);
		allowConsole = true;
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		restart();
	}

	public void restart() {
		plugin.getProxy().stop();
		new Thread((Runnable) () -> {
			try {
				String s;
				Process p;
				p = Runtime.getRuntime().exec("sh start.sh");
				BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
				StringBuilder sb = new StringBuilder();
				while ((s = br.readLine()) != null)
					sb.append(s);
				System.out.println(sb.toString());
				p.waitFor();
				p.destroy();
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}).start();
	}
}
