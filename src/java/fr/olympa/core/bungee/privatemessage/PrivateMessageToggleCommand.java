package fr.olympa.core.bungee.privatemessage;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.bungee.api.command.BungeeCommand;
import fr.olympa.core.bungee.utils.BungeeUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

@SuppressWarnings("deprecation")
public class PrivateMessageToggleCommand extends BungeeCommand {

	public static List<UUID> players = new ArrayList<>();

	public PrivateMessageToggleCommand(Plugin plugin) {
		super(plugin, "msgtoggle", OlympaCorePermissions.PRIVATEMESSAGE_TOGGLE, "msgt");
		allowConsole = false;
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		ProxiedPlayer player = (ProxiedPlayer) sender;
		if (players.contains(player.getUniqueId())) {
			player.sendMessage(Prefix.DEFAULT_GOOD + BungeeUtils.color("Tes messages privés sont désormais &2&lactivés&2."));
			players.remove(player.getUniqueId());
		} else {
			player.sendMessage(Prefix.DEFAULT_GOOD + BungeeUtils.color("Tes messages privés sont désormais &2&ldésactivés&2."));
			players.add(player.getUniqueId());
		}
	}

}
