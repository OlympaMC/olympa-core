package fr.olympa.core.bungee.ban.commands.methods;

import java.util.UUID;

import fr.olympa.api.objects.OlympaPlayer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;

public class MuteIp {

	public static void addMute(UUID author, CommandSender sender, String ip, String[] args, OlympaPlayer olympaPlayer) {

		ProxyServer.getInstance().getPlayers().stream().filter(player -> player.getAddress().getAddress().getHostAddress().equals(ip)).forEach(player -> {
			MutePlayer.addMute(author, sender, player.getName(), player.getUniqueId(), args, olympaPlayer);
		});
	}

}
