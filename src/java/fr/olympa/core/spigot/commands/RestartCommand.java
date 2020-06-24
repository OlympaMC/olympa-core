package fr.olympa.core.spigot.commands;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.permission.OlympaCorePermissions;

public class RestartCommand extends OlympaCommand {

	public RestartCommand(Plugin plugin) {
		super(plugin, "restart", "Redémarre le serveur", OlympaCorePermissions.SERVER_RESTART_COMMAND);
		allowConsole = true;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		restart();
		return false;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}
	
	public void restart() {
		plugin.getServer().shutdown();
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
