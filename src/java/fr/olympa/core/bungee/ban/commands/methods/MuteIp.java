package fr.olympa.core.bungee.ban.commands.methods;

import fr.olympa.api.player.OlympaPlayer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;

public class MuteIp {

	@SuppressWarnings("deprecation")
	public static void addMute(long authorId, CommandSender sender, String ip, String[] args, OlympaPlayer olympaPlayer) {

		ProxyServer.getInstance().getPlayers().stream().filter(player -> player.getAddress().getAddress().getHostAddress().equals(ip)).forEach(player -> {
			MutePlayer.addMute(authorId, sender, player.getName(), player.getUniqueId(), args, olympaPlayer);
		});
	}

}
