package fr.tristiisch.olympa.core.chat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import fr.tristiisch.olympa.api.Prefix;
import fr.tristiisch.olympa.api.command.OlympaCommand;
import fr.tristiisch.olympa.api.objects.OlympaServerSettings;
import fr.tristiisch.olympa.api.utils.Utils;
import fr.tristiisch.olympa.core.permission.OlympaPermission;

public class ChatCommand extends OlympaCommand {

	public ChatCommand(Plugin plugin) {
		super(plugin, "chat", OlympaPermission.CHAT_COMMAND, "tchat");
		this.setUsageString("<slow|clear|mute>");
		this.setMinArg(1);
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
	public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
		OlympaServerSettings serverSettings = OlympaServerSettings.getInstance();

		if (args[0].equalsIgnoreCase("slow")) {

			final int timecooldown = 2;
			if (serverSettings.isChatSlow()) {
				this.sendMessage(Prefix.DEFAULT, "L'antispam a été désactivé.");
				serverSettings.setChatSlow(false);
			} else {
				this.sendMessage(Prefix.DEFAULT, "&aL'antispam a été activé à un message toutes les %second% secondes.".replaceFirst("%second%", String.valueOf(timecooldown)));
				serverSettings.setChatSlow(true);
			}

		} else if (args[0].equalsIgnoreCase("clear")) {

			for (final Player allPlayer : Bukkit.getOnlinePlayers()) {
				for (int i = 0; i < 100; i++) {
					allPlayer.sendMessage("");
				}
				this.sendMessage(allPlayer, Prefix.DEFAULT_BAD, "&lLe chat a été nettoyé.");
			}

		} else if (args[0].equalsIgnoreCase("mute")) {

			if (!serverSettings.isChatMute()) {
				serverSettings.setChatMute(true);
				this.sendMessageToAll(Prefix.DEFAULT_GOOD, "&lLe chat a été réactivé.");
			} else {
				serverSettings.setChatMute(false);
				this.sendMessageToAll(Prefix.DEFAULT_BAD, "&lLe chat a été désactivé.");
			}

		} else {
			this.sendUsage(label);
		}
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 1) {
			List<String> postentielArgs = Utils.startWords(args[0], new HashSet<>(Arrays.asList("slow", "clear", "mute")));
			return postentielArgs;
		}
		return null;
	}

}