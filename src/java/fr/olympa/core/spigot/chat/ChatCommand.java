package fr.olympa.core.spigot.chat;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.server.OlympaServerSettings;
import fr.olympa.api.utils.Prefix;

public class ChatCommand extends OlympaCommand {
	
	public ChatCommand(Plugin plugin) {
		super(plugin, "chat", OlympaCorePermissions.CHAT_COMMAND, "tchat");
		this.addArgs(true, "slow", "clear", "mute");
	}
	
	/*
	 * Dev: Tristiisch74
	 *
	 * Commandes de gestion de tchat (sur le serveur où la commande est exécuté)
	 *
	 * ./chat slow = Désactiver antispam (ralentit le chat) ./chat clear = Vide le
	 * tchat ./chat mute = Met le tchat en pause (toggle on/off)
	 *
	 */
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		OlympaServerSettings serverSettings = OlympaServerSettings.getInstance();
		if (args[0].equalsIgnoreCase("slow")) {
			int timecooldown = 2;
			if (serverSettings.isChatSlow()) {
				sendMessage(Prefix.DEFAULT, "L'antispam a été désactivé.");
				serverSettings.setChatSlow(false);
			} else {
				sendMessage(Prefix.DEFAULT, "&aL'antispam a été activé à un message toutes les %s secondes.".replace("%s", String.valueOf(timecooldown)));
				serverSettings.setChatSlow(true);
			}
		} else if (args[0].equalsIgnoreCase("clear"))
			for (Player allPlayer : Bukkit.getOnlinePlayers()) {
				for (int i = 0; i < 100; i++)
					allPlayer.sendMessage("");
				Prefix.DEFAULT_BAD.sendMessage(allPlayer, "&lLe chat a été nettoyé.");
			}
		else if (args[0].equalsIgnoreCase("mute")) {
			if (!serverSettings.isChatMute()) {
				serverSettings.setChatMute(true);
				broadcastToAll(Prefix.DEFAULT_BAD, "&lLe chat a été désactivé.");
			} else {
				serverSettings.setChatMute(false);
				broadcastToAll(Prefix.DEFAULT_GOOD, "&lLe chat a été réactivé.");
			}
			
		} else
			sendUsage(label);
		return true;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}
	
}