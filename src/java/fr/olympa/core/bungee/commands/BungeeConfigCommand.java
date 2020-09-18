package fr.olympa.core.bungee.commands;

import java.io.IOException;
import java.text.DecimalFormat;

import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.bungee.api.command.BungeeCommand;
import fr.olympa.core.bungee.api.config.BungeeCustomConfig;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeeConfigCommand extends BungeeCommand {

	public BungeeConfigCommand(Plugin plugin) {
		super(plugin, "bungeeconfig", OlympaCorePermissions.BUNGEE_CONFIG_COMMAND, "bconfig");
		description = "Gère les configs bungeecord";
		addArgs(true, "config");
		addArgs(true, "reload", "save");
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		BungeeCustomConfig config = BungeeCustomConfig.getConfig(args[0]);
		if (config == null) {
			this.sendMessage(Prefix.DEFAULT_BAD, "Config &4%s&c inconnu.", args[0]);
			return;
		}
		switch (args[1]) {
		case "reload":
			long time = System.nanoTime();
			try {
				config.reload();
				time = System.nanoTime() - time;
				sendMessage(Prefix.DEFAULT_GOOD, "Config &2%s&a chargé en &2%s secondes", config.getName(), new DecimalFormat("0.#").format(time / 1000000000d));
			} catch (IOException e) {
				sendMessage(Prefix.ERROR, "Impossible de charger la config &4%s&c : &4%s&c.", config.getName(), e.getMessage());
				e.printStackTrace();
			}
			break;
		case "save":
			time = System.nanoTime();
			try {
				config.save();
				time = System.nanoTime() - time;
				sendMessage(Prefix.DEFAULT_GOOD, "Config &2%s&a sauvegarder en &2%s secondes", config.getName(), new DecimalFormat("0.#").format(time / 1000000000d));
			} catch (IOException e) {
				sendMessage(Prefix.ERROR, "Impossible de sauvegarder la config &4%s&c sur le disque : &4%s&c.", config.getName(), e.getMessage());
				e.printStackTrace();
			}
			break;
		default:
			sendUsage();
			break;
		}
	}
}
