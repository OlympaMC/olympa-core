package fr.olympa.core.ban.commands.methods;

import java.util.UUID;

import fr.tristiisch.emeraldmc.api.commons.object.EmeraldPlayer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;

public class KickIp {

	public static void addKick(final UUID author, final CommandSender sender, final String ip, final String[] args, final EmeraldPlayer emeraldPlayer) {
		ProxyServer.getInstance().getPlayers().stream().filter(player -> player.getAddress().getAddress().getHostAddress().equals(ip)).forEach(player -> {
			KickPlayer.addKick(author, sender, player.getName(), player.getUniqueId(), args, emeraldPlayer);
		});
	}
}
