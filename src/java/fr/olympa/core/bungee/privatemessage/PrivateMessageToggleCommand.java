package fr.olympa.core.bungee.privatemessage;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import fr.olympa.api.permission.OlympaCorePermissions;
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
		this.allowConsole = false;
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		ProxiedPlayer player = (ProxiedPlayer) sender;

		if (players.contains(player.getUniqueId())) {
			player.sendMessage(BungeeUtils.color("&eOlympa &7» &cVous messages privés sont désormais &2&lactivés"));
			players.remove(player.getUniqueId());
		} else {
			player.sendMessage(BungeeUtils.color("&eOlympa &7» &cVous messages privés sont désormais &4&ldésactivés"));
			players.add(player.getUniqueId());
		}
	}

}
